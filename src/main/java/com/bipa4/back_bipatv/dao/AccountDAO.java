package com.bipa4.back_bipatv.dao;

import com.bipa4.back_bipatv.dataType.ErrorCode;
import com.bipa4.back_bipatv.entity.Accounts;
import com.bipa4.back_bipatv.exception.CustomApiException;
import com.bipa4.back_bipatv.repository.AccountRepository;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDAO {

//  @PersistenceContext
//  private EntityManager em;

  @Autowired
  private AccountRepository accountRepository;

//  public Long save(Accounts member) {
//    em.persist(member);
//    return member.getAccountId();
//  }
//
//  public Accounts find(Long id) {
//    return em.find(Accounts.class, id);
//  }

  public boolean findAccount(Accounts accounts) {
    Optional<Accounts> optAccount = accountRepository.findByLoginId(accounts.getLoginId());

    return optAccount.isPresent();
  }

  public Accounts selectAccount(Accounts accounts) {
    Optional<Accounts> optAccount = accountRepository.findByLoginId(accounts.getLoginId());
    //토큰이 유효하지만, account를 찾을 수 없는 경우.
    if (optAccount == null) {
      throw new CustomApiException(ErrorCode.USER_JOIN_ERROR);
    }
    return optAccount.orElse(null);
  }


  public Accounts selectAccountId(Accounts accounts) {
    Optional<Accounts> accounts1 = accountRepository.findById(accounts.getAccountId());

    return accounts1.orElse(null);
  }

  //  @Transactional
//  연산이 고립되어, 다른 연산과의 혼선으로 인해 잘못된 값을 가져오는 경우가 방지된다.
//  연산의 원자성이 보장되어, 연산이 도중에 실패할 경우 변경사항이 Commit되지 않는다.
  @Transactional
  public void createAccount(Accounts accounts) {
    accountRepository.save(accounts);
  }

}
