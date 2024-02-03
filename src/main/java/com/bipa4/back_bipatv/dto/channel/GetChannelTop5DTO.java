package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetChannelTop5DTO {

  private Integer ranking; // ranking 필드 추가
  private UUID channelId;
  private String channelName;
  private String profileUrl;
  private String content;

}
