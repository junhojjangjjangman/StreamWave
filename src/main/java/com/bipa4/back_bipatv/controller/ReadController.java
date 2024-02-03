package com.bipa4.back_bipatv.controller;


import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dto.channel.GetChannelDTO;
import com.bipa4.back_bipatv.dto.channel.GetChannelTop5DTO;
import com.bipa4.back_bipatv.dto.channel.GetInfiniteScrollRequestChannelDto;
import com.bipa4.back_bipatv.dto.channel.SelectChannelDTO;
import com.bipa4.back_bipatv.dto.comment.ChildCommentResponse;
import com.bipa4.back_bipatv.dto.comment.CommentResponse;
import com.bipa4.back_bipatv.dto.video.GetCategoryNameRequestDto;
import com.bipa4.back_bipatv.dto.video.GetDetailResponseDto;
import com.bipa4.back_bipatv.dto.video.GetInfiniteScrollRequestDto;
import com.bipa4.back_bipatv.dto.video.GetVideoResponseDto;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.ChannelService;
import com.bipa4.back_bipatv.service.CommentService;
import com.bipa4.back_bipatv.service.RecommendationService;
import com.bipa4.back_bipatv.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = {"ReadController"})
@RequiredArgsConstructor
@Controller
public class ReadController {

  private final VideoService videoService;
  private final CommentService commentService;
  private final ChannelService channelService;
  private final SecurityService securityService;
  private final RecommendationService recommendationService;


  // 전체 조회 (최신 순으로)
  @ApiOperation(value = "전체 조회", notes = "최신순으로 전체 조회 (무한스크롤) / 처음엔 page 안넘겨주면 됨.")
  @GetMapping("/video/latest")
  public ResponseEntity<GetInfiniteScrollRequestDto> getAllVideos(
      @RequestParam(value = "page", required = false) String page,
      @RequestParam("pageSize") int pageSize) {
    String nextUlid = null;

    List<GetVideoResponseDto> videos = videoService.getAllVideos(page, pageSize);

    if (!videos.isEmpty()) {
      nextUlid = videoService.getNextUUID(
          videos.get(videos.size() - 1).getVideoId()); // 마지막 page의 UUID 호출
    }
    GetInfiniteScrollRequestDto responseDto = new GetInfiniteScrollRequestDto(videos, nextUlid);
    return ResponseEntity.ok().body(responseDto);
  }


  // 카테고리별 조회
  @ApiOperation(value = "카테고리별 전체 조회", notes = "카테고리 별 전체 조회 (무한스크롤)")
  @GetMapping("/video/category/{category}")
  public ResponseEntity<GetInfiniteScrollRequestDto> getCategoryVideos(
      @PathVariable("category") UUID category,
      @RequestParam(value = "page", required = false) String page,
      @RequestParam("pageSize") int pageSize) {
    String nextUlid = null;

    List<GetVideoResponseDto> videos = videoService.getCategoryVideos(category, page, pageSize);

    if (!videos.isEmpty()) {
      nextUlid = videoService.getNextCategoryUUID(videos.get(videos.size() - 1).getVideoId(),
          category); // 마지막 page의 UUID 호출
    }
    GetInfiniteScrollRequestDto responseDto = new GetInfiniteScrollRequestDto(videos, nextUlid);
    return ResponseEntity.ok().body(responseDto);
  }


  // 카테고리 이름 리스트 조회
  @ApiOperation(value = "카테고리 리스트 조회", notes = "카테고리 메뉴 등을 위한 카테고리 이름 추출")
  @GetMapping("/video/category")
  public ResponseEntity<List<GetCategoryNameRequestDto>> getCategoryNames() {
    List<GetCategoryNameRequestDto> categorys = videoService.getCategoryNames();
    return new ResponseEntity<List<GetCategoryNameRequestDto>>(categorys, HttpStatus.OK);
  }


  // 조회수 급상승 TOP 10
  @ApiOperation(value = "조회수 급상승 TOP 10", notes = "1시간마다 조회수가 급상승된 영상 10개 추출")
  @GetMapping("/video/top10")
  public ResponseEntity<List<GetVideoResponseDto>> getViewsTop10Videos() {
    List<GetVideoResponseDto> videos = videoService.getViewsTop10Videos();
    return new ResponseEntity<List<GetVideoResponseDto>>(videos, HttpStatus.OK);
  }


  // 디비 1시간 전 정보 저장
  @ApiOperation(value = "조회수 급상승 TOP 10", notes = "1시간마다 조회수가 급상승된 영상 10개 추출")
  @Scheduled(cron = "0 0 0/1 * * *")
  public ResponseEntity<Boolean> getViewsUpdate() {
    return new ResponseEntity<Boolean>(videoService.updateViews(), HttpStatus.OK);
  }


  // 영상 상세 조회
  @ApiOperation(value = "영상 상세 조회", notes = "영상 클릭시 상세 정보 + 추천 영상 추출")
  @GetMapping("/video/detail/{videoId}")
  public ResponseEntity<GetDetailResponseDto> getVideoDetail(@PathVariable("videoId") String id) {
    JDBCDataModel dataModel = recommendationService.getDataModel();
    GetDetailResponseDto video = videoService.getVideoDetail(id, dataModel);
    return new ResponseEntity<>(video, HttpStatus.OK);
  }


