package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Logs, Long>, LogRepositoryCustom {

}
