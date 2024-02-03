package com.bipa4.back_bipatv.dto.video;

import com.bipa4.back_bipatv.entity.Channels;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostSaveRequestDto {

  private String videoUrl;
  private String title;
  private String content;
  private boolean privateType;
  private boolean commentPermission;
  private String thumbnail;
  private Channels channelId;

  @Builder
  public PostSaveRequestDto(String videoUrl, String title, String content, boolean privateType,
      boolean commentPermission, String thumbnail, Channels channelId) {
    this.videoUrl = videoUrl;
    this.title = title;
    this.content = content;
    this.privateType = privateType;
    this.commentPermission = commentPermission;
    this.thumbnail = thumbnail;
    this.channelId = channelId;
  }

//  public Videos toEntity() {
//    return Videos.builder().title(title).content(content).videoUrl(videoUrl)
//        .privateType(privateType).commentPermission(commentPermission).thumbnail(thumbnail)
//        .channelId(channelId).build();
//  }
}
