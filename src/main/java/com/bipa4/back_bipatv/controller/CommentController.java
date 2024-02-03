package com.bipa4.back_bipatv.controller;

import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dto.comment.CommentRequest;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.CommentService;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  private final SecurityService securityService;

  // 부모 댓글 INSERT
  @PostMapping("/commentParent")
  @AccessTokenValid
  public ResponseEntity<Boolean> insertParentComment(@RequestBody CommentRequest commentRequest,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.saveParentComment(account, commentRequest);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // 자식 댓글 INSERT
  @PostMapping("/commentChild")
  @AccessTokenValid
  public ResponseEntity<Boolean> insertChildComment(@RequestBody CommentRequest commentRequest,
      @RequestParam("groupIndex") Integer groupIndex,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.saveChildComment(account, commentRequest, groupIndex);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // 댓글 UPDATE
  @PutMapping("/comment")
  @AccessTokenValid
  public ResponseEntity<Boolean> updateComment(@RequestBody CommentRequest commentRequest,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.updateComment(commentRequest, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // 부모 댓글 DELETE
  @DeleteMapping("/commentParent/{commentId}")
  @AccessTokenValid
  public ResponseEntity<Boolean> deleteParentComment(
      @PathVariable UUID commentId,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.deleteParentComment(commentId, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // 자식 댓글 DELETE
  @DeleteMapping("/commentChild/{commentId}")
  @AccessTokenValid
  public ResponseEntity<Boolean> deleteChildComment(
      @PathVariable UUID commentId,
      @CookieValue(name = "accessToken") String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.deleteChildComment(commentId, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // 댓글 고정됨
  @PutMapping("/{videoId}/comment-pick")
  @AccessTokenValid
  public ResponseEntity<Boolean> pickComment(@RequestBody CommentRequest commentRequest,
      @CookieValue(name = "accessToken") String accessToken, @PathVariable String videoId,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.pickComment(commentRequest, account, videoId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/{videoId}/comment-pick/{commentId}")
  @AccessTokenValid
  public ResponseEntity<Boolean> cancelPickComment(
      @CookieValue(name = "accessToken") String accessToken,
      @PathVariable String videoId, @PathVariable UUID commentId,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");

    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = commentService.cancelPickComment(commentId, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


}

