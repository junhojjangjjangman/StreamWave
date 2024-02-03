package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GetSumVideoViewNumGroupChannelDTO {

  private UUID channelId;
  private Integer totalScore;//1시간 동안 본 시청 수 합
}
