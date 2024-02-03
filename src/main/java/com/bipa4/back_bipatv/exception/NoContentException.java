package com.bipa4.back_bipatv.exception;

import com.bipa4.back_bipatv.dataType.HandleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class NoContentException extends RuntimeException {

  HandleCode handleMessage;
}


