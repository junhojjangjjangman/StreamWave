package com.bipa4.back_bipatv.repository;

import com.bipa4.back_bipatv.entity.Channels;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<Channels, UUID>, ChannelRepositoryCustom {

  @Query(value = "select a.* "
      + "from channels a "
      + "join accounts b "
      + "on a.account_id = b.account_id "
      + "where b.account_id = :accountId", nativeQuery = true)
  Optional<Channels> findByChannelToAccountId(UUID accountId);


  Channels findByChannelId(UUID channelId);

  Optional<Channels> findByChannelName(String ChannelName);
}
