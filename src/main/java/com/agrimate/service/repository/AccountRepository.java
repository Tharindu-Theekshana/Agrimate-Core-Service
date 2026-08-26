package com.agrimate.service.repository;

import com.agrimate.service.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByPhone(String phone);
    Optional<Account> findByUserId(Long userId);

    @Query(value = "SELECT * FROM accounts a WHERE jsonb_exists(a.device_tokens, :token)", nativeQuery = true)
    Optional<Account> findByDeviceToken(@Param("token") String token);
}
