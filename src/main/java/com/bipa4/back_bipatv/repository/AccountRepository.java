package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.entity.Accounts;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Accounts, UUID>,
    AccountRepositoryCustom { //<entity명,pk자료형>

  Optional<Accounts> findByLoginId(String loginId);

}
