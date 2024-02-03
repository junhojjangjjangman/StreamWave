package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dataType.ETokenTime;
import com.bipa4.back_bipatv.entity.RefreshToken;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRepository {

  private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = ETokenTime.REFRESHTOKEN_EXP_TIME_TEST.getTime(); // 토큰 만료 시간 (6시간)


  private final RedisTemplate<String, String> redisTemplate;
  private final RedisTemplate<String, String> redisBlackListTemplate;

  @Autowired
  public RedisRepository(RedisTemplate<String, String> redisTemplate,
      RedisTemplate<String, String> redisBlackListTemplate) {
    this.redisTemplate = redisTemplate;
    this.redisBlackListTemplate = redisBlackListTemplate;
  }

  // Refresh Token을 Redis에 저장합니다.
  public void save(RefreshToken refreshToken) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(refreshToken.getRefreshToken(), refreshToken.getMemberId(),
        REFRESH_TOKEN_EXPIRATION_SECONDS, TimeUnit.SECONDS);
  }

  // Refresh Token을 Redis에서 검색합니다.
  public Optional<RefreshToken> findById(String refreshToken) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String memberId = valueOperations.get(refreshToken);

    if (memberId != null) {
      return Optional.of(new RefreshToken(refreshToken, memberId));
    } else {
      return Optional.empty();
    }
  }

  public boolean delete(String refreshToken) {
    if (redisTemplate.opsForValue().get(refreshToken) != null) {
      redisTemplate.delete(refreshToken);
      return true;
    }
    return false;
  }

  public void setBlackList(String accessTokenValue, String accessTokenName, Long expiration) {
    ValueOperations<String, String> valueOperations = redisBlackListTemplate.opsForValue();
    valueOperations.set(accessTokenValue, accessTokenName,
        expiration, TimeUnit.SECONDS);
  }

  public String getBlackList(String accessTokenValue) {
    return redisBlackListTemplate.opsForValue().get(accessTokenValue);
  }

  public boolean deleteBlackList(String accessTokenValue) {
    if (redisBlackListTemplate.opsForValue().get(accessTokenValue) != null) {
      redisBlackListTemplate.delete(accessTokenValue);
      return true;
    } else {
      return false;
    }
  }

  public boolean hasKeyBlackList(String accessTokenValue) {
    return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(accessTokenValue));
  }
}
