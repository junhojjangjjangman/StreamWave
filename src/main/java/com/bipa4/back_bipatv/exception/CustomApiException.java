package com.bipa4.back_bipatv.exception;


import com.bipa4.back_bipatv.dataType.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CustomApiException extends RuntimeException {

  ErrorCode errorMessage;
}
