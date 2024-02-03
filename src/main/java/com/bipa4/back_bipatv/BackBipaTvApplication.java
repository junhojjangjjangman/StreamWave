package com.bipa4.back_bipatv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@ServletComponentScan
public class BackBipaTvApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackBipaTvApplication.class, args);
  }

}
