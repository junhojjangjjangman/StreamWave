package com.bipa4.back_bipatv.dto.user;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetAccountCheckDTO {

  private UUID channelId;
  private String channelName;
  private String channelProfileUrl;
}
