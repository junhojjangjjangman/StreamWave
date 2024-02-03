package com.bipa4.back_bipatv.service;

import com.bipa4.back_bipatv.dao.AccountDAO;
import com.bipa4.back_bipatv.dataType.ELogin_Type;
import com.bipa4.back_bipatv.dto.user.GetAccountCheckDTO;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.entity.Channels;
import com.bipa4.back_bipatv.entity.RefreshToken;
import com.bipa4.back_bipatv.exception.ResourceNotFoundException;
import com.bipa4.back_bipatv.repository.AccountRepository;
import com.bipa4.back_bipatv.repository.ChannelRepository;
import com.bipa4.back_bipatv.repository.RedisRepository;
import com.bipa4.back_bipatv.security.SecurityService;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

//  private final int EXP_TIME = 2 * 1000 * 60 * 60;//2시간
  private final int EXP_TIME_TEST = 1000 * 60;//분

  private final Environment env;
  private final RestTemplate restTemplate = new RestTemplate();
  private final AccountDAO accountDAO;
  private final SecurityService securityService;
  private final RedisRepository redisRepository;
  private final AccountRepository accountRepository;
  private final ChannelRepository channelRepository;

  private void insertUser(Accounts accounts) {

    Timestamp now = new Timestamp(System.currentTimeMillis());
    UUID uuid = UUID.randomUUID();
    accounts.setAccountId(uuid);
    accounts.setJoinDate(now);
    accountDAO.createAccount(accounts);

  }

  private void insertChannels(Accounts accounts) {
    Channels channels = new Channels();
    UUID uuid = UUID.randomUUID();
    channels.setChannelId(uuid);
    channels.setChannelName(accounts.getLoginId() + "-Channel");
    channels.setPrivateType(false);
    channels.setAccounts(accounts);
    channels.setProfileUrl(accounts.getProfileUrl());
    channels.setContent(accounts.getName() + "의 채널");
    channelRepository.save(channels);
  }

  private boolean findAccount(Accounts accounts) {
    return accountDAO.findAccount(accounts);
  }

  private Accounts selectAccount(Accounts accounts) {
    return accountDAO.selectAccount(accounts);
  }

  private Cookie createCookie(String name, String token) {
    Cookie cookie = new Cookie(name, token);

    return cookie;
  }


  private String getAccessToken(String authorizationCode, String registrationId) {
    String clientId = env.getProperty("oauth2." + registrationId + ".client-id");
    String clientSecret = env.getProperty("oauth2." + registrationId + ".client-secret");
    String redirectUri = env.getProperty("oauth2." + registrationId + ".redirect-uri");
    String tokenUri = env.getProperty("oauth2." + registrationId + ".token-uri");
//  Httpheader 오브젝트 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-type",
        "application/x-www-form-urlencoded;charset=utf-8");//-->http데이터가 key:value형식이라는 것을 알려주는거임
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

//  Httpbody 오브젝트 생성
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", authorizationCode);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("grant_type", "authorization_code");

