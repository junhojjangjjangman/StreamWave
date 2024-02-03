package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Setter
@Getter
public class GetSearchChannelDTO {

  private UUID channelId;
  private String channelName;
  private String content;
  private boolean privateType;
  private String profileUrl;
  private Integer ranking;

  public GetSearchChannelDTO() {
  }
}
