package com.bipa4.back_bipatv.dto.video;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetFileUrlResponseDto {

  private String fileUrl;
  private String fileName;
}
