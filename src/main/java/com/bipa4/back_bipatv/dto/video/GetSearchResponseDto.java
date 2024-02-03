package com.bipa4.back_bipatv.dto.video;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;


public interface GetSearchResponseDto {

  String getVideoId();

  UUID getChannelId();

  String getThumbnail();

  String getVideoTitle();

  String getContent();

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  Date getCreateAt();

  Integer getReadCount();

  boolean getPrivateType();

  String getChannelProfileUrl();

  String getChannelName();
}
