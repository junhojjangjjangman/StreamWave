package com.bipa4.back_bipatv.controller;


import com.bipa4.back_bipatv.aspect.AccessTokenValid;
import com.bipa4.back_bipatv.dto.channel.GetInfiniteScrollSearchChannelDTO;
import com.bipa4.back_bipatv.dto.channel.GetSearchChannelDTO;
import com.bipa4.back_bipatv.dto.channel.PutChannelDTO;
import com.bipa4.back_bipatv.dto.video.GetInfiniteScrollSearchVideoInChannelDTO;
import com.bipa4.back_bipatv.dto.video.GetSearchVideoINChannelDTO;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.entity.Channels;
import com.bipa4.back_bipatv.security.SecurityService;
import com.bipa4.back_bipatv.service.ChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"ChannelController"})
@RequestMapping("/channel")
@RequiredArgsConstructor
@RestController
public class ChannelController {

  private final ChannelService channelService;
  private final SecurityService securityService;


  @ApiOperation(value = "updateMyChannel", notes = "채널 정보 수정")
  @AccessTokenValid
  @PutMapping("/{channelId}")
  public ResponseEntity<Channels> updateMyChannelInfo(@PathVariable UUID channelId,
      @CookieValue(value = "accessToken", required = false) String code,
      @RequestBody PutChannelDTO putChannelDTO, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      code = nat;
    }
    Accounts loginAccount = securityService.getSubjectAccount(code);
    Channels putChannel = channelService.findbyChannelId(channelId);
    Channels updatedChannel = channelService.updateChannel(loginAccount, putChannel, putChannelDTO);

    return new ResponseEntity<>(updatedChannel, HttpStatus.OK);
  }


  @ApiOperation(value = "채널 내 영상 검색", notes = "채널 안의 영상 검색")
  @AccessTokenValid
  @GetMapping("/{channelId}/video")
  public ResponseEntity<GetInfiniteScrollSearchVideoInChannelDTO> searchVideoInChannel(
      @CookieValue(value = "accessToken", required = false) String accessToken,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam("page_size") int pageSize, @RequestParam("search_query") String searchQuery,
      @PathVariable("channelId") UUID channelId, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Integer nextRank = null;

    Accounts loginAccount = securityService.getSubjectAccount(accessToken);
    List<GetSearchVideoINChannelDTO> videos = channelService.searchVideoInChannel(loginAccount,
        channelId, page, pageSize, searchQuery);

    if (!videos.isEmpty()) {
      nextRank =
          channelService.getSearchNextChannelVideoUUID(videos.get(videos.size() - 1).getRanking(),
              channelId, searchQuery, loginAccount, pageSize) == null ? null : page + 1;
    }

    GetInfiniteScrollSearchVideoInChannelDTO responseDto = new GetInfiniteScrollSearchVideoInChannelDTO(
        videos, nextRank);

    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @ApiOperation(value = "채널 검색", notes = "채널 검색")
  @GetMapping("/search")
  public ResponseEntity<GetInfiniteScrollSearchChannelDTO> searchChannel(
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam("page_size") int pageSize, @RequestParam("search_query") String searchQuery) {
    Integer nextRank = null;

    List<GetSearchChannelDTO> channels = channelService.searchChannel(page, pageSize, searchQuery);

    if (!channels.isEmpty()) {
      nextRank = channelService.getNextChannelRank(searchQuery,
          channels.get(channels.size() - 1).getRanking(), pageSize, page);
    }

    GetInfiniteScrollSearchChannelDTO responseDto = new GetInfiniteScrollSearchChannelDTO(channels,
        nextRank);

    return new ResponseEntity<>(responseDto, HttpStatus.OK);
  }

  @ApiOperation(value = "getUpdateFlag", notes = "업데이트 플레그 얻기")
  @AccessTokenValid
  @GetMapping("/flag/{channelId}")
  public ResponseEntity<Boolean> getUpdateFlag(
      @CookieValue(value = "accessToken", required = false) String accessToken,
      @PathVariable("channelId") UUID channelId, HttpServletRequest request) {
    String nat = (String) request.getAttribute("newAccessToken");
    if (nat != null) {
      accessToken = nat;
    }
    Accounts loginAccount = securityService.getSubjectAccount(accessToken);
    return new ResponseEntity<>(channelService.getUpdateFlag(loginAccount, channelId),
        HttpStatus.OK);
  }

  @ApiOperation(value = "채널명 중복 확인", notes = "채널명 중복 확인")
  @GetMapping("/check/name")
  public ResponseEntity<Boolean> getCheckChannelName(
      @RequestParam("channelName") String channelName) {
    return new ResponseEntity<>(channelService.getChannelNameCheck(channelName), HttpStatus.OK);
  }
}
