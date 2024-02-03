package com.bipa4.back_bipatv.dao;

import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dto.comment.ChildCommentResponse;
import com.bipa4.back_bipatv.dto.comment.CommentResponse;
import com.bipa4.back_bipatv.entity.Comments;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.exception.NoContentException;
import com.bipa4.back_bipatv.repository.CommentRepository;
import com.github.f4b6a3.ulid.Ulid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentDAO {

  private final CommentRepository commentRepository;


  public List<CommentResponse> findParentComments(String videoId) {
    List<CommentResponse> list;

    try {
      list = commentRepository.findParentComments(videoId);
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
    }
    return list;
  }

  public List<CommentResponse> findOldParentComments(String videoId) {
    List<CommentResponse> list;

    try {
      list = commentRepository.findOldParentComments(videoId);
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
    }
    return list;
  }

  public List<CommentResponse> findPopularityParentComments(String videoId) {
    List<CommentResponse> list;

    try {
      list = commentRepository.findPopularityParentComments(videoId);
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
    }
    return list;
  }


  public List<ChildCommentResponse> findChildComments(String videoId, int groupIndex) {
    List<ChildCommentResponse> list;

    try {
      list = commentRepository.findChildComments(videoId, groupIndex);
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
    }
    return list;
  }

  public boolean saveParentComment(Comments comment) {
    try {
      commentRepository.save(comment);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INSERT_ERROR);
    }
    return true;
  }

  public boolean saveChildComment(Comments comment) {
    try {
      commentRepository.save(comment);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INSERT_ERROR);
    }
    return true;
  }


  public Comments findByCommentId(UUID commentId) {
    return commentRepository.findById(commentId).orElse(null);
  }


  public boolean deleteParentComment(String videoId, int groupIndex) {
    int result;
    try {
      result = commentRepository.deleteParentComment(videoId, groupIndex);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.DELETE_ERROR);
    }
    return result > 0 ? true : false;
  }

  public boolean deleteChildComment(UUID commentId) {
    try {
      commentRepository.deleteById(commentId);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.DELETE_ERROR);
    }
    return true;
  }

  public Integer findMaxGroupIndex(String videoId) {

    if (commentRepository.findMaxGroupIndex(videoId) == null) {
      return 0;
    }
    return commentRepository.findMaxGroupIndex(videoId);
  }

  public Comments findPickedParentComment(String videoId) {
    return commentRepository.findPickedParentComment(videoId);
  }





}




