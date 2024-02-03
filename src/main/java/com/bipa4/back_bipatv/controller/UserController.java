package com.bipa4.back_bipatv.controller;


import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dataType.ETokenTime;
import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.dto.user.GetAccountCheckDTO;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.PresignedUrlService;
import com.bipa4.back_bipatv.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "AccountController v1")
@RequestMapping(produces = "application/json")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final SecurityService securityService;
  private final PresignedUrlService presignedUrlService;

  @ApiOperation(value = "Social_Login", notes = "소셜 로그인")
  @GetMapping("/auth/{registrationId}/callback")
  public ResponseEntity<String> doLogin(@RequestParam String code,
      @PathVariable String registrationId,
      HttpServletResponse httpresponse) {

    Map<String, Cookie> cookieMap = userService.socialLogin(code, registrationId);
    if (cookieMap != null) {
      ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",
              cookieMap.get("refreshToken").getValue())
          .path("/")
          .maxAge(ETokenTime.REFRESHTOKEN_EXP_TIME_TEST.getTime())// refreshToken도 6시간
          .httpOnly(true)
          .secure(true)
          .sameSite("None")
          .build();
      httpresponse.addHeader("Set-Cookie", refreshCookie.toString());

      ResponseCookie accessCookie = ResponseCookie.from("accessToken",
              cookieMap.get("accessToken").getValue())
          .path("/")
          .maxAge(ETokenTime.ACCESSTOKEN_EXP_TIME_TEST.getTime())//기간 2시간
          .httpOnly(true)
          .secure(true)
          .sameSite("None")
          .build();
      httpresponse.addHeader("Set-Cookie", accessCookie.toString());
      return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }
    throw new CustomApiException(ErrorCode.LOGIN_ERROR);
  }

  @ApiOperation(value = "userUpdate", notes = "유저 정보 수정")
  @PutMapping("/account")
  public ResponseEntity<Accounts> updateAccount(@CookieValue(value = "accessToken") String code,
      @RequestBody @Validated Accounts accounts, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      code = nat;
    }

    Accounts loginAccount = securityService.getSubjectAccount(code);
    Accounts updatedAccount = userService.updateAccount(loginAccount, accounts);

    return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
  }

  @ApiOperation(value = "CheckAccount", notes = "accessToken에 맞는 Account반환")
  @GetMapping("/account/check")
  public ResponseEntity<GetAccountCheckDTO> getAccountCheck(
      @CookieValue(name = "accessToken", required = false) String accessToken,
    HttpServletRequest request) {
      GetAccountCheckDTO getAccountCheckDTO = null;

      String nat = (String) request.getAttribute("newAccessToken");
      if (nat != null) {
        accessToken = nat;
      }

      if (accessToken != null) {
      getAccountCheckDTO = userService.getAccountCheck(accessToken);
    }

    return getAccountCheckDTO == null ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
        : new ResponseEntity<>(getAccountCheckDTO, HttpStatus.OK);
  }

  @ApiOperation(value = "Logout", notes = "로그아웃기능")
  @AccessTokenValid
  @PostMapping("/account/logout")
  public ResponseEntity<Boolean> doLogout(HttpServletResponse httpresponse,
      @CookieValue(value = "refreshToken") String refreshToken,
      @CookieValue(value = "accessToken") String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    boolean result = userService.logout(refreshToken, accessToken);
    //쿠키를 삭제하기 위해선 쿠키의 이름을 같게하고 유효기간을 0을 주어 삭제한다.
    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",
            "")
        .path("/")
        .maxAge(0)
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .build();
    httpresponse.addHeader("Set-Cookie", refreshCookie.toString());

    ResponseCookie accessCookie = ResponseCookie.from("accessToken",
            "")
        .path("/")
        .maxAge(0)
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .build();
    httpresponse.addHeader("Set-Cookie", accessCookie.toString());
    if (!result) {
      throw new CustomApiException(ErrorCode.LOGOUT_ERROR);
    }
    return new ResponseEntity<>(true, HttpStatus.OK);
  }

  @ApiOperation(value = "Delete Account", notes = "회원 탈퇴")
  @AccessTokenValid
  @DeleteMapping("/account")
  public void deleteAccount(
      @CookieValue("accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    userService.deleteAccount(accessToken);
  }

//  @ApiOperation(value = "ReCreateAcessToken", notes = "액세스 토큰 재발급")
//  @GetMapping("/account/reAcessToken")
//  public void reCreateAcessToken(@CookieValue("refreshToken") String refreshToken,
//      HttpServletResponse httpresponse) {
//    ResponseCookie accessCokkie = ResponseCookie.from("accessToken",
//            userService.createAccessTokenToRefreshToken(refreshToken))
//        .path("/")
//        .maxAge(ACCESSTOKEN_EXP_TIME)// refreshToken도 6시간
//        .httpOnly(true)
//        .secure(true)
//        .sameSite("None")
//        .build();
//    httpresponse.addHeader("Set-Cookie", accessCokkie.toString());
//  }
}
