package com.bipa4.back_bipatv.controller;

import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dto.video.GetFileUrlResponseDto;
import com.bipa4.back_bipatv.dto.video.GetSearchResponseDto;
import com.bipa4.back_bipatv.dto.video.GetUrlResponseDto;
import com.bipa4.back_bipatv.dto.video.PostUploadRequestDto;
import com.bipa4.back_bipatv.dto.video.PutUpdateRequestDto;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.PresignedUrlService;
import com.bipa4.back_bipatv.service.RecommendationService;
import com.bipa4.back_bipatv.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = {"VideoController"})
@RequestMapping("/video")
@Slf4j
@RequiredArgsConstructor
@Controller
public class VideoController {

  private final VideoService videoService;
  private final PresignedUrlService presignedUrlService;
  private final SecurityService securityService;
  private final RecommendationService recommendationService;

  // 본인 영상인지 확인
  @ApiOperation(value = "본인의 영상이 맞는지 확인", notes = "토큰을 통해 본인의 영상이 맞는지 확인 (삭제 또는 업로드 등애 사용)")
  @GetMapping("/check")
  @AccessTokenValid
  public ResponseEntity<Boolean> checkVideos(
      @CookieValue(name = "accessToken", required = false) String accessToken,
      @RequestParam("videoId") String videoId, HttpServletRequest request) {
    System.out.println(accessToken);
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.check(account, videoId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // 영상  삭제
  @ApiOperation(value = "영상 삭제", notes = "영상 삭제 진행")
  @AccessTokenValid
  @DeleteMapping("/{id}")
  public ResponseEntity<Boolean> deleteVideo(@PathVariable("id") String id,
      @CookieValue("accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.removeVideo(id, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // S3 presigned-url 발급
  @ApiOperation(value = "S3 presigned-image-url 발급", notes = "비디오 및 이미지 업로드를 위한 임시 url 발급")
  @PostMapping("/presigned/image")
  public ResponseEntity<GetFileUrlResponseDto> saveImage(
      @RequestParam("imageName") String imageName, @RequestParam("folderName") String folderName) {
    GetFileUrlResponseDto responseDto = presignedUrlService.getPreSignedUrl(imageName, folderName);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @ApiOperation(value = "S3 presigned-video-url 발급", notes = "비디오 및 이미지 업로드를 위한 임시 url 발급")
  @PostMapping("/presigned/video")
  public ResponseEntity<GetFileUrlResponseDto> saveVideo(
      @RequestParam("videoName") String videoName, @RequestParam("folderName") String folderName) {
    GetFileUrlResponseDto responseDto = presignedUrlService.getPreSignedUrl(videoName, folderName);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }


  // CDN presigned-url 발급
  @ApiOperation(value = "CDN presigned-url 발급", notes = "비디오 및 이미지 업로드를 위한 임시 url 발급")
  @PostMapping("/presigned-cdn")
  public ResponseEntity<GetUrlResponseDto> saveFileCDN(@RequestParam("videoName") String videoName,
      @RequestParam("imageName") String imageName) {
    GetUrlResponseDto responseDto = presignedUrlService.getPreSignedUrlCDN(videoName, imageName);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  // 영상 업로드
  @ApiOperation(value = "영상 업로드", notes = "영상 업로드 진행")
  @PostMapping("/upload")
  @AccessTokenValid
  public ResponseEntity<Boolean> upload(
      @RequestBody @ApiParam(value = "수정할 회원 정보", required = true) PostUploadRequestDto responseDto,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.uploadVideo(responseDto, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // 영상 수정
  @ApiOperation(value = "영상 수정", notes = "영상 수정 진행")
  @PutMapping("/{id}")
  @AccessTokenValid
  public ResponseEntity<Boolean> update(@PathVariable String id,
      @RequestBody PutUpdateRequestDto requestDto, @CookieValue("accessToken") String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.updateVideo(id, requestDto, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // 영상 검색
  @ApiOperation(value = "영상 검색", notes = "Full-text searches 진행")
  @GetMapping("/search")
  public ResponseEntity<List<GetSearchResponseDto>> search(
      @RequestParam("search_query") String searchQuery) {
    List<GetSearchResponseDto> responseDtos = videoService.search(searchQuery);
    if (responseDtos == null) {
      return new ResponseEntity<List<GetSearchResponseDto>>(responseDtos, HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<List<GetSearchResponseDto>>(responseDtos, HttpStatus.OK);
  }


  // 좋아요
  @ApiOperation(value = "좋아요", notes = "줗아요 버튼을 눌렀을 시")
  @AccessTokenValid
  @GetMapping("/detail/{videoId}/like")
  public ResponseEntity<Boolean> like(@PathVariable("videoId") String videoId,
      @CookieValue(name = "accessToken") String accessToken, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.like(videoId, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }


  // 좋아요 취소
  @ApiOperation(value = "좋아요 취소", notes = "줗아요 버튼을 다시 눌렀을 시")
  @DeleteMapping("/detail/{videoId}/like")
  @AccessTokenValid
  public ResponseEntity<Boolean> cancelLike(
      @ApiParam(value = "좋아요를 취소할 영상 아이디") @PathVariable("videoId") String videoId,
      @ApiParam(value = "좋아요를 취소할 유저의 토큰값") @CookieValue(name = "accessToken") String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.cancelLike(videoId, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // 조회수 상승
  @ApiOperation(value = "조회수 상승", notes = "조회수 상승")
  @PutMapping("/updateViews/{videoId}")
  public ResponseEntity<Boolean> getPlusViews(@PathVariable("videoId") String id,
      HttpServletRequest request, HttpServletResponse response) {
    Cookie oldCookie = null;
    boolean result = false;

    // cookie 값 추출
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("postView")) {
          oldCookie = cookie;
        }
      }
    }

    if (oldCookie != null) {
      if (!oldCookie.getValue().contains("[" + id.toString() + "]")) {
        result = videoService.plusViews(id);
        oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]");
        oldCookie.setPath("/");
        oldCookie.setMaxAge(60 * 60 * 24);
        response.addCookie(oldCookie);
      }
    } else {
      result = videoService.plusViews(id);
      Cookie newCookie = new Cookie("postView", "[" + id + "]");
      newCookie.setPath("/");
      newCookie.setMaxAge(60 * 60 * 24);
      response.addCookie(newCookie);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // 영상 추천 데이터 조회수 상승
  @ApiOperation(value = "영상 추천 데이터 조회수 상승", notes = "영상 추천 조회수 상승")
  @PutMapping("/updateRecommend/{videoId}")
  public ResponseEntity<Boolean> getPlusRecommend(@PathVariable("videoId") String id,
      @CookieValue(name = "accessToken", required = false) String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    boolean response = videoService.plusRecommend(id, account);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
