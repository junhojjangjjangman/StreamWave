package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dto.user.GetAccountCheckDTO;
import java.util.UUID;

public interface AccountRepositoryCustom {

  GetAccountCheckDTO getAccountCheckDto(UUID AccountId);
}
