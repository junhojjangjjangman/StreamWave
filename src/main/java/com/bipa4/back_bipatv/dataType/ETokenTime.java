package com.bipa4.back_bipatv.dataType;

public enum ETokenTime {
  ACCESSTOKEN_EXP_TIME(60 * 60 * 2),
  REFRESHTOKEN_EXP_TIME(60 * 60 * 6),
  ACCESSTOKEN_EXP_TIME_TEST(60),
  REFRESHTOKEN_EXP_TIME_TEST(60 * 5);
  private final int time;

  ETokenTime(int time) {
    this.time = time;
  }

  public int getTime() {
    return time;
  }
}
