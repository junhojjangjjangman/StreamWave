package com.bipa4.back_bipatv.dto.channel;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class GetInfiniteScrollRequestChannelDto {

  private List<GetChannelDTO> channelList;
  private UUID nextUUID;
}
