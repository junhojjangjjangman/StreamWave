package com.bipa4.back_bipatv.entity;

import com.github.f4b6a3.ulid.Ulid;
import com.querydsl.core.annotations.QueryEntity;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;


@Entity
@QueryEntity
@Data
@Table(name = "Videos")
@NoArgsConstructor
public class Videos {

  @Id
  @Column(name = "video_id", length = 26)
  private String videoId;
  @Column(name = "video_url", nullable = true, length = 200)
  @NotBlank
  private String videoUrl;
  @Size(min = 1, max = 500)
  @Column(name = "title", nullable = true, length = 500)
  @NotBlank
  private String title;
  @Size(min = 0, max = 3000)
  @Column(name = "content", nullable = true, length = 3000)
  @NotBlank
  private String content;
  @PositiveOrZero
  @Column(name = "read_cnt", nullable = true)
  @NotNull
  private int readCnt;
  @Column(name = "create_at", nullable = true)
  @NotNull
  private Timestamp createAt;
  @Column(name = "update_at", nullable = true)
  private Timestamp updateAt;
  @Column(name = "private_type")
  @NotNull
  private Boolean privateType;
  @Column(name = "thumbnail", nullable = true, length = 200)
  @NotBlank
  private String thumbnail;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Channels channelId;
}
