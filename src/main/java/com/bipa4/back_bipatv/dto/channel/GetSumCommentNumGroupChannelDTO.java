package com.bipa4.back_bipatv.dto.channel;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GetSumCommentNumGroupChannelDTO {

  private UUID channelId;
  private Integer sumCommentNum;//댓글 총 합
}
