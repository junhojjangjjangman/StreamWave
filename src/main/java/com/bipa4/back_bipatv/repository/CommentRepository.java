package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dto.comment.ChildCommentResponse;
import com.bipa4.back_bipatv.dto.comment.CommentResponse;
import com.bipa4.back_bipatv.entity.Comments;
import com.github.f4b6a3.ulid.Ulid;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface CommentRepository extends JpaRepository<Comments, UUID> {


  @Query(value =
      "SELECT BIN_TO_UUID(channels.channel_id) AS channelId, channels.profile_url AS channelProfileUrl, channels.name AS channelName, comments.content AS content, comments.create_at AS createAt, BIN_TO_UUID(comments.comment_id) AS commentId, comments.group_index AS groupIndex, childCount, is_updated AS isUpdated, is_picked AS isPicked\n"
          + "FROM (SELECT *, COUNT(*) over (PARTITION BY group_index) -1 AS childCount FROM bipaTV.comments WHERE video_id = :videoId) comments\n"
          + "LEFT JOIN accounts\n" + "ON comments.account_id = accounts.account_id\n"
          + "LEFT JOIN channels\n" + "ON accounts.account_id = channels.account_id\n"
          + "WHERE parent_child = 0 \n"
          + "ORDER BY isPicked desc, comments.create_at desc;", nativeQuery = true)
  List<CommentResponse> findParentComments(@Param("videoId") String videoId);

  @Query(value =
      "SELECT BIN_TO_UUID(channels.channel_id) AS channelId, channels.profile_url AS channelProfileUrl, channels.name AS channelName, comments.content AS content, comments.create_at AS createAt, BIN_TO_UUID(comments.comment_id) AS commentId, comments.group_index AS groupIndex, childCount, is_updated AS isUpdated, is_picked AS isPicked\n"
          +
          "FROM (SELECT *, COUNT(*) over (PARTITION BY group_index) -1 AS childCount FROM bipaTV.comments WHERE video_id = :videoId) comments\n"
          +
          "LEFT JOIN accounts\n" +
          "ON comments.account_id = accounts.account_id\n" +
          "LEFT JOIN channels\n" +
          "ON accounts.account_id = channels.account_id\n" +
          "WHERE parent_child = 0 \n" +
          "ORDER BY isPicked desc, comments.create_at asc;", nativeQuery = true)
  List<CommentResponse> findOldParentComments(@Param("videoId") String videoId);

  @Query(value =
      "SELECT BIN_TO_UUID(channels.channel_id) AS channelId, channels.profile_url AS channelProfileUrl, channels.name AS channelName, comments.content AS content, comments.create_at AS createAt, BIN_TO_UUID(comments.comment_id) AS commentId, comments.group_index AS groupIndex, childCount, is_updated AS isUpdated, is_picked AS isPicked\n"
          +
          "FROM (SELECT *, COUNT(*) over (PARTITION BY group_index) -1 AS childCount FROM bipaTV.comments WHERE video_id = :videoId) comments\n"
          +
          "LEFT JOIN accounts\n" +
          "ON comments.account_id = accounts.account_id\n" +
          "LEFT JOIN channels\n" +
          "ON accounts.account_id = channels.account_id\n" +
          "WHERE parent_child = 0 \n" +
          "ORDER BY isPicked desc, childCount desc, comments.create_at desc;", nativeQuery = true)
  List<CommentResponse> findPopularityParentComments(@Param("videoId") String videoId);

  @Query(value =
      "SELECT BIN_TO_UUID(channels.channel_id) AS channelId, channels.profile_url AS channelProfileUrl, channels.name AS channelName, comments.content AS content, comments.create_at AS createAt, BIN_TO_UUID(comments.comment_id) AS commentId, comments.group_index AS groupIndex, is_updated AS isUpdated\n"
          + "FROM comments \n" + "LEFT JOIN accounts\n"
          + "ON comments.account_id = accounts.account_id\n" + "LEFT JOIN channels\n"
          + "ON accounts.account_id = channels.account_id\n"
          + "WHERE video_id = :videoId AND parent_child = 1 AND group_index = :groupIndex\n"
          + "ORDER BY comments.create_at desc", nativeQuery = true)
  List<ChildCommentResponse> findChildComments(@Param("videoId") String videoId,
      @Param("groupIndex") int groupIndex);


  @Query(value = "SELECT MAX(group_index) \n" + "FROM comments\n" + "WHERE parent_child = 0\n"
      + "AND video_id  = :videoId", nativeQuery = true)
  Integer findMaxGroupIndex(@Param("videoId") String videoId);

  @Modifying
  @Query(value = "DELETE FROM comments\n" + "WHERE group_index = :groupIndex \n"
      + "AND video_id = :videoId", nativeQuery = true)
  Integer deleteParentComment(@Param("videoId") String videoId,
      @Param("groupIndex") int groupIndex);

  @Query(value =
      "SELECT * FROM comments\n"
          + "WHERE video_id = :videoId\n"
          + "AND is_picked = 1;", nativeQuery = true)
  Comments findPickedParentComment(@Param("videoId") String videoId);


}
