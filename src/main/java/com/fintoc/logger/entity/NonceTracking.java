package com.fintoc.logger.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nonce_tracking")
public class NonceTracking {
    
    @Id
    @Column(name = "nonce", unique = true, nullable = false, length = 64)
    private String nonce;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    public NonceTracking() {}
    
    public NonceTracking(String nonce, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.nonce = nonce;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
    
    // Getters and setters
    public String getNonce() {
        return nonce;
    }
    
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}