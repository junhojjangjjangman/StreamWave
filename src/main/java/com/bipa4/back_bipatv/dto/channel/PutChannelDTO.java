package com.bipa4.back_bipatv.dto.channel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class PutChannelDTO {

  @Size(min = 0, max = 200)
  private String content;

  @NotBlank
  @Pattern(regexp = "^[^\\s!@#$%^&*()_+{}\\[\\]:;<>,.?/~\\\\|]{2,99}$")//특수문자와 공백을 제외한 문자)
  private String channelName;

  private boolean privateType;
  private String profileUrl;

}