//  Httpheader와 body를 하나의 오브젝트에 담기
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

    ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity,
        JsonNode.class);
    JsonNode accessTokenNode = responseNode.getBody();
    return accessTokenNode.get("access_token").asText();
  }

  private JsonNode getUserResource(String accessToken, String registrationId) {
    String resourceUri = env.getProperty("oauth2." + registrationId + ".resource-uri");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    HttpEntity entity = new HttpEntity(headers);
    return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
  }

  public Map<String, Cookie> socialLogin(String code,
      String registrationId) {//구글, 카카오에게 로그인 정보를 받은 후 실행되는거임 인증은 카카오랑 구글이 함
    String accessToken = getAccessToken(code, registrationId);

    JsonNode userResourceNode = getUserResource(accessToken, registrationId);
    Accounts accounts = new Accounts();

    switch (registrationId) {
      case "google": {
        accounts.setLoginId("google_" + userResourceNode.get("id").asText());
        accounts.setEMail(userResourceNode.get("email").asText());
        JsonNode nameNode = userResourceNode.get("name");
        if (nameNode != null && !nameNode.isNull()) {
          accounts.setName(nameNode.asText());
        } else {
          accounts.setName("google_" + userResourceNode.get("id").asText());
        }

        accounts.setProfileUrl(userResourceNode.get("picture").asText());
        accounts.setLoginType(ELogin_Type.GOOGLE);
        break;
      }
      case "kakao": {
        accounts.setLoginId("kakao_" + userResourceNode.get("id").asText());
        if (userResourceNode.get("kakao_account").get("email_needs_agreement")
            .asBoolean()) {//사용자가 사용 동의하면 false값이 들어옴
          accounts.setEMail("");
        } else {
          accounts.setEMail(userResourceNode.get("kakao_account").get("email").asText());
        }
        if (userResourceNode.get("kakao_account").get("profile_nickname_needs_agreement")
            .asBoolean()) {
          accounts.setName("kakao_" + userResourceNode.get("id").asText());
        } else {
          accounts.setName(
              userResourceNode.get("kakao_account").get("profile").get("nickname").asText());
        }
        accounts.setLoginType(ELogin_Type.KAKAO);
        if (userResourceNode.get("properties").get("thumbnail_image").asBoolean()) {
          accounts.setProfileUrl(
              userResourceNode.get("properties").get("thumbnail_image").asText());
        } else {
          accounts.setProfileUrl("");
        }
        break;
      }
      default: {
        throw new RuntimeException("UNSUPPORTED SOCIAL TYPE");
      }
    }
    //유저 아이디에 대한 리프레쉬 토큰 검샘
    if (!findAccount(accounts)) {//
      insertUser(accounts);
      insertChannels(accounts);
    }
    if (accountDAO.selectAccount(accounts).getDeleteAt() != null) {//삭제된 아이디로 로그인 시 null반환
      return null;
    }
    String refreshToken = securityService.createRefreshToken(accounts);
    RefreshToken Rtoken = null;
    if (redisRepository.findById(refreshToken).isEmpty()) {
      Rtoken = new RefreshToken(refreshToken, accounts.getLoginId());
      redisRepository.save(Rtoken);
    }

//    String loginAccountToken = securityService.createToken(accounts, EXP_TIME);
    String loginAccountToken = securityService.createToken(accounts, EXP_TIME_TEST);
    Cookie refreshCookie = createCookie("RefreshToken", refreshToken);
    Cookie accessCookie = createCookie("AccessToken", loginAccountToken);
    Map<String, Cookie> map = new HashMap<>();
    map.put("refreshToken", refreshCookie);
    map.put("accessToken", accessCookie);

    return map;
  }

  public String createAccessTokenToRefreshToken(
      String refreshToken) {//리플레시 토큰으로 엑세스 토큰을 다시 만들어달라는 것을 요청할 때 사용할 메소드
    if (refreshToken == null) {
      return null;
    }
    Optional<RefreshToken> optRefreshToken = redisRepository.findById(refreshToken);
    if (optRefreshToken.isPresent()) {
      Accounts dummyAccount = new Accounts();
      dummyAccount.setLoginId(optRefreshToken.get().getMemberId());
      return securityService.createToken(accountDAO.selectAccount(dummyAccount), EXP_TIME_TEST);
//      return securityService.createToken(accountDAO.selectAccount(dummyAccount), EXP_TIME_TEST);
    } else {//재 로그인 요청
      return null;
    }
  }

  @Transactional
  public Accounts updateAccount(Accounts loginUser, Accounts updateDataAccount) {
    boolean flag = false;
    if (loginUser != null) {
      if (!loginUser.getProfileUrl().equals(updateDataAccount.getProfileUrl())) {
        loginUser.setProfileUrl(updateDataAccount.getProfileUrl());
        flag = true;
      }
      if (!(loginUser.getName().equals(updateDataAccount.getName()))) {
        loginUser.setName(updateDataAccount.getName());
        flag = true;
      }
      if (flag) {
        return accountRepository.save(loginUser);
      }
      return null;
    }
    throw new ResourceNotFoundException(
        "User not found"); // 리소스를 찾지 못한 경우 예외 던지기
  }

  public boolean logout(String refreshToken, String accessToken) {
    // 1. Access Token 검증
    if (!securityService.isTokenValid(accessToken)) {//token값이 유효한가
      throw new IllegalArgumentException("로그아웃 : 유효하지 않은 accessToken토큰입니다.");
    }

    // 2. Access Token 에서 authentication 을 가져옵니다.
    Accounts accounts = securityService.getSubjectAccount(accessToken);

    // 3. DB에 저장된 Refresh Token 제거
    boolean flag1 = redisRepository.delete(refreshToken);
    // 4. Access Token blacklist에 등록하여 만료시키기
    // 해당 엑세스 토큰의 남은 유효시간을 얻음
    Long expiration = securityService.getExpiration(accessToken);
    redisRepository.setBlackList(accessToken, "accessToken",
        expiration);
    boolean flag2 = redisRepository.getBlackList(accessToken) != null;
    return flag1 && flag2;
  }

  public void deleteAccount(String accessToken) {
    Accounts loginAccount = securityService.getSubjectAccount(accessToken);
    LocalDateTime now = LocalDateTime.now();

    loginAccount.setDeleteAt(Timestamp.valueOf(
        now.plusHours(9).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    accountRepository.save(loginAccount);
  }

  public GetAccountCheckDTO getAccountCheck(String accessToken) {
    Accounts loginAccount = securityService.getSubjectAccount(accessToken);
    return accountRepository.getAccountCheckDto(loginAccount.getAccountId());
  }
}
