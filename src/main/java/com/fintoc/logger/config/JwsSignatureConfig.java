package com.fintoc.logger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWS signature keys
 * Reads private and public keys from application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "jws.signature")
public class JwsSignatureConfig {
    
    private String privateKey;
    private String publicKey;
    
    // Getters and Setters
    public String getPrivateKey() {
        return privateKey;
    }
    
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    /**
     * Get private key without PEM headers/footers
     */
    public String getPrivateKeyContent() {
        if (privateKey == null) return null;
        return privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
    }
    
    /**
     * Get public key without PEM headers/footers
     */
    public String getPublicKeyContent() {
        if (publicKey == null) return null;
        return publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
    }
}