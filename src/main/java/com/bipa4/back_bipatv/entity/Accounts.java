package com.bipa4.back_bipatv.entity;


import com.bipa4.back_bipatv.dataType.ELogin_Type;
import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
@Table(name = "Accounts")
@NoArgsConstructor
public class Accounts {

  @Id
  @Column(name = "account_id", columnDefinition = "BINARY(16)")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  private UUID accountId;


  @Column(name = "join_date", nullable = false)
  private Timestamp joinDate;


  @Column(name = "login_type", nullable = false, length = 20)
  @Enumerated(value = EnumType.STRING)
  private ELogin_Type loginType;


  @Column(name = "login_Id", nullable = false, length = 100)
  private String loginId;


  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "profile_url", nullable = true, length = 200)
  private String profileUrl;

  @Column(name = "email")
  @Email
  private String eMail;

  @Column(name = "dalete_at")
  private Timestamp deleteAt;
}
