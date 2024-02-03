package com.bipa4.back_bipatv.entity;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Table(name = "Logs")
public class Logs {

  @Id
  @Column(name = "log_id", columnDefinition = "BINARY(16)")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @GeneratedValue(generator = "UUID")
  private UUID logId;
  @Column(name = "date", nullable = false)
  private Timestamp date;
  @Column(name = "content", nullable = false, length = 400)
  private String content;
  @Column(name = "log_function", nullable = false, length = 200)
  private String logFunction;
  @ManyToOne
  @JoinColumn(name = "account_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Accounts accounts;

}
