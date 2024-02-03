package com.bipa4.back_bipatv.service;

import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.entity.Logs;
import com.bipa4.back_bipatv.repository.LogRepository;
import com.bipa4.back_bipatv.security.SecurityService;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LogService {

  private final LogRepository logRepository;
  private final SecurityService securityService;

  public void saveLog(HttpServletRequest request, String statusCode, String status) {
    Cookie[] cookies = request.getCookies();
    String accessToken = null;
    Accounts loginUser = new Accounts();
    Logs log = new Logs();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("accessToken".equals(cookie.getName())) {
          accessToken = cookie.getValue();
          loginUser = securityService.getSubjectAccount(accessToken);

          log.setAccounts(loginUser);
          log.setLogFunction(request.getRequestURI());
          log.setContent(status + "Status Code:" + statusCode);
          Timestamp now = Timestamp.valueOf(
              LocalDateTime.now().plusHours(9)
                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          log.setDate(now);
          logRepository.save(log);
        }
      }
    }
  }
}
