package com.bipa4.back_bipatv.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Table(name = "view_log")
public class ViewLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "view_logId")
  private Long viewLogId;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "video_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Videos videoId;

  @PositiveOrZero
  @Column(name = "view_cnt")
  private int viewCnt;
}
