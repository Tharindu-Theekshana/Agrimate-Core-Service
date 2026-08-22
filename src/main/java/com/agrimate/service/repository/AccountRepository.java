package com.agrimate.service.repository;

import com.agrimate.service.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByPhone(String phone);
    Optional<Account> findByUserId(Long userId);
}
