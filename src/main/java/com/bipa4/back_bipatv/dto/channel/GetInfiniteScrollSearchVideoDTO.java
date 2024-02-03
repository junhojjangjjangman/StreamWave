package com.bipa4.back_bipatv.dto.channel;

import com.bipa4.back_bipatv.dto.video.GetSearchResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class GetInfiniteScrollSearchVideoDTO {

  private List<GetSearchResponseDto> channelList;
  private String nextUUID;
}
