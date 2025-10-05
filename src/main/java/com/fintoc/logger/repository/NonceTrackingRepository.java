package com.fintoc.logger.repository;

import com.fintoc.logger.entity.NonceTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NonceTrackingRepository extends JpaRepository<NonceTracking, String> {
    
    boolean existsByNonce(String nonce);
    
    @Modifying
    @Query("DELETE FROM NonceTracking n WHERE n.expiresAt < :now")
    int deleteExpiredNonces(@Param("now") LocalDateTime now);
}