package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.dto.user.GetAccountCheckDTO;
import com.bipa4.back_bipatv.entity.QAccounts;
import com.bipa4.back_bipatv.entity.QChannels;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public GetAccountCheckDTO getAccountCheckDto(UUID accountId) {
    QAccounts qAccounts = QAccounts.accounts;
    QChannels qChannels = QChannels.channels;

    return jpaQueryFactory.select(
            Projections.bean(
                GetAccountCheckDTO.class,
                qAccounts.accountId,
                qAccounts.loginId,
                qAccounts.name.as("userName"),
                qAccounts.profileUrl.as("userProfileUrl"),
                qAccounts.eMail,
                qChannels.channelId,
                qChannels.channelName,
                qChannels.profileUrl.as("channelProfileUrl")

            )
        ).from(qChannels)
        .leftJoin(qChannels.accounts, qAccounts)
        .where(qAccounts.accountId.eq(accountId)
        ).fetchOne();
  }
}
