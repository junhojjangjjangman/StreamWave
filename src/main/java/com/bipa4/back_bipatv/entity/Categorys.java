package com.bipa4.back_bipatv.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Table(name = "Categorys")
public class Categorys {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Long categoryId;
  @ManyToOne
  @JoinColumn(name = "category_name_id")
  private CategoryName categoryNameId;
  @ManyToOne
  @JoinColumn(name = "video_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Videos videoId;
}
