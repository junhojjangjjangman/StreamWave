package com.bipa4.back_bipatv.dto.video;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.f4b6a3.ulid.Ulid;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@Setter
@Getter
@NoArgsConstructor
public class GetDetailResponseDto {

  private String channelName;
  private String channelProfileUrl;
  private UUID channelId;
  private String videoUrl;
  private String videoTitle;
  private String content;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  private Timestamp createAt;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+9")
  private Timestamp updateAt;
  private Integer readCount;
  private boolean privateType;
  private String videoId;
  private String thumbnail;
  private List<GetVideoResponseDto> recommendedList;
  private Long likeCount;
  private List<UUID> categoryId;
}
