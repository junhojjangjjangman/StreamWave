package com.bipa4.back_bipatv.dto.video;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class GetSearchVideoINChannelDTO {

  private String channelName;
  private String channelProfileUrl;
  private String thumbnail;
  private String videoTitle;
  private Timestamp createAt;
  private int readCount;
  private String videoId;
  private UUID channelId;
  private Integer ranking;
}
