package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GetTop5StatisticsDTO {

  private UUID channelId;
  private Integer statistic;
}
