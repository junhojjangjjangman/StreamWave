package com.bipa4.back_bipatv.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Embeddable
public class FavoritePK implements Serializable {

  @MapsId("video_id")
  @ManyToOne
  @JoinColumn(name = "video_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Videos videos;

  @MapsId("account_id")
  @ManyToOne
  @JoinColumn(name = "account_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Accounts accounts;
}
