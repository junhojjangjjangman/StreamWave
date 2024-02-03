package com.bipa4.back_bipatv.dto.video;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.f4b6a3.ulid.Ulid;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetVideoResponseDto {

  private String channelName;
  private String channelProfileUrl;
  private String thumbnail;
  private String videoTitle;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
  private Date createAt;
  private Integer readCount;
  private String videoId;
  private UUID channelId;
  private boolean privateType;
}
