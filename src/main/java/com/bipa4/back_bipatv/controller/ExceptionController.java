package com.bipa4.back_bipatv.controller;

import com.bipa4.back_bipatv.dto.ErrorResult;
import com.bipa4.back_bipatv.exception.AuthorizationException;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.exception.NoContentException;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Api(tags = {"ExceptionController"})
@Slf4j
@RestControllerAdvice
public class ExceptionController {

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ExceptionHandler(NoContentException.class)
  public ErrorResult noContentExHandler(NoContentException e) {
    return new ErrorResult(e.getHandleMessage().getCode());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(CustomApiException.class)
  public ErrorResult customExHandler(CustomApiException e) {
    return new ErrorResult(e.getErrorMessage().getCode());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ErrorResult badRequestExHandler(MissingServletRequestParameterException e) {
    return new ErrorResult("MissingServletRequestParameterException");
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ErrorResult sqlExHandler(DataIntegrityViolationException e) {
    return new ErrorResult("DataIntegrityViolationException");
  }


  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(MissingRequestCookieException.class)
  public ErrorResult cookieExHandler(MissingRequestCookieException e) {
    return new ErrorResult("MissingRequestCookieException");
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(ExpiredJwtException.class)
  public ErrorResult JwtDueExHandler(ExpiredJwtException e) {
    return new ErrorResult("ExpiredJwtException");
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(AuthorizationException.class)
  public ErrorResult authExHandler(AuthorizationException e) {
    return new ErrorResult("AuthorizationException");
  }
}
