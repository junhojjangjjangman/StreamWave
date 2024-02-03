package com.bipa4.back_bipatv.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

public interface ChildCommentResponse {

    String getCommentId();
    int getGroupIndex();
    String getChannelProfileUrl();
    String getChannelId();
    String getChannelName();
    String getContent();
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date getCreateAt();
    Boolean getIsUpdated();

}
