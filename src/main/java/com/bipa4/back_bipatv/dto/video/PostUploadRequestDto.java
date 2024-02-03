package com.bipa4.back_bipatv.dto.video;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostUploadRequestDto {

  @NotNull
  @ApiModelProperty(example = "비디오 URL")
  private String videoUrl;

  @NotNull
  @ApiModelProperty(example = "썸네일 URL")
  private String thumbnailUrl;

  @NotNull
  @ApiModelProperty(example = "영상 제목")
  private String title;

  @NotNull
  @ApiModelProperty(example = "영상 내용")
  private String content;

  @NotNull
  @ApiModelProperty(example = "영상 공개 여부")
  private Boolean privateType;

  @ApiModelProperty(example = "카테고리")
  private List<String> category;
}
