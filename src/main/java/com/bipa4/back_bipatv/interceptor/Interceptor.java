package com.bipa4.back_bipatv.interceptor;

import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.security.SecurityService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

  private final SecurityService securityService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String userName = "비회원";
    String accessToken = null;
    boolean flag = false;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("accessToken".equals(cookie.getName())) {
          accessToken = cookie.getValue();
          flag = securityService.isTokenValid(accessToken);
        }
        if(!flag){
          Accounts accounts = securityService.newAccessTokenAccount();
          System.out.println(accounts);
          if(accounts != null){
            userName = accounts.getName();
          }
        }

      }
    }

    log.info("사용자: {} 호출 url: {}", userName, request.getRequestURI());
    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    log.info("postHandle response status: {}", response.getStatus());
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) throws Exception {
    if (ex != null) {
      // 예외가 발생한 경우 로그에 에러 메시지를 추가합니다.
      log.error("afterCompletion Request completed with error: " + ex.getMessage());
    } else {
      // 예외가 발생하지 않은 경우 HTTP 응답 코드를 로그로 남깁니다.
      log.info("afterCompletion Response status: " + response.getStatus());
    }
    HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
  }
}
