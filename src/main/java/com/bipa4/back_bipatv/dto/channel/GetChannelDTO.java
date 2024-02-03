package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetChannelDTO {

  private UUID channelId;
  private String channelName;
  private String content;
  private boolean privateType;
  private String profileUrl;
}
