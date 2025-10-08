package com.fintoc.logger.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintoc.logger.entity.WebhookLog;
import com.fintoc.logger.repository.WebhookLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Service for handling Fintoc webhook processing and signature verification
 */
@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${fintoc.webhook.secret:}")
    private String webhookSecret;

    @Value("${fintoc.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final WebhookLogRepository webhookLogRepository;

    @Autowired
    public WebhookService(ObjectMapper objectMapper, WebhookLogRepository webhookLogRepository) {
        this.objectMapper = objectMapper;
        this.webhookLogRepository = webhookLogRepository;
    }

    /**
     * Verify Fintoc webhook signature
     * Expected format: t=timestamp,v1=signature
     */
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isEmpty()) {
            logger.warn("Missing fintoc-signature header");
            return false;
        }

        try {
            // Parse signature header: t=timestamp,v1=signature
            String[] parts = signatureHeader.split(",");
            String timestamp = null;
            String signature = null;

            for (String part : parts) {
                if (part.startsWith("t=")) {
                    timestamp = part.substring(2);
                } else if (part.startsWith("v1=")) {
                    signature = part.substring(3);
                }
            }

            if (timestamp == null || signature == null) {
                logger.warn("Invalid signature header format: {}", signatureHeader);
                return false;
            }

            // Create the signed payload: timestamp + . + payload
            String signedPayload = timestamp + "." + payload;

            // Use webhook secret if configured, otherwise use API key
            String secretToUse = (webhookSecret != null && !webhookSecret.isEmpty()) ? webhookSecret : apiKey;
            
            if (secretToUse == null || secretToUse.isEmpty()) {
                logger.error("No webhook secret or API key configured for signature verification");
                return false;
            }

            // Calculate expected signature
            String expectedSignature = calculateHmacSha256(signedPayload, secretToUse);

            logger.debug("Using secret type: {}, Signed payload: {}", 
                (webhookSecret != null && !webhookSecret.isEmpty()) ? "webhook-secret" : "api-key", 
                signedPayload);

            // Compare signatures (constant time comparison to prevent timing attacks)
            boolean isValid = constantTimeEquals(signature, expectedSignature);

            if (!isValid) {
                logger.warn("Webhook signature mismatch. Expected: {}, Got: {}", expectedSignature, signature);
                logger.warn("Signed payload was: {}", signedPayload);
                logger.warn("Secret used: {} characters", secretToUse.length());
            } else {
                logger.info("Webhook signature verification successful using: {}", 
                    (webhookSecret != null && !webhookSecret.isEmpty()) ? "webhook-secret" : "api-key");
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Process webhook event and store in database
     */
    public boolean processWebhook(JsonNode webhookData, String signature, String rawBody) {
        try {
            // Extract event details
            String eventId = webhookData.path("id").asText();
            String eventType = webhookData.path("type").asText();
            String mode = webhookData.path("mode").asText();
            String createdAt = webhookData.path("created_at").asText();
            
            logger.info("Processing webhook event: {} of type: {} created at: {}", eventId, eventType, createdAt);

            // Extract data section
            JsonNode dataSection = webhookData.path("data");
            String accountVerificationId = dataSection.path("id").asText();
            String status = dataSection.path("status").asText();
            String reason = dataSection.path("reason").asText(null);
            String receiptUrl = dataSection.path("receipt_url").asText(null);
            String transferId = dataSection.path("transfer_id").asText(null);
            String transactionDate = dataSection.path("transaction_date").asText(null);

            // Extract counterparty information
            JsonNode counterparty = dataSection.path("counterparty");
            String holderId = counterparty.path("holder_id").asText(null);
            String holderName = counterparty.path("holder_name").asText(null);
            String accountNumber = counterparty.path("account_number").asText(null);
            String accountType = counterparty.path("account_type").asText(null);

            // Extract institution information
            JsonNode institution = counterparty.path("institution");
            String institutionId = institution.path("id").asText(null);
            String institutionName = institution.path("name").asText(null);
            String institutionCountry = institution.path("country").asText(null);

            // Create webhook log entry
            WebhookLog webhookLog = new WebhookLog();
            webhookLog.setEventId(eventId);
            webhookLog.setEventType(eventType);
            webhookLog.setMode(mode);
            webhookLog.setCreatedAt(LocalDateTime.now());
            webhookLog.setRawBody(rawBody);
            webhookLog.setSignatureHeader(signature);
            webhookLog.setProcessed(true);
            webhookLog.setAccountVerificationId(accountVerificationId);
            webhookLog.setStatus(status);
            webhookLog.setReason(reason);
            webhookLog.setReceiptUrl(receiptUrl);
            webhookLog.setTransferId(transferId);
            webhookLog.setTransactionDate(transactionDate);
            webhookLog.setHolderId(holderId);
            webhookLog.setHolderName(holderName);
            webhookLog.setAccountNumber(accountNumber);
            webhookLog.setAccountType(accountType);
            webhookLog.setInstitutionId(institutionId);
            webhookLog.setInstitutionName(institutionName);
            webhookLog.setInstitutionCountry(institutionCountry);

            // Save to database
            webhookLogRepository.save(webhookLog);

            logger.info("Successfully processed and stored webhook: {}", eventId);
            return true;

        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Calculate HMAC-SHA256 signature
     */
    private String calculateHmacSha256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hex string
        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Constant time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}