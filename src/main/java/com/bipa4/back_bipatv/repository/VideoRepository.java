package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.entity.Videos;
import com.github.f4b6a3.ulid.Ulid;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Videos, String>,
    VideoRepositoryCustom {

}
