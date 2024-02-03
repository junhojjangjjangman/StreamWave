package com.bipa4.back_bipatv.exception;

public class JwtNotFoundException extends RuntimeException {

  private static final String MESSAGE = "유효한 TOKEN 값이 아닙니다";

  public JwtNotFoundException() {
    super(MESSAGE);
  }

}
