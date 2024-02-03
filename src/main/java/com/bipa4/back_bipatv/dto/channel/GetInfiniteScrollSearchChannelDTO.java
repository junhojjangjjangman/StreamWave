package com.bipa4.back_bipatv.dto.channel;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class GetInfiniteScrollSearchChannelDTO {

  private List<GetSearchChannelDTO> channelList;
  private Integer page;
}
