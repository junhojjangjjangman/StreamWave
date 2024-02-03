package com.bipa4.back_bipatv.dto.video;

import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
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
public class GetCategoryNameRequestDto {

  @ApiModelProperty(example = "카테고리 id")
  private UUID categoryNameId;

  @ApiModelProperty(example = "카테고리 이름")
  private String categoryName;
}
