package com.bipa4.back_bipatv.dataType;

public enum ELogin_Type {
  GOOGLE("구글"),
  KAKAO("카카오"),
  NAVER("네이버");

  private final String LoginType;

  ELogin_Type(String LoginType) {
    this.LoginType = LoginType;
  }

  public String getLoginType() {
    return LoginType;
  }
}
