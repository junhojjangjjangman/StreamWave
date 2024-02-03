package com.bipa4.back_bipatv.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Favorite")
@Data
public class Favorite {

  @EmbeddedId
  private FavoritePK favoritePK;
}
