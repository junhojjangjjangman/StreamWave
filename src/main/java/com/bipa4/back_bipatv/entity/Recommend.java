package com.bipa4.back_bipatv.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Table(name = "Recommend")
public class Recommend {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recommend_id", nullable = false)
  private long recommendId;

  @Column(name = "account_id", nullable = false)
  private long accountId;

  @Column(name = "video_id", nullable = false)
  private long videoId;

  @Column(name = "rating", nullable = false)
  private float rating;

  @ManyToOne
  @JoinColumn(name = "video_ulid_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Videos videoUUIDId;
}
