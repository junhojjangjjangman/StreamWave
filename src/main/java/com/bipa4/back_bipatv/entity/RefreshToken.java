package com.bipa4.back_bipatv.entity;

import javax.persistence.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "refreshToken", timeToLive = 60)
public class RefreshToken {

  @Id
  private String refreshToken;
  private String loginId;

  public RefreshToken(final String refreshToken, final String loginId) {
    this.refreshToken = refreshToken;
    this.loginId = loginId;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getMemberId() {
    return loginId;
  }
}

