package com.bipa4.back_bipatv.aspect;

import com.bipa4.back_bipatv.service.LogService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {


  private final LogService logService;


  @AfterReturning(value = "@annotation(org.springframework.web.bind.annotation.PutMapping)", returning = "returnData")
  public void putLogAfterReturningRequest(JoinPoint joinPoint, ResponseEntity returnData) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    logService.saveLog(request, returnData.getStatusCode()+"", "수정");
  }
  @AfterThrowing(value = "@annotation(org.springframework.web.bind.annotation.PutMapping)",throwing = "exception")
  public void putLogAfterThrowingRequest(JoinPoint joinPoint, Exception exception) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    logService.saveLog(request, exception.getMessage(), "수정");
  }

  @AfterReturning(value = "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",returning = "returnData")
  public void deleteLogAfterReturningRequest(JoinPoint joinPoint, ResponseEntity returnData) {
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      logService.saveLog(request, returnData.getStatusCode()+"", "삭제");
  }
  @AfterThrowing(value = "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",throwing = "exception")
  public void deleteLogAfterThrowingRequest(JoinPoint joinPoint, Exception exception) {

    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    logService.saveLog(request, exception.getMessage(), "삭제");
  }

  @AfterReturning(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)", returning = "returnData")
  public void insertLogAfterRequest(JoinPoint joinPoint, ResponseEntity returnData) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    if (!request.getRequestURI().contains("logout")) {
      logService.saveLog(request, returnData.getStatusCode()+"", "요청");
    }
  }
  @AfterThrowing(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)", throwing = "exception")
  public void insertLogAfterRequest(JoinPoint joinPoint, Exception exception) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    if (!request.getRequestURI().contains("logout")) {
      logService.saveLog(request, exception.getMessage(), "요청");
    }
  }
}
