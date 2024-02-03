package com.bipa4.back_bipatv.repository;

import static org.aspectj.runtime.internal.Conversions.intValue;

import com.amazonaws.services.s3.AmazonS3;
import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dataType.HandleCode;
import com.bipa4.back_bipatv.dto.video.GetCategoryNameRequestDto;
import com.bipa4.back_bipatv.dto.video.GetDetailResponseDto;
import com.bipa4.back_bipatv.dto.video.GetSearchVideoINChannelDTO;
import com.bipa4.back_bipatv.dto.video.GetVideoResponseDto;
import com.bipa4.back_bipatv.dto.video.PostUploadRequestDto;
import com.bipa4.back_bipatv.dto.video.PutUpdateRequestDto;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.entity.Channels;
import com.bipa4.back_bipatv.entity.Favorite;
import com.bipa4.back_bipatv.entity.FavoritePK;
import com.bipa4.back_bipatv.entity.QCategoryName;
import com.bipa4.back_bipatv.entity.QCategorys;
import com.bipa4.back_bipatv.entity.QChannels;
import com.bipa4.back_bipatv.entity.QFavorite;
import com.bipa4.back_bipatv.entity.QRecommend;
import com.bipa4.back_bipatv.entity.QVideos;
import com.bipa4.back_bipatv.entity.QViewLog;
import com.bipa4.back_bipatv.entity.Videos;
import com.bipa4.back_bipatv.exception.AuthorizationException;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.exception.NoContentException;
import com.github.f4b6a3.ulid.Ulid;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Repository
public class VideoRepositoryImpl implements VideoRepositoryCustom {

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private final JPAQueryFactory jpaQueryFactory;
  private final EntityManager entityManager;
  private final AmazonS3 amazonS3;

  //----------------------------------------------VIDEO---------------------------------------------

