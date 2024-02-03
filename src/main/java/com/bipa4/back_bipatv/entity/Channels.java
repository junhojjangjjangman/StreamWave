package com.bipa4.back_bipatv.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Table(name = "Channels")
public class Channels {

  //  @Id
//  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//  @Type(type = "uuid-char")
//  @Column(name = "channel_id", length = 36)
//  private UUID channelId;
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "channel_id", columnDefinition = "BINARY(16)")
  private UUID channelId;

  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String channelName;

  @Column(name = "content", nullable = true, length = 200)
  private String content;

  @Column(name = "private_type", nullable = true)
  private Boolean privateType;

  @Column(name = "profile_url", nullable = true)
  private String profileUrl;

  @OneToOne
  @JoinColumn(name = "account_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Accounts accounts;
}
