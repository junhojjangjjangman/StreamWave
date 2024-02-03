package com.bipa4.back_bipatv.dto.video;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetUrlResponseDto {

  private String videoUrl;
  private String imageUrl;
  private String videoName;
  private String imageName;
}
