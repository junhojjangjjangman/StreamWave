package com.bipa4.back_bipatv.service;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class RecommendationService {

  private final DataSource dataSource;

  @Autowired
  public RecommendationService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public JDBCDataModel getDataModel() {
    try {
      JDBCDataModel dataModel = new MySQLJDBCDataModel(dataSource, "recommend", "account_id", "video_id", "rating", "");
      return dataModel;
    } catch (Exception e) {
      return null;
    }
  }
}
