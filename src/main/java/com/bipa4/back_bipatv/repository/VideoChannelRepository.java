package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dto.video.GetSearchResponseDto;
import com.bipa4.back_bipatv.entity.Videos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoChannelRepository extends JpaRepository<Videos, Long> {

  @Modifying
  @Query(value =
      "SELECT video_id as videoId, BIN_TO_UUID(c.channel_id) as channelId, s.content, create_at as createAt, s.private_type as privateType, read_cnt as readCount, thumbnail, title as videoTitle, profile_url as channelProfileUrl, name as channelName FROM (SELECT *, MATCH (v.title, v.content) AGAINST (:searchQuery IN NATURAL LANGUAGE MODE) as score\n"
          + " FROM bipaTV.videos v) s left outer join channels c on s.channel_id = c.channel_id\n"
          + " WHERE s.score > 0.7 AND s.private_type = false AND c.private_type = false ORDER BY s.score DESC", nativeQuery = true)
  List<GetSearchResponseDto> findBySearchQuery(@Param("searchQuery") String searchQuery);
}
