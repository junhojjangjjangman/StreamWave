package com.bipa4.back_bipatv.repository;

import static com.querydsl.core.types.dsl.Expressions.asNumber;
import static org.aspectj.runtime.internal.Conversions.intValue;

import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dto.channel.GetChannelDTO;
import com.bipa4.back_bipatv.dto.channel.GetChannelTop5DTO;
import com.bipa4.back_bipatv.dto.channel.GetSearchChannelDTO;
import com.bipa4.back_bipatv.dto.channel.GetSumCommentNumGroupChannelDTO;
import com.bipa4.back_bipatv.dto.channel.GetSumVideoViewNumGroupChannelDTO;
import com.bipa4.back_bipatv.dto.channel.SelectChannelDTO;
import com.bipa4.back_bipatv.entity.QChannels;
import com.bipa4.back_bipatv.entity.QComments;
import com.bipa4.back_bipatv.entity.QVideos;
import com.bipa4.back_bipatv.entity.QViewLog;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.exception.NoContentException;
import com.bipa4.back_bipatv.security.SecurityService;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ChannelRepositoryImpl implements ChannelRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;
  private final SecurityService securityService;
  private final EntityManager entityManager;

  @Override
  public List<GetChannelDTO> getNotPrivateChannel(UUID page, int pageSize) {
    List<GetChannelDTO> responseDtos;

    QChannels qChannels = QChannels.channels;
    if (page == null) {
      return new ArrayList<>();
    }
    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetChannelDTO.class, qChannels.channelId, qChannels.channelName,
                  qChannels.profileUrl, qChannels.content, qChannels.privateType)).from(qChannels)
          .where(qChannels.privateType.eq(false).and(qChannels.accounts.deleteAt.isNull())
              .and(qChannels.channelId.loe(page))).orderBy(qChannels.channelId.desc())
          .limit(pageSize).fetch();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.NO_EXIST_CHANNEL);
    }

    return responseDtos;
  }

  public List<GetChannelTop5DTO> findTop5Channels() {
    List<GetChannelTop5DTO> responseDtos;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QViewLog qViewLog = QViewLog.viewLog;

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetChannelTop5DTO.class, qChannels.channelId, qChannels.channelName,
                  qChannels.profileUrl, qChannels.content,
                  asNumber(qVideos.readCnt.subtract(qViewLog.viewCnt)).as("timeLimitSumCnt")))
          .from(qViewLog).leftJoin(qViewLog.videoId, qVideos).leftJoin(qVideos.channelId, qChannels)
          .where(qChannels.privateType.eq(false).and(qChannels.accounts.deleteAt.isNull()))
          .groupBy(qChannels.channelId, qChannels.channelName, qChannels.profileUrl,
              qChannels.content)
          .orderBy(asNumber(qVideos.readCnt.subtract(qViewLog.viewCnt)).doubleValue().desc())
          .limit(5).fetch();

    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CHANNEL_TOP5_ERROR);
    }
    return responseDtos;
  }

  @Override
  public SelectChannelDTO selectChannel(UUID channelId) {
    SelectChannelDTO responseDto;

    QChannels qChannels = QChannels.channels;

    try {
      responseDto = jpaQueryFactory.select(
              Projections.bean(SelectChannelDTO.class, qChannels.channelId, qChannels.channelName,
                  qChannels.profileUrl, qChannels.content, qChannels.privateType)).from(qChannels)
          .where(qChannels.channelId.eq(channelId).and(qChannels.accounts.deleteAt.isNull()))
          .fetchOne();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CHANNEL_ERROR);
    }

    if (responseDto == null) {
      throw new CustomApiException(ErrorCode.NO_EXIST_CHANNEL);
    }

    return responseDto;
  }

  @Override
  public UUID lastUUID() {
    UUID lastUUID = null;

    QChannels qChannels = QChannels.channels;

    try {
      lastUUID = jpaQueryFactory.select(qChannels.channelId).from(qChannels)
          .where(qChannels.privateType.eq(false).and(qChannels.accounts.deleteAt.isNull()))
          .orderBy(qChannels.channelId.desc()).limit(1).fetchOne();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }

    return lastUUID;
  }

  @Override
  public UUID getChannelNextUUID(UUID uuid) {
    UUID nextUUID = null;

    QChannels qChannels = QChannels.channels;

    try {
      nextUUID = jpaQueryFactory.select(qChannels.channelId).from(qChannels)
          .where(qChannels.channelId.lt(uuid).and(qChannels.privateType.eq(false)))
          .orderBy(qChannels.channelId.desc()).limit(1).fetchOne();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }

    return nextUUID;
  }


  @Override
  public String getNextChannelVideoUUID(String videoId, UUID channelId, boolean flag) {
    String nextUlid;

    QVideos qVideos = QVideos.videos;

    try {
      if (flag) {
        nextUlid = jpaQueryFactory.select(qVideos.videoId).from(qVideos)
            .where(qVideos.videoId.lt(videoId).and(qVideos.channelId.channelId.eq(channelId)))
            .orderBy(qVideos.videoId.desc()).limit(1).fetchOne();
      } else {
        nextUlid = jpaQueryFactory.select(qVideos.videoId).from(qVideos).where(
                qVideos.videoId.lt(videoId).and(qVideos.channelId.channelId.eq(channelId))
                    .and(qVideos.privateType.eq(false))).orderBy(qVideos.videoId.desc()).limit(1)
            .fetchOne();
      }

    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }

    return nextUlid;
  }

  // 채널 검색
  @Override
  public Integer getSearchNextChannelVideoRank(Integer rank, UUID channelId, String searchQuery,
      int pageSize) {

    List<Integer> resultList = entityManager.createNativeQuery(
            "SELECT ranking\n" + "FROM (\n" + "SELECT ROW_NUMBER() OVER () AS ranking\n"
                + "FROM videos \n" + "join channels \n" + "on videos.channel_id = channels.channel_id\n"
                + "WHERE videos.channel_id = ? \n"
                + "and MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                + "AND videos.private_type = false\n" + ")as ranked\n" + "where ranking > ?\n"
                + "order by ranking asc\n" + "limit ?;").setParameter(1, channelId)
        .setParameter(2, searchQuery).setParameter(3, rank).setParameter(4, pageSize)
        .getResultList();
    Integer result = null;
    if (!resultList.isEmpty()) {
      return intValue(resultList.get(0));
    }
    return null;
  }

  @Override
  public Integer getSearchNextMyChannelVideoRank(Integer rank, UUID channelId, String searchQuery,
      int pageSize) {

    List<Integer> resultList = entityManager.createNativeQuery(
            "SELECT ranking\n" + "FROM (\n" + "SELECT ROW_NUMBER() OVER () AS ranking\n"
                + "FROM videos \n" + "join channels \n" + "on videos.channel_id = channels.channel_id\n"
                + "WHERE videos.channel_id = ? \n"
                + "and MATCH (videos.title, videos.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                + ")as ranked\n" + "where ranking > ?\n" + "order by ranking asc\n" + "limit ?;")
        .setParameter(1, channelId).setParameter(2, searchQuery).setParameter(3, rank)
        .setParameter(4, pageSize).getResultList();
    Integer result = null;
    if (!resultList.isEmpty()) {
      return intValue(resultList.get(0));
    }
    return null;
  }

  @Override
  public Integer lastUUIDSearchChannel(String searchQuery) {
    List<Integer> uuid = new ArrayList<>();

    try {
      uuid = entityManager.createNativeQuery(
              "SELECT ranking\n" + "FROM (\n" + "    SELECT ROW_NUMBER() OVER () AS ranking\n"
                  + "    FROM channels\n"
                  + "    WHERE MATCH (channels.name, channels.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
                  + "    ORDER BY ranking ASC\n" + "    LIMIT 1\n" + ") AS ranked_results;\n")
          .setParameter(1, searchQuery).getResultList();

      if (!uuid.isEmpty()) {
        return intValue(uuid.get(0));
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_LAST_UUID_ERRROR);
    }
    return null;
  }

  @Override
  public List<GetSearchChannelDTO> getSearchChannel(Integer page, int pageSize,
      String searchQuery) {
    List<GetSearchChannelDTO> searchList = new ArrayList<>();
    List<Object[]> resultList = new ArrayList<>();
    String sql;

    if (page == null) {
      page = 1;
    }

    sql =
        "select BIN_TO_UUID(channel_id) as channelId, name as channelName, content, private_type as privateType, profile_url as profileUrl, ROW_NUMBER() OVER () AS ranking\n"
            + "from channels \n"
            + "where MATCH (channels.name, channels.content) AGAINST ( ? IN NATURAL LANGUAGE MODE) \n"
            + "and channels.private_type = false \n" + "and channels.channel_id IN ( \n"
            + "  SELECT channel_id \n" + "  FROM ( \n"
            + "    SELECT channel_id, ROW_NUMBER() OVER () AS ranking\n" + "    FROM channels \n"
            + "    WHERE MATCH (channels.name, channels.content) AGAINST (? IN NATURAL LANGUAGE MODE)\n"
            + "  ) AS ranked\n" + "  WHERE ranking >= ?\n" + ")\n" + "order by ranking asc \n"
            + "LIMIT ?;";

    try {
      resultList = entityManager.createNativeQuery(sql).setParameter(1, searchQuery)
          .setParameter(2, searchQuery).setParameter(3, 1 + ((page - 1) * pageSize))
          .setParameter(4, pageSize).getResultList();
    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_ERROR);
    }

    try {
      for (Object[] row : resultList) {
        GetSearchChannelDTO dto = new GetSearchChannelDTO();//id, name, content, PT, url, ranking
        dto.setChannelId(UUID.fromString((String) row[0]));
        dto.setChannelName((String) row[1]);
        dto.setContent((String) row[2]);
        dto.setPrivateType((boolean) row[3]);
        dto.setProfileUrl((String) row[4]);
        dto.setRanking(((BigInteger) row[5]).intValue());
        searchList.add(dto);
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INSERT_DTO_ERROR);
    }
    return searchList;
  }

  @Override
  public Integer getNextChannelRank(String searchQuery, Integer ranking, int pageSize,
      Integer page) {
    List<Integer> list;

    if (page == null) {
      page = 1;
    }

    try {
      list = entityManager.createNativeQuery(
              "select ROW_NUMBER() OVER () AS ranking\n" + "from channels\n"
                  + "where MATCH (channels.name, channels.content) AGAINST ( ? IN NATURAL LANGUAGE MODE)\n"
                  + "and channels.private_type = false\n" + "and channels.channel_id IN (\n"
                  + "  SELECT channel_id\n" + "  FROM ( \n"
                  + "  SELECT channel_id, ROW_NUMBER() OVER () AS ranking\n" + "  FROM channels\n"
                  + "  WHERE MATCH (channels.name, channels.content) AGAINST ( ? IN NATURAL LANGUAGE MODE)\n"
                  + "  ) AS ranked\n" + "  WHERE ranking > ?\n" + ")\n" + "order by ranking asc\n"
                  + "LIMIT 1;\n").setParameter(1, searchQuery).setParameter(2, searchQuery)
          .setParameter(3, ((page - 1) * pageSize) + ranking).getResultList();

      if (!list.isEmpty()) {
        return intValue(list.get(0));
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_NEXT_UUID_ERRROR);
    }
    return null;
  }

  @Override
  public List<GetSumCommentNumGroupChannelDTO> getSumCommentNumGroupChannel() {
    List<GetSumCommentNumGroupChannelDTO> responseDtos;
    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QComments qComments = QComments.comments;

    try {
      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetSumCommentNumGroupChannelDTO.class, qChannels.channelId,
                  qComments.commentId.count().intValue().as("sumCommentNum"))).from(qChannels)
          .leftJoin(qVideos).on(qChannels.channelId.eq(qVideos.channelId.channelId))
          .leftJoin(qComments).on(qVideos.videoId.eq(qComments.videos.videoId))
          .where(qChannels.privateType.eq(false).and(qChannels.accounts.deleteAt.isNull()))
          .groupBy(qChannels.channelId).fetch();

    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CHANNEL_TOP5_ERROR);
    }
    return responseDtos;
  }

  @Override
  public List<GetSumVideoViewNumGroupChannelDTO> getSumVideoViewNumGroupChannel() {
    List<GetSumVideoViewNumGroupChannelDTO> responseDtos;

    QVideos qVideos = QVideos.videos;
    QChannels qChannels = QChannels.channels;
    QViewLog qViewLog = QViewLog.viewLog;
    QComments qComments = QComments.comments;

    try {

      responseDtos = jpaQueryFactory.select(
              Projections.bean(GetSumVideoViewNumGroupChannelDTO.class, qChannels.channelId,
                  qVideos.readCnt.subtract(qViewLog.viewCnt).multiply(10)
                      .add(qComments.commentId.count()).as("totalScore"))).from(qViewLog)
          .leftJoin(qVideos).on(qViewLog.videoId.videoId.eq(qVideos.videoId)).leftJoin(qChannels)
          .on(qChannels.channelId.eq(qVideos.channelId.channelId)).leftJoin(qComments)
          .on(qVideos.videoId.eq(qComments.videos.videoId))
          .where(qChannels.privateType.eq(false).and(qChannels.accounts.deleteAt.isNull()))
          .groupBy(qChannels.channelId).orderBy(
              qVideos.readCnt.subtract(qViewLog.viewCnt).multiply(10)
                  .add(qComments.commentId.count()).desc()).limit(5).fetch();

    } catch (NullPointerException e) {
      throw new NoContentException();
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.READ_CHANNEL_TOP5_ERROR);
    }
    return responseDtos;
  }


}
