//package com.bipa4.back_bipatv;
//
//import com.bipa4.back_bipatv.dao.AccountDAO;
//import com.bipa4.back_bipatv.entity.Accounts;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//
//import javax.transaction.Transactional;
//
//@DataJpaTest
//public class RepositoryTest {
//
//    //mockito
//    @Autowired
//    AccountDAO accountDAO;
//
//    @Test
//    @Transactional
//    @Rollback(false)
//    public void testMember() throws Exception {
//        //given
//        Accounts memberDTO = new Accounts();
//        memberDTO.setName("memberA");
//
//        //when
//        Long saveId = accountDAO.save(memberDTO);
//        Accounts findMemberDTO = accountDAO.find(saveId);
//
//        //then
//        Assertions.assertThat(findMemberDTO.getId()).isEqualTo(memberDTO.getId());
//        Assertions.assertThat(findMemberDTO.getName()).isEqualTo(memberDTO.getName());
//    }
//}
