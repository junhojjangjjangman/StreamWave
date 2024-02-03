package com.bipa4.back_bipatv.dto.comment;

import com.github.f4b6a3.ulid.Ulid;
import java.sql.Timestamp;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentRequest {

  private UUID commentId;

  @NotBlank
  private String content;
  private int parentChild;
  private int groupIndex;
  private Timestamp createAt;
  private Boolean isUpdated;
  private Boolean isPicked;
  private String videoId;
}
