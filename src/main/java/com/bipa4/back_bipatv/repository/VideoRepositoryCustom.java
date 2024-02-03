package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dto.video.GetCategoryNameRequestDto;
import com.bipa4.back_bipatv.dto.video.GetDetailResponseDto;
import com.bipa4.back_bipatv.dto.video.GetSearchVideoINChannelDTO;
import com.bipa4.back_bipatv.dto.video.GetVideoResponseDto;
import com.bipa4.back_bipatv.dto.video.PostUploadRequestDto;
import com.bipa4.back_bipatv.dto.video.PutUpdateRequestDto;
import com.bipa4.back_bipatv.entity.Accounts;
import java.util.List;
import java.util.UUID;
import org.apache.mahout.cf.taste.model.JDBCDataModel;

public interface VideoRepositoryCustom {

  List<GetVideoResponseDto> getAllVideos(String page, int pageSize);

  String lastUUID();

  String getNextUUID(String uuid);

  String getNextCategoryUUID(String uuid, UUID category);

  String lastCategoryUUID(UUID category);

  List<GetVideoResponseDto> findByCategory(UUID category, String page, int pageSize);

  List<GetVideoResponseDto> findByViews();

  boolean updateViews();

  GetDetailResponseDto getDetail(String id, JDBCDataModel dataModel);

  boolean plusViewsCount(String videoId, Accounts account);

  boolean remove(String id, Accounts account);

  boolean insert(PostUploadRequestDto videoResponseDto, Accounts account, UUID uuid, String ulid);

  boolean update(String id, PutUpdateRequestDto videoResponseDto, Accounts account);

  List<GetCategoryNameRequestDto> getCategoryNames();

  boolean checkOwner(Accounts account, String videoId);

  boolean plusViews(String videoId);

  boolean getFavorite(String videoId, Accounts account);

  boolean plusLike(String videoId, Accounts account);

  boolean minusLike(String videoId, Accounts account);

  List<GetVideoResponseDto> getVideosInChannel(UUID channelId, String uuid, int pageSize);

  List<GetVideoResponseDto> getVideosInMyChannel(UUID channelId, String uuid, int pageSize);

  List<GetSearchVideoINChannelDTO> getSearchVideoInMyChannel(UUID channelId, Integer currentPage,
      int pageSize,
      String searchQuery);

  Integer lastUUIDSearchVideoInChannel(UUID channelId, String searchQuery);

  List<GetSearchVideoINChannelDTO> getSearchVideoInChannel(UUID channelId, Integer currentPage,
      int pageSize,
      String searchQuery);

  String lastUUIDInChannel(UUID channelId);

  String lastUUIDInMyChannel(UUID channelId);

  Integer lastUUIDSearchVideoInMyChannel(UUID channelId, String searchQuery);
}
