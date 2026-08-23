package com.agrimate.service.repository;

import com.agrimate.service.model.farm.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByAccountId(Long accountId);
    Optional<Farm> findByIdAndAccountId(Long id, Long accountId);
}
