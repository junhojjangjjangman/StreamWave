package com.bipa4.back_bipatv.dto.video;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PutUpdateRequestDto {

  private String videoUrl;
  private String thumbnailUrl;
  private String title;
  private String content;
  private boolean privateType;
  private List<UUID> category;
}