  // 영상 좋아요 여부
  @ApiOperation(value = "영상 상세 조회 및 추천 영상 조회", notes = "영상 클릭시 상세 정보 + 추천 영상 추출")
  @AccessTokenValid
  @GetMapping("/video/like/{videoId}")
  public ResponseEntity<Boolean> getVideoLike(@PathVariable("videoId") String id,
      @CookieValue(name = "accessToken", required = false) String accessToken,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts account = securityService.getSubjectAccount(accessToken);
    return new ResponseEntity<>(videoService.getLike(id, account), HttpStatus.OK);
  }

  //부모 댓글 조회 최신순
  @ApiOperation(value = "부모 댓글 조회 최신순", notes = "부모 댓글 조회 최신순")
  @GetMapping("/comment/{videoId}/comment-parent/new")
  public ResponseEntity<List<CommentResponse>> findParentComments(@PathVariable String videoId,
      CommentResponse commentResponse) {
    List<CommentResponse> responseDtos = commentService.findParentComments(videoId);
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }

  //부모 댓글 조회 오래된순
  @ApiOperation(value = "부모 댓글 조회 오래된순", notes = "부모 댓글 조회 오래된순")
  @GetMapping("/comment/{videoId}/comment-parent/old")
  public ResponseEntity<List<CommentResponse>> findOldParentComments(@PathVariable String videoId,
      CommentResponse commentResponse) {
    List<CommentResponse> responseDtos = commentService.findOldParentComments(videoId);
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }

  //부모 댓글 조회 인기순
  @ApiOperation(value = "부모 댓글 조회 인기순", notes = "부모 댓글 조회 인기순")
  @GetMapping("/comment/{videoId}/comment-parent/popularity")
  public ResponseEntity<List<CommentResponse>> findPopularityParentComments(
      @PathVariable String videoId,
      CommentResponse commentResponse) {
    List<CommentResponse> responseDtos = commentService.findPopularityParentComments(videoId);
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }

  //자식 댓글 조회
  @ApiOperation(value = "자식 댓글 조회", notes = "자식 댓글 조회")
  @GetMapping("/comment/{videoId}/{groupIndex}/comment-child")
  public ResponseEntity<List<ChildCommentResponse>> findChildComments(@PathVariable String videoId,
      @PathVariable int groupIndex) {
    List<ChildCommentResponse> responseDtos = commentService.findChildComments(videoId, groupIndex);
    return new ResponseEntity<>(responseDtos, HttpStatus.OK);
  }


  // 나의 채널 정보 조회
  @ApiOperation(value = "채널 상세 조회", notes = "채널 상세 조회")
  @GetMapping("/channel/{channelId}")
  public ResponseEntity<SelectChannelDTO> getMyChannelInfo(
      @PathVariable("channelId") UUID channelId) {
    SelectChannelDTO responseDto = channelService.findChannel(channelId);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }


  // 인기 채널 top5 조회
  @ApiOperation(value = "실시간 인기 채널 5", notes = "가장 인기 있는 채널 TOP5을 들고온다")
  @GetMapping("/channel/top5")
  public ResponseEntity<List<GetChannelTop5DTO>> getViewsTop5Channels() {
    List<GetChannelTop5DTO> channels = channelService.findLimitTimeSumCnt();
    return new ResponseEntity<List<GetChannelTop5DTO>>(channels, HttpStatus.OK);
  }


  // 전체 채널 조회
  @ApiOperation(value = "전체 채널 조회", notes = "전체 채널에 대한 정보")
  @GetMapping("/channel/AllChannel")
  public ResponseEntity<GetInfiniteScrollRequestChannelDto> getAllChannels(
      @RequestParam(value = "page", required = false) UUID page,
      @RequestParam("pageSize") int pageSize) {
    UUID nextUUID = null;

    List<GetChannelDTO> list = channelService.getAllChannels(page, pageSize);

    if (!list.isEmpty()) {
      nextUUID = channelService.getChannelNextUUID(list.get(list.size() - 1).getChannelId());
    }
    GetInfiniteScrollRequestChannelDto responseDto = new GetInfiniteScrollRequestChannelDto(list,
        nextUUID);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }


  @ApiOperation(value = "채널 내 영상 조회", notes = "최신순으로 전체 조회 (무한스크롤) / 처음엔 page 안넘겨주면 됨.")
  @AccessTokenValid
  @GetMapping("/channel/video/{channelId}")
  public ResponseEntity<GetInfiniteScrollRequestDto> getVideosInChannel(
      @CookieValue(value = "accessToken", required = false) String accessToken,
      @RequestParam(value = "page", required = false) String page,
      @RequestParam("pageSize") int pageSize, @PathVariable("channelId") UUID channelId,
      HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }

    String nextUUID = null;

    Accounts account = securityService.getSubjectAccount(accessToken);
    List<GetVideoResponseDto> videos = channelService.getVideosInChannel(account, channelId, page,
        pageSize);

    if (!videos.isEmpty()) {
      nextUUID = channelService.getNextChannelVideoUUID(videos.get(videos.size() - 1).getVideoId(),
          channelId, account); // 마지막 page의 UUID 호출
    }
    GetInfiniteScrollRequestDto responseDto = new GetInfiniteScrollRequestDto(videos, nextUUID);
    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }
}
