package com.agrimate.service.repository;

import com.agrimate.service.model.scan.Scan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ScanRepository extends JpaRepository<Scan, Long> {
    Page<Scan> findByAccountId(Long accountId, Pageable pageable);
    Page<Scan> findByAccountIdAndPredictedDisease(Long accountId, String disease, Pageable pageable);
    List<Scan> findByAccountIdOrderByCreatedAtAsc(Long accountId);

    @Query("select s from Scan s where s.latitude is not null and s.longitude is not null "
            + "and s.createdAt >= :since")
    List<Scan> findGeoTaggedSince(Instant since);

    List<Scan> findByCreatedAtAfter(Instant since);

    long countByPredictedDisease(String disease);

    @Query("select s.predictedDisease as disease, count(s) as total from Scan s group by s.predictedDisease")
    List<DiseaseCount> countGroupedByDisease();

    interface DiseaseCount {
        String getDisease();
        long getTotal();
    }
}
