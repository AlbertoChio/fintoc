package com.fintoc.logger.service;

import com.fintoc.logger.config.JwsSignatureConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service for JWS signature operations
 * Uses RSA keys configured in application.yml
 */
@Service
public class JwsSignatureService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwsSignatureService.class);
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300; // 5 minutes
    private static final long MAX_TIMESTAMP_AGE_SECONDS = 3600; // 1 hour
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private NonceTrackingService nonceTrackingService;
    private static final String KEY_ALGORITHM = "RSA";
    
    private final JwsSignatureConfig jwsConfig;
    
    @Autowired
    public JwsSignatureService(JwsSignatureConfig jwsConfig) {
        this.jwsConfig = jwsConfig;
    }
    
    /**
     * Sign data using the private key from configuration
     */
    public String signData(String data) {
        try {
            PrivateKey privateKey = getPrivateKey();
            
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes("UTF-8"));
            
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
            
        } catch (Exception e) {
            logger.error("Error signing data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sign data", e);
        }
    }
    
    /**
     * Verify signature using the public key from configuration
     */
    public boolean verifySignature(String data, String signatureToVerify) {
        try {
            PublicKey publicKey = getPublicKey();
            
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes("UTF-8"));
            
            byte[] signatureBytes = Base64.getDecoder().decode(signatureToVerify);
            return signature.verify(signatureBytes);
            
        } catch (Exception e) {
            logger.error("Error verifying signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create JWS header for Fintoc API calls - EXACTLY matches JavaScript implementation
     */
    public String createJwsHeader(String rawBody) {
        try {
            // Generate nonce and timestamp exactly like JS version
            String nonce = generateNonce(); // 32 hex chars from 16 random bytes
            long timestamp = System.currentTimeMillis() / 1000;
            
            // JWS Header matching JS exactly - includes nonce, ts, and crit
            String header = String.format(
                "{\"alg\":\"RS256\",\"nonce\":\"%s\",\"ts\":%d,\"crit\":[\"ts\",\"nonce\"]}", 
                nonce, timestamp
            );
            
            // Base64url encode header and payload (matches JS Buffer.toString('base64url'))
            String protectedBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes("UTF-8"));
            String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rawBody.getBytes("UTF-8"));
            
            // Create signing input exactly like JS: `${protectedBase64}.${payloadBase64}`
            String signingInput = protectedBase64 + "." + payloadBase64;
            
            // Debug output like JS version
            logger.info("Signing input: {}", signingInput);
            logger.info("Payload: {}", rawBody);
            
            // Sign using RSA-SHA256 with PKCS1 padding (matches JS crypto.createSign('sha256'))
            PrivateKey privateKey = getPrivateKey();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signingInput.getBytes("UTF-8"));
            
            byte[] signatureBytes = signature.sign();
            String signatureBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(signatureBytes);
            
            // Create complete token for debugging (like JS)
            String completeToken = protectedBase64 + "." + payloadBase64 + "." + signatureBase64;
            logger.info("Token: {}", completeToken);
            
            // Return detached JWS format: header.signature (exactly like JS)
            return protectedBase64 + "." + signatureBase64;
            
        } catch (Exception e) {
            logger.error("Error creating JWS header: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create JWS header", e);
        }
    }
    
    /**
     * Create COMPLETE JWS token (header.payload.signature) - EXACTLY matches JavaScript 
     * This includes the payload so it can be decoded later without needing the original body
     */
    public String createCompleteJws(String rawBody) {
        try {
            // Generate nonce and timestamp exactly like JS version
            String nonce = generateNonce(); // 32 hex chars from 16 random bytes
            long timestamp = System.currentTimeMillis() / 1000;
            
            // JWS Header matching JS exactly - includes nonce, ts, and crit
            String header = String.format(
                "{\"alg\":\"RS256\",\"nonce\":\"%s\",\"ts\":%d,\"crit\":[\"ts\",\"nonce\"]}", 
                nonce, timestamp
            );
            
            // Base64url encode header and payload exactly like JS
            String protectedBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes("UTF-8"));
            String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rawBody.getBytes("UTF-8"));
            
            // Create signing input exactly like JS: `${protectedBase64}.${payloadBase64}`
            String signingInput = protectedBase64 + "." + payloadBase64;
            
            // Sign using RSA-SHA256 with PKCS1 padding (matches JS crypto.createSign('sha256'))
            PrivateKey privateKey = getPrivateKey();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signingInput.getBytes("UTF-8"));
            
            byte[] signatureBytes = signature.sign();
            String signatureBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(signatureBytes);
            
            // Return complete JWS format: header.payload.signature
            String completeToken = protectedBase64 + "." + payloadBase64 + "." + signatureBase64;
            
            // Debug output like JS version
            logger.info("Token: {}", completeToken);
            logger.info("Payload: {}", rawBody);
            
            return completeToken;
            
        } catch (Exception e) {
            logger.error("Error creating complete JWS: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create complete JWS", e);
        }
    }
    
    /**
     * Validate JWS timestamp to prevent replay attacks
     */
    public boolean validateTimestamp(long jwsTimestamp) {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        
        // Check if timestamp is too far in the future (clock skew tolerance)
        if (jwsTimestamp > currentTimestamp + TIMESTAMP_TOLERANCE_SECONDS) {
            logger.warn("JWS timestamp is too far in the future: {} vs current: {}", 
                jwsTimestamp, currentTimestamp);
            return false;
        }
        
        // Check if timestamp is too old
        if (jwsTimestamp < currentTimestamp - MAX_TIMESTAMP_AGE_SECONDS) {
            logger.warn("JWS timestamp is too old: {} vs current: {}", 
                jwsTimestamp, currentTimestamp);
            return false;
        }
        
        return true;
    }
    
    /**
     * Verify JWS signature using public key
     */
    public boolean verifyJwsSignature(String jwsToken, String originalBody) {
        try {
            String[] parts = jwsToken.split("\\.");
            if (parts.length < 2) {
                logger.error("Invalid JWS format: insufficient parts");
                return false;
            }
            
            String headerBase64 = parts[0];
            String signatureBase64 = parts[parts.length - 1];
            
            // For detached signature, reconstruct signing input
            String signingInput;
            if (parts.length == 2) {
                // Detached format: header.signature
                if (originalBody == null) {
                    logger.error("Original body required for detached JWS verification");
                    return false;
                }
                String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(originalBody.getBytes("UTF-8"));
                signingInput = headerBase64 + "." + payloadBase64;
            } else {
                // Complete format: header.payload.signature
                String payloadBase64 = parts[1];
                signingInput = headerBase64 + "." + payloadBase64;
            }
            
            // Verify signature
            PublicKey publicKey = getPublicKey();
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(signingInput.getBytes("UTF-8"));
            
            byte[] signatureBytes = Base64.getUrlDecoder().decode(signatureBase64);
            boolean isValid = signature.verify(signatureBytes);
            
            if (isValid) {
                // Also validate timestamp if present
                String headerJson = new String(Base64.getUrlDecoder().decode(headerBase64), "UTF-8");
                JsonNode headerNode = objectMapper.readTree(headerJson);
                if (headerNode.has("ts")) {
                    long timestamp = headerNode.get("ts").asLong();
                    if (!validateTimestamp(timestamp)) {
                        logger.warn("JWS signature valid but timestamp validation failed");
                        return false;
                    }
                }
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying JWS signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Comprehensive JWS validation including signature, timestamp, and nonce
     */
    public boolean validateJwsComprehensive(String jwsToken, String originalBody) {
        try {
            String[] parts = jwsToken.split("\\.");
            if (parts.length < 2) {
                logger.error("Invalid JWS format: insufficient parts");
                return false;
            }
            
            // Extract and validate header
            String headerBase64 = parts[0];
            String headerJson = new String(Base64.getUrlDecoder().decode(headerBase64), "UTF-8");
            JsonNode headerNode = objectMapper.readTree(headerJson);
            
            // 1. Validate timestamp if present
            if (headerNode.has("ts")) {
                long timestamp = headerNode.get("ts").asLong();
                if (!validateTimestamp(timestamp)) {
                    logger.warn("JWS timestamp validation failed");
                    return false;
                }
            }
            
            // 2. Validate nonce if present (and mark as used)
            if (headerNode.has("nonce")) {
                String nonce = headerNode.get("nonce").asText();
                if (!nonceTrackingService.validateAndMarkNonce(nonce)) {
                    logger.warn("JWS nonce validation failed - possibly replay attack");
                    return false;
                }
            }
            
            // 3. Validate signature
            boolean signatureValid = verifyJwsSignature(jwsToken, originalBody);
            if (!signatureValid) {
                logger.warn("JWS signature validation failed");
                return false;
            }
            
            logger.info("JWS comprehensive validation passed");
            return true;
            
        } catch (Exception e) {
            logger.error("Error in comprehensive JWS validation: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generate random nonce exactly like JS: crypto.randomBytes(16).toString('hex')
     * Returns 32 hex characters from 16 random bytes
     */
    private String generateNonce() {
        byte[] nonceBytes = new byte[16]; // Exactly 16 bytes like JS
        new java.security.SecureRandom().nextBytes(nonceBytes);
        StringBuilder nonce = new StringBuilder();
        for (byte b : nonceBytes) {
            nonce.append(String.format("%02x", b)); // Lowercase hex like JS
        }
        return nonce.toString(); // Returns 32 hex characters
    }
    
    /**
     * Get private key from configuration
     */
    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyContent = jwsConfig.getPrivateKeyContent();
        if (privateKeyContent == null || privateKeyContent.isEmpty()) {
            throw new IllegalStateException("Private key not configured");
        }
        
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }
    
    /**
     * Get public key from configuration
     */
    private PublicKey getPublicKey() throws Exception {
        String publicKeyContent = jwsConfig.getPublicKeyContent();
        if (publicKeyContent == null || publicKeyContent.isEmpty()) {
            throw new IllegalStateException("Public key not configured");
        }
        
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }
}