package com.bipa4.back_bipatv.aspect;

import com.bipa4.back_bipatv.dataType.ETokenTime;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.UserService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AccessTokenAspect {

  private final SecurityService securityService;
  private final UserService userService;

  @Before("@annotation(accessTokenValid)")
  public void checkAccessToken(JoinPoint joinPoint, AccessTokenValid accessTokenValid) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    HttpServletResponse httpServletResponse = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    if (httpServletResponse != null) {

      Cookie[] cookies = request.getCookies();
      String accessToken = null;
      String refreshToken = null;
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("accessToken")) {
          accessToken = cookie.getValue();
        }
        if (cookie.getName().equals("refreshToken")) {
          refreshToken = cookie.getValue();
        }
      }
      boolean tokenValid = false;
      if (accessToken != null) {
        tokenValid = securityService.isTokenValid(accessToken);
      }
      System.out.println("token 유효성 : " + tokenValid);
      if (tokenValid) {//토큰을 사용할 수 있는 경우
        System.out.println("토큰 유효 기간이 안 지난 경우 들어오는지 확인");
        //어떤 값이 들어와야할까?
      } else {//토큰 유효시간이 지난 경우
        System.out.println("토큰 유효시간이 지난 경우 accessToken 다시 만들기");

        String newAccessToken = userService.createAccessTokenToRefreshToken(refreshToken);
        if (newAccessToken != null) {
          ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken",
                  newAccessToken)
              .path("/")
              .maxAge(ETokenTime.ACCESSTOKEN_EXP_TIME_TEST.getTime())
              .httpOnly(true)
              .secure(true)
              .sameSite("None")
              .build();
          request.setAttribute("newAccessToken", newAccessToken);
          httpServletResponse.addHeader("Set-Cookie", newAccessTokenCookie.toString());
          System.out.println("재발급된 accessToken:" + newAccessToken);
        } else {//refreshToken을 발견하지 못한 경우, 유효하지 않은 경우
          System.out.println("refreshToken유효하지 않음");
        }
      }
    }
  }
}

