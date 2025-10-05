package com.fintoc.logger.service;

import com.fintoc.logger.entity.NonceTracking;
import com.fintoc.logger.repository.NonceTrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NonceTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(NonceTrackingService.class);
    private static final int NONCE_EXPIRY_HOURS = 2; // Nonces expire after 2 hours
    
    @Autowired
    private NonceTrackingRepository nonceRepository;
    
    /**
     * Check if nonce has been used before and mark it as used
     * @param nonce The nonce to check
     * @return true if nonce is valid (not used before), false if already used
     */
    @Transactional
    public boolean validateAndMarkNonce(String nonce) {
        if (nonce == null || nonce.trim().isEmpty()) {
            logger.warn("Empty or null nonce provided");
            return false;
        }
        
        // Check if nonce already exists
        if (nonceRepository.existsByNonce(nonce)) {
            logger.warn("Nonce already used: {}", nonce);
            return false;
        }
        
        try {
            // Mark nonce as used
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusHours(NONCE_EXPIRY_HOURS);
            
            NonceTracking tracking = new NonceTracking(nonce, now, expiresAt);
            nonceRepository.save(tracking);
            
            logger.debug("Nonce marked as used: {}", nonce);
            return true;
            
        } catch (Exception e) {
            logger.error("Error saving nonce: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Cleanup expired nonces - runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 ms
    @Transactional
    public void cleanupExpiredNonces() {
        try {
            int deletedCount = nonceRepository.deleteExpiredNonces(LocalDateTime.now());
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired nonces", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error cleaning up expired nonces: {}", e.getMessage(), e);
        }
    }
}