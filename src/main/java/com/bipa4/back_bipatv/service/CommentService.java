package com.bipa4.back_bipatv.service;

import com.bipa4.back_bipatv.dao.CommentDAO;
import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dto.comment.ChildCommentResponse;
import com.bipa4.back_bipatv.dto.comment.CommentRequest;
import com.bipa4.back_bipatv.dto.comment.CommentResponse;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.entity.Channels;
import com.bipa4.back_bipatv.entity.Comments;
import com.bipa4.back_bipatv.entity.Videos;
import com.bipa4.back_bipatv.exception.AuthorizationException;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.repository.ChannelRepository;
import com.bipa4.back_bipatv.repository.CommentRepository;
import com.bipa4.back_bipatv.repository.VideoRepository;
import com.github.f4b6a3.ulid.Ulid;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentDAO commentDAO;
  private final VideoRepository videoRepository;
  private final CommentRepository commentRepository;
  private final ChannelRepository channelRepository;


  public List<CommentResponse> findParentComments(String videoId) {
    List<CommentResponse> list = commentDAO.findParentComments(videoId);
    return list;
  }

  public List<CommentResponse> findOldParentComments(String videoId) {
    List<CommentResponse> list = commentDAO.findOldParentComments(videoId);
    return list;
  }

  public List<CommentResponse> findPopularityParentComments(String videoId) {
    List<CommentResponse> list = commentDAO.findPopularityParentComments(videoId);
    return list;
  }


  public List<ChildCommentResponse> findChildComments(String videoId, int groupIndex) {
    List<ChildCommentResponse> list = commentDAO.findChildComments(videoId, groupIndex);
    return list;
  }

  @Transactional
  public boolean saveParentComment(Accounts account, CommentRequest commentRequest) {
    Comments comment = convertDtoToEntityForInsert(account, commentRequest, null);
    return commentDAO.saveParentComment(comment);
  }

  public boolean saveChildComment(Accounts account, CommentRequest commentRequest,
      Integer groupIndex) {
    Comments comment = convertDtoToEntityForInsert(account, commentRequest, groupIndex);
    return commentDAO.saveChildComment(comment);

  }

  @Transactional
  public boolean updateComment(CommentRequest commentRequest, Accounts account) {
    Comments comment = convertDtoToEntityForUpdate(commentRequest, account);
    return commentDAO.saveParentComment(comment);
  }

  @Transactional
  public boolean deleteParentComment(UUID commentId, Accounts account) {
    Comments comment = commentRepository.findById(commentId).orElse(null);

    // 댓글이 존재하지 않는다면.
    if (comment == null) {
      throw new CustomApiException(ErrorCode.No_EXIST_COMMENT);
    }

    // 본인이 작성한 댓글이 아니라면.
    if (!Objects.equals(comment.getAccounts().getAccountId(), account.getAccountId())) {
      throw new AuthorizationException();
    }

    return commentDAO.deleteParentComment(comment.getVideos().getVideoId(),
        comment.getGroupIndex());
  }

  @Transactional
  public boolean deleteChildComment(UUID commentId, Accounts account) {
    Comments comment = commentRepository.findById(commentId).orElse(null);

    // 댓글이 존재하지 않는다면.
    if (comment == null) {
      throw new CustomApiException(ErrorCode.No_EXIST_COMMENT);
    }

    // 본인이 작성한 댓글이 아니라면.
    if (!Objects.equals(comment.getAccounts().getAccountId(), account.getAccountId())) {
      throw new AuthorizationException();
    }

    return commentDAO.deleteChildComment(commentId);
  }


  private Comments convertDtoToEntityForInsert(Accounts account, CommentRequest commentRequest,
      Integer groupIndex) {
    Comments comment = new Comments();

    Videos video = videoRepository.findById(commentRequest.getVideoId()).orElse(null);

    // 비디오가 없다면
    if (video == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    Timestamp now = Timestamp.valueOf(
        LocalDateTime.now().plusHours(9)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    groupIndex = commentRequest.getParentChild() == 0 ?
        commentDAO.findMaxGroupIndex(video.getVideoId()) + 1 : groupIndex;

    // Comment DTO를 만드는 부분
    try {
      comment.setVideos(video);
      comment.setAccounts(account);
      comment.setGroupIndex(groupIndex);
      comment.setContent(commentRequest.getContent());
      comment.setParentChild(commentRequest.getParentChild());
      comment.setCreateAt(now);
      comment.setIsUpdated(false);
      comment.setIsPicked(false);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INSERT_DTO_ERROR);
    }
    return comment;
  }

  private Comments convertDtoToEntityForUpdate(CommentRequest commentRequest, Accounts account) {
    Comments comment = commentDAO.findByCommentId(commentRequest.getCommentId());

    // 댓글이 존재하지 않는다면
    if (comment == null) {
      throw new CustomApiException(ErrorCode.No_EXIST_COMMENT);
    }

    // 본인이 작성한 댓글이 아니라면.
    if (!Objects.equals(comment.getAccounts().getAccountId(), account.getAccountId())) {
      throw new AuthorizationException();
    }

    Timestamp now = Timestamp.valueOf(
        LocalDateTime.now().plusHours(9)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    // Comment DTO를 만드는 부분
    try {
      comment.setContent(commentRequest.getContent());
      comment.setCreateAt(now);
      comment.setIsUpdated(true);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INSERT_DTO_ERROR);
    }
    return comment;
  }

  @Transactional
  public boolean pickComment(CommentRequest commentRequest, Accounts account, String videoId) {
    Comments comment = commentDAO.findByCommentId(commentRequest.getCommentId());
    Channels channels = comment.getVideos().getChannelId();
    Optional<Channels> loginUserChannel = channelRepository.findByChannelToAccountId(
        account.getAccountId());

    // 댓글이 존재하지 않는다면
    if (comment == null) {
      throw new CustomApiException(ErrorCode.No_EXIST_COMMENT);
    }

    // channel 주인이 아니라면.
    if (loginUserChannel.isEmpty() || !Objects.equals(channels, loginUserChannel.get())) {
      throw new AuthorizationException();
    }

    // 기존 고정 댓글 조회
    Comments pickedComment = commentDAO.findPickedParentComment(videoId);

    try {
      if (pickedComment != null) {
        // 기존 고정 댓글이 있는 경우 고정을 취소
        pickedComment.setIsPicked(false);
        commentDAO.saveParentComment(pickedComment);
      }

      // 현재 요청으로 받은 댓글을 고정
      comment.setIsPicked(true);
      commentDAO.saveParentComment(comment);

    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_COMMENT_ERROR);
    }
    return true;
  }

  @Transactional
  public boolean cancelPickComment(UUID commentId, Accounts account) {
    Comments comment = commentDAO.findByCommentId(commentId);
    Channels channels = comment.getVideos().getChannelId();
    Optional<Channels> loginUserChannel = channelRepository.findByChannelToAccountId(
        account.getAccountId());

    // 댓글이 존재하지 않는다면
    if (comment == null) {
      throw new CustomApiException(ErrorCode.No_EXIST_COMMENT);
    }

    // channel 주인이 아니라면.
    if (loginUserChannel.isEmpty() || !Objects.equals(channels, loginUserChannel.get())) {
      throw new AuthorizationException();
    }

    // 댓글 고정 취소 로직 추가
    try {
      if (comment.getIsPicked()) {
        // 현재 요청으로 받은 댓글의 고정을 취소
        comment.setIsPicked(false);
        commentDAO.saveParentComment(comment);
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_COMMENT_ERROR);
    }
    return true;
  }
}