  // 전체보기 (무한 스크롤)
  @Override
  public List<GetVideoResponseDto> getAllVideos(String page, int pageSize) {
    List<GetVideoResponseDto> responseDtos;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;

    if (page == null) {
      return new ArrayList<>();
    }

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId, qVideos.thumbnail,
                  qVideos.title.as("videoTitle"), qVideos.createAt, qVideos.readCnt.as("readCount"),
                  qVideos.videoId)).from(qVideos).leftJoin(qVideos.channelId, qChannels).where(
              qVideos.videoId.loe(page).and(qVideos.privateType.eq(false))
                  .and(qVideos.channelId.privateType.eq(false))).orderBy(qVideos.videoId.desc())
          .limit(pageSize).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_ERROR);
    }
    return responseDtos;
  }


  // Default UUID (무한 스크롤 시작점 찾기)
  @Override
  public String lastUUID() {
    String defaultUlid;

    QVideos qVideos = QVideos.videos;

    try {
      defaultUlid = jpaQueryFactory.select(qVideos.videoId)
          .from(qVideos)
          .where(qVideos.privateType.eq(false).and(qVideos.channelId.privateType.eq(false)))
          .orderBy(qVideos.videoId.desc()).limit(1).fetchFirst();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }

    return defaultUlid;
  }


  // 다음 페이지의 UUID 찾기
  @Override
  public String getNextUUID(String ulid) {
    QVideos qVideos = QVideos.videos;
    String nextUlid = null;

    try {
      nextUlid = jpaQueryFactory.select(qVideos.videoId).from(qVideos).where(
              qVideos.videoId.lt(ulid).and(qVideos.privateType.eq(false))
                  .and(qVideos.channelId.privateType.eq(false))).orderBy(qVideos.videoId.desc())
          .limit(1).fetchOne();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }

    return nextUlid;
  }


  // 카테고리별 전체보기
  @Override
  public List<GetVideoResponseDto> findByCategory(UUID category, String page, int pageSize) {
    List<GetVideoResponseDto> responseDto;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QCategorys qCategorys = QCategorys.categorys;
    QCategoryName qCategoryName = QCategoryName.categoryName;

    if (page == null) {
      return new ArrayList<>();
    }

    try {
      responseDto = jpaQueryFactory.select(
              Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId, qVideos.thumbnail,
                  qVideos.title.as("videoTitle"), qVideos.createAt, qVideos.readCnt.as("readCount"),
                  qVideos.videoId)).from(qCategorys).leftJoin(qCategorys.videoId, qVideos)
          .leftJoin(qVideos.channelId, qChannels).leftJoin(qCategorys.categoryNameId, qCategoryName)
          .where(qCategoryName.categoryNameId.eq(category).and(qVideos.videoId.loe(page))
              .and(qVideos.privateType.eq(false).and(qChannels.privateType.eq(false))))
          .orderBy(qVideos.videoId.desc()).limit(pageSize).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException(HandleCode.NO_CONTENT);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_ERROR);
    }
    return responseDto;
  }

  // 카테고리 Default UUID (무한 스크롤 시작점 찾기)
  @Override
  public String lastCategoryUUID(UUID category) {
    String defaultUlid = null;

    QVideos qVideos = QVideos.videos;
    QCategorys qCategorys = QCategorys.categorys;
    QCategoryName qCategoryName = QCategoryName.categoryName;

    try {
      defaultUlid = jpaQueryFactory.select(qVideos.videoId).from(qCategorys)
          .leftJoin(qCategorys.videoId, qVideos).leftJoin(qCategorys.categoryNameId, qCategoryName)
          .where(qCategoryName.categoryNameId.eq(category).and(qVideos.privateType.eq(false))
              .and(qVideos.channelId.privateType.eq(false))).orderBy(qVideos.videoId.desc())
          .limit(1).fetchOne();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }

    return defaultUlid;
  }

  // 카테고리 다음 페이지의 UUID 찾기
  @Override
  public String getNextCategoryUUID(String ulid, UUID category) {
    String nextUlid = null;

    QVideos qVideos = QVideos.videos;
    QCategorys qCategorys = QCategorys.categorys;
    QCategoryName qCategoryName = QCategoryName.categoryName;

    try {
      nextUlid = jpaQueryFactory.select(qVideos.videoId).from(qCategorys)
          .leftJoin(qCategorys.videoId, qVideos).leftJoin(qCategorys.categoryNameId, qCategoryName)
          .where(qCategoryName.categoryNameId.eq(category).and(qVideos.videoId.lt(ulid))
              .and(qVideos.privateType.eq(false)).and(qVideos.channelId.privateType.eq(false)))
          .orderBy(qVideos.videoId.desc()).limit(1).fetchOne();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }

    return nextUlid;
  }

  // 카테고리 이름 리스트
  @Override
  public List<GetCategoryNameRequestDto> getCategoryNames() {
    List<GetCategoryNameRequestDto> responseDtos = null;

    QCategoryName qCategoryName = QCategoryName.categoryName;

    try {
      responseDtos = jpaQueryFactory.select(
          Projections.bean(GetCategoryNameRequestDto.class, qCategoryName.categoryNameId,
              qCategoryName.name.as("categoryName"))).from(qCategoryName).fetch();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CATEGORY_ERROR);
    }

    return responseDtos;
  }


  // 조회수 급상승 TOP 10 + 디비 1시간 전 정보 저장
  @Override
  public List<GetVideoResponseDto> findByViews() {
    List<GetVideoResponseDto> responseDtos = null;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QViewLog qViewLog = QViewLog.viewLog;
    QFavorite qFavorite = QFavorite.favorite;

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId, qVideos.thumbnail,
                  qVideos.title.as("videoTitle"), qVideos.createAt, qVideos.readCnt.as("readCount"),
                  qVideos.videoId)).from(qViewLog).leftJoin(qViewLog.videoId, qVideos)
          .leftJoin(qVideos.channelId, qChannels).leftJoin(qFavorite)
          .on(qFavorite.favoritePK.videos.videoId.eq(qVideos.videoId))
          .where(qVideos.privateType.eq(false).and(qVideos.channelId.privateType.eq(false)))
          .orderBy(qVideos.readCnt.subtract(qViewLog.viewCnt).multiply(10)
              .add(qFavorite.favoritePK.videos.videoId.count().coalesce(0l)).desc())
          .groupBy(qVideos.videoId).limit(10).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_TOP10_ERROR);
    }
    return responseDtos;
  }


  @Override
  public boolean updateViews() {
    int result;

    try {
      result = entityManager.createNativeQuery(
              "update view_log vl join videos v on vl.video_id = v.video_id set view_cnt = read_cnt where v.video_id = vl.video_id")
          .executeUpdate();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_ERROR);
    }

    return result > 0 ? true : false;
  }


  // 상세보기
  @Override
  public GetDetailResponseDto getDetail(String id, JDBCDataModel dataModel) {
    GetDetailResponseDto responseDto = null;
    List<GetVideoResponseDto> recommendedVideos = new ArrayList<>();

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QRecommend qRecommend = QRecommend.recommend;
    QFavorite qFavorite = QFavorite.favorite;
    QCategorys qCategorys = QCategorys.categorys;

    try {
      responseDto = jpaQueryFactory.select(
              Projections.bean(GetDetailResponseDto.class, qChannels.channelName,
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId,
                  qVideos.videoUrl,
                  qVideos.title.as("videoTitle"), qVideos.content, qVideos.createAt,
                  qVideos.privateType,
                  qVideos.readCnt.as("readCount"), qVideos.videoId, qVideos.thumbnail,
                  qVideos.updateAt)).from(qVideos)
          .leftJoin(qVideos.channelId, qChannels).where(
              qVideos.videoId.eq(id)).fetchOne();
    } catch (AuthorizationException e) {
      throw new AuthorizationException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_DETAIL_ERROR);
    }

    if (responseDto == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    // 영상 처리
    RestTemplate restTemplate = new RestTemplate();
    try {
      URI uri = UriComponentsBuilder
          .fromUriString(responseDto.getVideoUrl())
          .encode()
          .build()
          .toUri();

      ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
    } catch (Exception e) {
      responseDto.setVideoUrl(
          "https://du30t7lolw1uk.cloudfront.net/static/donuts_-_32649+(360p).mp4");
    }

    // 추천 영상 리스트 추출
    long modelData = jpaQueryFactory.select(qRecommend.recommendId.count()).from(qRecommend)
        .where(qRecommend.videoUUIDId.videoId.eq(id)).fetchOne();
    if (modelData >= 5) { // 데이터가 충분하다면
      try {
        List<Videos> itemIDs = new ArrayList<>();
        long videoNumberId = ulidToLong(id);

        ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel,
            itemSimilarity);

        List<RecommendedItem> recommendations = recommender.mostSimilarItems(videoNumberId, 10);

        for (RecommendedItem recommendation : recommendations) {
          GetVideoResponseDto dto = new GetVideoResponseDto();
          Videos recommendVideo = jpaQueryFactory.select(qRecommend.videoUUIDId).from(qVideos)
              .leftJoin(qRecommend).on(qRecommend.videoUUIDId.eq(qVideos)).leftJoin(qChannels)
              .on(qChannels.channelId.eq(qVideos.channelId.channelId))
              .where(qRecommend.videoId.eq(recommendation.getItemID())).fetchOne();

          if (recommendVideo.getPrivateType() == false
              && recommendVideo.getChannelId().getPrivateType() == false) {
            dto.setVideoId(recommendVideo.getVideoId());
            dto.setVideoTitle(recommendVideo.getTitle());
            dto.setThumbnail(recommendVideo.getThumbnail());
            dto.setPrivateType(recommendVideo.getPrivateType());
            dto.setChannelId(recommendVideo.getChannelId().getChannelId());
            dto.setChannelName(recommendVideo.getChannelId().getChannelName());
            dto.setReadCount(recommendVideo.getReadCnt());
            dto.setCreateAt(recommendVideo.getCreateAt());
            dto.setChannelProfileUrl(recommendVideo.getChannelId().getProfileUrl());
            recommendedVideos.add(dto);
          }
        }
      } catch (NullPointerException e) {
        throw new NoContentException();
      } catch (Exception e) {
        throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
      }
    } else { // 충분하지 않다면
      try {
        recommendedVideos = jpaQueryFactory.select(
                Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                    qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId,
                    qVideos.thumbnail, qVideos.title.as("videoTitle"), qVideos.createAt,
                    qVideos.readCnt.as("readCount"), qVideos.videoId)).from(qVideos)
            .leftJoin(qVideos.channelId, qChannels).where(
                qVideos.channelId.channelId.eq(responseDto.getChannelId())
                    .and(qVideos.videoId.ne(responseDto.getVideoId())))
            .orderBy(qVideos.readCnt.desc()).limit(10).fetch();
      } catch (NullPointerException e) {
        throw new NoContentException();
      } catch (Exception e) {
        throw new CustomApiException(ErrorCode.READ_RECOMMEND_ERROR);
      }
    }

    responseDto.setRecommendedList(recommendedVideos);

    // 영상의 좋아요 총 개수
    try {
      long favoriteCnt = jpaQueryFactory.select(qFavorite.count()).from(qFavorite)
          .where(qFavorite.favoritePK.videos.videoId.eq(responseDto.getVideoId())).fetchFirst();
      responseDto.setLikeCount(favoriteCnt);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LIKE_ERROR);
    }

    // 영상의 카테고리 저장
    try {
      List<UUID> uuid = jpaQueryFactory.select(qCategorys.categoryNameId.categoryNameId)
          .from(qCategorys).where(qCategorys.videoId.videoId.eq(responseDto.getVideoId())).fetch();
      responseDto.setCategoryId(uuid);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CATEGORY_ERROR);
    }

    return responseDto;
  }

  // 영상 삭제
  @Override
  public boolean remove(String id, Accounts account) {
    QChannels qChannels = QChannels.channels;

    Videos video = entityManager.find(Videos.class, id);

    // 요청한 영상이 존재하지 않는 경우.
    if (video == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    Channels requestChannel = accountToChannel(account);

    // 요청한 유저의 채널과 video의 채널이 다를 경우.
    if (!video.getChannelId().getChannelId().equals(requestChannel.getChannelId())) {
      throw new AuthorizationException();
    }

    // S3 삭제
    try {
      String videoName = video.getVideoUrl().replace("https://du30t7lolw1uk.cloudfront.net/", "");
      String thumbnailName = video.getThumbnail()
          .replace("https://du30t7lolw1uk.cloudfront.net/", "");
      amazonS3.deleteObject(bucketName, (videoName).replace(File.separatorChar, '/'));
      amazonS3.deleteObject(bucketName, (thumbnailName).replace(File.separatorChar, '/'));
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.S3_DELETE_ERROR);
    }

    // 비디오 삭제
    entityManager.remove(video);
    return true;
  }

  // 영상 수정
  @Override
  public boolean update(String id, PutUpdateRequestDto videoResponseDto, Accounts account) {
    QChannels qChannels = QChannels.channels;

    Videos video = entityManager.find(Videos.class, id);

    // 요청한 영상이 존재하지 않는 경우.
    if (video == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    Channels requestChannel = accountToChannel(account);

    // 요청한 유저의 채널과 video의 채널이 다를 경우.
    if (!video.getChannelId().getChannelId().equals(requestChannel.getChannelId())) {
      throw new AuthorizationException();
    }

    // S3 삭제
    if (!video.getVideoUrl().equals(videoResponseDto.getVideoUrl())) {
      deleteS3(video.getVideoUrl());
    }
    if (!video.getThumbnail().equals(videoResponseDto.getThumbnailUrl())) {
      deleteS3(video.getThumbnail());
    }

    LocalDateTime now = LocalDateTime.now();

    // 영상 업데이트
    try {
      video.setContent(videoResponseDto.getContent());
      video.setUpdateAt(Timestamp.valueOf(now));
      video.setPrivateType(videoResponseDto.isPrivateType());
      video.setThumbnail(videoResponseDto.getThumbnailUrl());
      video.setTitle(videoResponseDto.getTitle());
      video.setVideoUrl(videoResponseDto.getVideoUrl());
      video.setContent(videoResponseDto.getContent());
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_ERROR);
    }
    return true;
  }

  // 영상 본인 글인지 확인하기
  @Override
  public boolean checkOwner(Accounts account, String videoId) {
    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;

    if (account == null) {
      return false;
    }

    Videos requestVideo = jpaQueryFactory.selectFrom(qVideos).where(qVideos.videoId.eq(videoId))
        .fetchOne();

    // 해당 영상이 존재하지 않는다면.
    if (requestVideo == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    Channels channel = accountToChannel(account);

    if (channel.getChannelId().equals(requestVideo.getChannelId().getChannelId())) {
      return true;
    }
    return false;
  }


  // 영상 업로드
  @Override
  public boolean insert(PostUploadRequestDto videoResponseDto, Accounts account, UUID uuid,
      String ulid) {
    QChannels qChannels = QChannels.channels;

    Channels channel = accountToChannel(account);

    // video 테이블 create.
    int videoFlag = entityManager.createNativeQuery(
            "INSERT INTO videos (video_url, thumbnail, title, content, private_type, create_at, channel_id, read_cnt, video_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .setParameter(1, videoResponseDto.getVideoUrl())
        .setParameter(2, videoResponseDto.getThumbnailUrl())
        .setParameter(3, videoResponseDto.getTitle())
        .setParameter(4, videoResponseDto.getContent())
        .setParameter(5, videoResponseDto.getPrivateType())
        .setParameter(6, new Timestamp(System.currentTimeMillis()))
        .setParameter(7, channel)
        .setParameter(8, 0)
        .setParameter(9, ulid).executeUpdate();

    if (videoFlag == 0) {
      throw new CustomApiException(ErrorCode.UPLOAD_ERROR);
    }

    // view log 테이블 create.
    int viewLogFlag = entityManager.createNativeQuery(
            "INSERT INTO view_log (video_id, view_cnt) VALUES (?, ?);")
        .setParameter(1, ulid)
        .setParameter(2, 0).executeUpdate();

    if (viewLogFlag == 0) {
      throw new CustomApiException(ErrorCode.VIEW_LOG_CREATE_ERROR);
    }

    // category 테이블 create.
    int categoryFlag;
    for (int i = 0; i < videoResponseDto.getCategory().size(); i++) {
      categoryFlag = entityManager.createNativeQuery(
              "INSERT INTO categorys (video_id, category_name_id) VALUES (?, ?)")
          .setParameter(1, ulid)
          .setParameter(2, UUID.fromString(videoResponseDto.getCategory().get(i)))
          .executeUpdate();

      if (categoryFlag == 0) {
        throw new CustomApiException(ErrorCode.CATEGORY_CREATE_ERROR);
      }
    }

    // recommend 테이블 create.
    long test = ulidToLong(ulid);
    int viewRecommendFlag = entityManager.createNativeQuery(
            "INSERT INTO recommend (account_id, video_id, rating, video_ulid_id) VALUES (?, ?, ?, ?);")
        .setParameter(1, uuidToLong(account.getAccountId())).setParameter(2, ulidToLong(ulid))
        .setParameter(3, 1).setParameter(4, ulid).executeUpdate();

    if (viewRecommendFlag == 0) {
      throw new CustomApiException(ErrorCode.VIEW_RECOMMEND_CREATE_ERROR);
    }
    return true;
  }


  // 조회수 상승
  @Override
  public boolean plusViews(String videoId) {
    Videos video = entityManager.find(Videos.class, videoId);

    if (video == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    try {
      video.setReadCnt(video.getReadCnt() + 1);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_VIEW_ERROR);
    }

    return true;
  }

  // 추천 영상을 위한 조회수 상승
  @Override
  public boolean plusViewsCount(String videoId, Accounts account) {
    try {
      int viewLogFlag = entityManager.createNativeQuery(
              "UPDATE recommend SET rating = rating+1\n" + "WHERE video_ulid_id=?\n"
                  + "AND account_id=?;").setParameter(1, videoId)
          .setParameter(2, uuidToLong(account.getAccountId())).executeUpdate();

      if (viewLogFlag == 0) {
        int viewRecommendFlag = entityManager.createNativeQuery(
                "INSERT INTO recommend (account_id, video_id, video_ulid_id, rating) VALUES (?, ?, ?, ?);")
            .setParameter(1, uuidToLong(account.getAccountId()))
            .setParameter(2, ulidToLong(videoId)).setParameter(3, videoId).setParameter(4, 1)
            .executeUpdate();

        if (viewRecommendFlag == 0) {
          throw new CustomApiException(ErrorCode.VIEW_RECOMMEND_CREATE_ERROR);
        }
        return true;
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UPDATE_RECOMMEND_ERROR);
    }

    return true;
  }


  // 좋아요 버튼 눌렀는지 여부
  @Override
  public boolean getFavorite(String videoId, Accounts account) {
    long result;

    QFavorite qFavorite = QFavorite.favorite;

    if (account == null) {
      return false;
    }

    try {
      result = jpaQueryFactory.select(qFavorite.count()).from(qFavorite).where(
          qFavorite.favoritePK.videos.videoId.eq(videoId)
              .and(qFavorite.favoritePK.accounts.eq(account))).fetchFirst();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LIKE_ERROR);
    }

    return result > 0 ? true : false;
  }

  // 좋아요
  @Override
  public boolean plusLike(String videoId, Accounts account) {
    try {
      entityManager.createNativeQuery("INSERT INTO favorite VALUES (?, ?)")
          .setParameter(1, account.getAccountId()).setParameter(2, videoId).executeUpdate();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.LIKE_ERROR);
    }
    return true;
  }


  // 좋아요 취소
  @Override
  public boolean minusLike(String videoId, Accounts account) {
    Videos video = entityManager.find(Videos.class, videoId);

    // 요청한 영상이 존재하지 않는 경우.
    if (video == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }

    FavoritePK favoritePK = new FavoritePK();
    favoritePK.setVideos(video);
    favoritePK.setAccounts(account);

    Favorite favorite = entityManager.find(Favorite.class, favoritePK);

    // 좋아요를 누를지 않았덛라면.
    if (favorite == null) {
      throw new CustomApiException(ErrorCode.CANNOT_UNLIKE_ERROR);
    }

    try {
      entityManager.remove(favorite);
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.UNLIKE_ERROR);
    }
    return true;
  }

  //Account to Channel
  private Channels accountToChannel(Accounts account) {
    QChannels qChannels = QChannels.channels;

    return jpaQueryFactory.selectFrom(qChannels).leftJoin(qChannels.accounts).fetchJoin()
        .where(qChannels.accounts.eq(account)).fetchOne();
  }

  //Delete S3 File
  private void deleteS3(String videoUrl) {
    String videoName = videoUrl.replace("https://du30t7lolw1uk.cloudfront.net/", "");
    try {
      amazonS3.deleteObject(bucketName, (videoName).replace(File.separatorChar, '/'));
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.NO_EXIST_VIDEO);
    }
  }

  //UUID to long
  public long uuidToLong(UUID uuid) {
    return uuid.getMostSignificantBits() + uuid.getLeastSignificantBits();
  }

  //Ulid to long
  public static long ulidToLong(String ulidStr) {
    Ulid ulid = Ulid.from(ulidStr);
    long upperBits = ulid.getMostSignificantBits();
    long lowerBits = ulid.getLeastSignificantBits() & 0xFFFFFFFFL;
    return (upperBits << 32) | lowerBits;
  }

  //----------------------------------------------CHANNEL----------------------------------------


  @Override
  public List<GetVideoResponseDto> getVideosInChannel(UUID channelId, String page, int pageSize) {
    List<GetVideoResponseDto> responseDtos = null;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;

    if (page == null) {
      return new ArrayList<>();
    }

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId, qVideos.thumbnail,
                  qVideos.title.as("videoTitle"), qVideos.createAt, qVideos.readCnt.as("readCount"),
                  qVideos.videoId, qVideos.privateType)).from(qVideos)
          .leftJoin(qVideos.channelId, qChannels).where(
              qVideos.channelId.channelId.eq(channelId).and(qVideos.videoId.loe(page))
                  .and(qVideos.privateType.eq(false))).orderBy(qVideos.videoId.desc())
          .limit(pageSize).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_ERROR);
    }

    return responseDtos;
  }

  @Override
  public String lastUUIDInChannel(UUID channelId) {
    String lastUlid = null;

    QVideos qVideos = QVideos.videos;

    try {
      lastUlid = jpaQueryFactory.select(qVideos.videoId).from(qVideos)
          .where(qVideos.channelId.channelId.eq(channelId).and(qVideos.privateType.eq(false)))
          .orderBy(qVideos.videoId.desc()).limit(1).fetchOne();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }
    return lastUlid;
  }

  @Override
  public String lastUUIDInMyChannel(UUID channelId) {
    String lastUlid = null;
    QVideos qVideos = QVideos.videos;

    try {
      lastUlid = jpaQueryFactory.select(qVideos.videoId).from(qVideos)
          .where(qVideos.channelId.channelId.eq(channelId)).orderBy(qVideos.videoId.desc()).limit(1)
          .fetchOne();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }

    return lastUlid;
  }

  @Override
  public Integer lastUUIDSearchVideoInMyChannel(UUID channelId, String searchQuery) {
    List<Integer> uuid = new ArrayList<>();

    try {
      uuid = entityManager.createNativeQuery(
              "SELECT ranking\n" + "FROM (\n" + "    SELECT ROW_NUMBER() OVER () AS ranking\n"
                  + "    FROM videos\n" + "    WHERE videos.channel_id = ?\n"
                  + "    AND MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                  + "    ORDER BY ranking ASC \n" + "    LIMIT 1\n" + ") AS ranked_results;\n")
          .setParameter(1, channelId).setParameter(2, searchQuery).getResultList();

      if (!uuid.isEmpty()) {
        return intValue(uuid.get(0));
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }
    return null;
  }

  @Override
  public Integer lastUUIDSearchVideoInChannel(UUID channelId, String searchQuery) {
    List<Integer> uuid = new ArrayList<>();
    try {
      uuid = entityManager.createNativeQuery(
              "SELECT ranking\n" + "FROM (\n" + "    SELECT ROW_NUMBER() OVER () AS ranking\n"
                  + "    FROM videos\n" + "    WHERE videos.channel_id = ?\n"
                  + "    AND MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                  + "    AND videos.private_type = false\n" + "    ORDER BY ranking ASC \n"
                  + "    LIMIT 1\n" + ") AS ranked_results;\n ").setParameter(1, channelId)
          .setParameter(2, searchQuery).getResultList();

      if (!uuid.isEmpty()) {
        return intValue(uuid.get(0));
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }
    return null;
  }

  @Override
  public List<GetVideoResponseDto> getVideosInMyChannel(UUID channelId, String uuid, int pageSize) {
    List<GetVideoResponseDto> responseDtos;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;

    if (uuid == null) {
      return new ArrayList<>();
    }

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetVideoResponseDto.class, qChannels.channelName.as("channelName"),
                  qChannels.profileUrl.as("channelProfileUrl"), qChannels.channelId, qVideos.thumbnail,
                  qVideos.title.as("videoTitle"), qVideos.createAt, qVideos.readCnt.as("readCount"),
                  qVideos.videoId, qVideos.privateType)).from(qVideos)
          .leftJoin(qVideos.channelId, qChannels)
          .where(qVideos.channelId.channelId.eq(channelId).and(qVideos.videoId.loe(uuid)))
          .orderBy(qVideos.videoId.desc()).limit(pageSize).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_ERROR);
    }

    return responseDtos;
  }

  @Override
  public List<GetSearchVideoINChannelDTO> getSearchVideoInMyChannel(UUID channelId,
      Integer nextRank, int pageSize, String searchQuery) {
    if (nextRank == null) {
      nextRank = 1;
    }
    List<Object[]> resultList = entityManager.createNativeQuery(
            "SELECT ranking, videoId, videoTitle, channelId, channelName, channelProfileUrl, thumbnail, createAt, readCount\n"
                + "FROM (\n"
                + "SELECT ROW_NUMBER() OVER () AS ranking, videos.video_id as videoId, videos.title as videoTitle, BIN_TO_UUID(channels.channel_id) as channelId, videos.read_cnt as readCount, videos.create_at as createAt, videos.thumbnail as thumbnail, channels.profile_url as channelProfileUrl, channels.name as channelName\n"
                + "FROM videos \n" + "join channels \n" + "on videos.channel_id = channels.channel_id\n"
                + "WHERE videos.channel_id = ?\n"
                + "and MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                + ")as ranked\n" + "where ranking >= ?\n" + "order by ranking asc\n" + "limit ?;")
        .setParameter(1, channelId).setParameter(2, searchQuery).setParameter(3, nextRank)
        .setParameter(4, pageSize).getResultList();

    List<GetSearchVideoINChannelDTO> searchList = new ArrayList<>();
    for (Object[] row : resultList) {
      GetSearchVideoINChannelDTO dto = new GetSearchVideoINChannelDTO();//
      dto.setRanking(((BigInteger) row[0]).intValue());
      String ulid = (String) row[1];
      dto.setVideoId(ulid);

      dto.setVideoTitle((String) row[2]);
      dto.setChannelId(UUID.fromString((String) row[3]));

      dto.setChannelName((String) row[4]);
      dto.setChannelProfileUrl((String) row[5]);
      dto.setThumbnail((String) row[6]);
      dto.setCreateAt((Timestamp) row[7]);
      dto.setReadCount((int) row[8]);
      // 나머지 필드 설정
      searchList.add(dto);

    }
    return searchList;
  }

  @Override
  public List<GetSearchVideoINChannelDTO> getSearchVideoInChannel(UUID channelId, Integer nextRank,
      int pageSize, String searchQuery) {
    List<GetSearchVideoINChannelDTO> searchList = new ArrayList<>();
    List<Object[]> resultList = new ArrayList<>();
    if (nextRank == null) {
      nextRank = 1;
    }
    String sql =
        "SELECT ranking, videoId, videoTitle, BIN_TO_UUID(channelId) as channelId, channelName, channelProfileUrl, thumbnail, createAt, readCount\n"
        + "FROM (\n"
        + "SELECT ROW_NUMBER() OVER () AS ranking, videos.video_id as videoId, videos.title as videoTitle, channels.channel_id as channelId, videos.read_cnt as readCount, videos.create_at as createAt, videos.thumbnail as thumbnail, channels.profile_url as channelProfileUrl, channels.name as channelName\n"
        + "FROM videos \n" + "join channels \n" + "on videos.channel_id = channels.channel_id\n"
        + "WHERE videos.channel_id = ?\n"
        + "and MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
        + "AND videos.private_type = false\n" + ")as ranked\n" + "where ranking >= ?\n"
        + "order by ranking asc\n" + "limit ?;";

        try {
          resultList = entityManager.createNativeQuery(sql).setParameter(1, channelId)
              .setParameter(2, searchQuery).setParameter(3, nextRank).setParameter(4, pageSize)
              .getResultList();
        }catch (NullPointerException e) {
          throw new NoContentException();
        } catch (Exception e) {
          throw new CustomApiException(ErrorCode.READ_ERROR);
        }

    for (Object[] row : resultList) {
      GetSearchVideoINChannelDTO dto = new GetSearchVideoINChannelDTO();//
      dto.setRanking(((BigInteger) row[0]).intValue());
      String ulid = (String) row[1];
      dto.setVideoId(ulid);

      dto.setVideoTitle((String) row[2]);
      dto.setChannelId(UUID.fromString((String) row[3]));

      dto.setChannelName((String) row[4]);
      dto.setChannelProfileUrl((String) row[5]);
      dto.setThumbnail((String) row[6]);
      dto.setCreateAt((Timestamp) row[7]);
      dto.setReadCount((int) row[8]);
      // 나머지 필드 설정
      searchList.add(dto);
    }
    return searchList;
  }
}
