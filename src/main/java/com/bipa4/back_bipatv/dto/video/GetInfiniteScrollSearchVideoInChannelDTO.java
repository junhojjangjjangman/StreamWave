package com.bipa4.back_bipatv.dto.video;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class GetInfiniteScrollSearchVideoInChannelDTO {

  private List<GetSearchVideoINChannelDTO> videos;
  private Integer page;
}
