package com.bipa4.back_bipatv.security;

import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dao.AccountDAO;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.exception.JwtNotFoundException;
import com.bipa4.back_bipatv.repository.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SecurityService {

  @Value("${security.secretKey}")
  private String SECRET_KEY;

  @Autowired
  AccountDAO accountDAO;
  @Autowired
  RedisRepository redisRepository;

  public String createToken(Accounts accounts,
      long expTime) {//Id는 subject, PW는 SecretKey를 만드는데 사용 보통
    if (expTime <= 0) {
      throw new RuntimeException("만료시간이 0보다 커야함");
    }
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
        SECRET_KEY);//SECRET_KEY byte타입으로 만들어서 넣어줌
    Key singingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());//key가 만들어짐

    return Jwts.builder()//지금은 subject값고 만료시간만 넣어줌 but 다양한 값 넣을 수 있음 확인해보기
        .setSubject(accounts.getLoginId())
        .signWith(singingKey, signatureAlgorithm)
        .claim("userId", accounts.getLoginId())
        .claim("nickName", accounts.getName())
        .setExpiration(new Date(System.currentTimeMillis() + expTime))//만료시간
        .compact();
  }

  public String createRefreshToken(Accounts accounts) {
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(
        SECRET_KEY);//SECRET_KEY byte타입으로 만들어서 넣어줌
    Key singingKey = new SecretKeySpec(secretKeyBytes, signatureAlgorithm.getJcaName());//key가 만들어짐

    return Jwts.builder()//지금은 subject값고 만료시간만 넣어줌 but 다양한 값 넣을 수 있음 확인해보기
        .setSubject("RefreshToken" + accounts.getLoginId())
        .signWith(singingKey, signatureAlgorithm)
//        .setExpiration(new Date(System.currentTimeMillis() + 6 * 1000 * 60 * 60))//만료시간
        .setExpiration(new Date(System.currentTimeMillis() + 5 * 1000 * 60))//만료시간 5분 테스트
        .compact();
  }

  public boolean getSubject(String token) {//------------------valid 토큰 accessToken검증

    Claims claims = Jwts.parserBuilder()
        .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
        .build()
        .parseClaimsJws(token)
        .getBody();
    Date expirationDate = claims.getExpiration();
    Date now = new Date();

    if (expirationDate != null && expirationDate.after(now)
        && redisRepository.getBlackList(token) == null) {
      // JWT가 유효하면 사용자 정보를 가져옴
      Accounts dummyAccount = new Accounts();
      dummyAccount.setLoginId(claims.getSubject());
      return accountDAO.findAccount(dummyAccount);
    } else {
      throw new JwtNotFoundException();
    }

  }

  public Accounts getSubjectAccount(String token) {
    if (token == null) {
      return null;
    }
    Claims claims;
    try{
      claims = Jwts.parserBuilder()
          .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
          .build()
          .parseClaimsJws(token)
          .getBody();
    }catch (ExpiredJwtException e){
      return newAccessTokenAccount();
    }catch (Exception e) {
      // 다른 예외 처리
      throw e;
    }

    // JWT의 만료 시간 (exp) 확인
    Date expirationDate = claims.getExpiration();
    Date now = new Date();

    if (expirationDate != null && expirationDate.after(now)
        && redisRepository.getBlackList(token) == null) {
      // JWT가 유효하면 사용자 정보를 가져옴
      Accounts dummyAccount = new Accounts();
      dummyAccount.setLoginId(claims.getSubject());
      return accountDAO.selectAccount(dummyAccount);
    } else {
      throw new JwtNotFoundException();
    }
  }
  @AccessTokenValid
  public Accounts newAccessTokenAccount(){
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String nat = (String)request.getAttribute("newAccessToken");
    Accounts accounts = null;
    if (nat != null) {
      accounts = getSubjectAccount(nat);
    }
    return accounts;
  }
  public boolean isTokenValid(String token) {
    return validateToken(token);
  }

  public boolean validateToken(String token) {
    Claims claims;
    try {
      claims = Jwts.parserBuilder()
          .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      // 토큰이 만료되었을 때 예외 처리
      return false;
    } catch (Exception e) {
      // 다른 예외 처리
      throw e;
    }

    Date expirationDate = claims.getExpiration();
    Date now = new Date();
    return expirationDate != null && expirationDate.after(now)
        && redisRepository.getBlackList(token) == null;
  }


  public Long getExpiration(String accessToken) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
        .build()
        .parseClaimsJws(accessToken)
        .getBody();
    return claims.getExpiration().getTime();
  }
}
