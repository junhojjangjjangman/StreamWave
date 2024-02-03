package com.bipa4.back_bipatv.dto.comment;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

public interface CommentResponse {

  String getCommentId();

  int getGroupIndex();

  String getChannelProfileUrl();

  String getChannelName();

  String getContent();
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  Date getCreateAt();

  int getChildCount();

  Boolean getIsUpdated();

  Boolean getIsPicked();

  String getChannelId();
}
