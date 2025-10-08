package com.fintoc.logger.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintoc.logger.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller for handling Fintoc webhooks
 */
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebhookController(WebhookService webhookService, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }

    /**
     * Fintoc webhook endpoint for account verification events
     * Receives webhooks with fintoc-signature header for verification
     */
    @PostMapping("/fintoc")
    public ResponseEntity<Map<String, String>> handleFintocWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "fintoc-signature", required = false) String fintocSignature,
            HttpServletRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Received Fintoc webhook with signature: {}", fintocSignature);
            logger.info("Webhook raw body: {}", rawBody);
            
            // Parse the webhook body
            JsonNode webhookData = objectMapper.readTree(rawBody);
            
            // Extract event details
            String eventId = webhookData.path("id").asText();
            String eventType = webhookData.path("type").asText();
            String mode = webhookData.path("mode").asText();
            String createdAt = webhookData.path("created_at").asText();
            
            logger.info("Processing webhook - ID: {}, Type: {}, Mode: {}, Created: {}", eventId, eventType, mode, createdAt);
            
            // Verify webhook signature
            boolean signatureValid = webhookService.verifyWebhookSignature(rawBody, fintocSignature);
            if (!signatureValid) {
                logger.warn("Invalid webhook signature for event: {}", eventId);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_signature",
                    "message", "Webhook signature verification failed"
                ));
            }
            
            // Process the webhook based on event type
            boolean processed = webhookService.processWebhook(webhookData, fintocSignature, rawBody);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (processed) {
                logger.info("Successfully processed webhook {} in {}ms", eventId, executionTime);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "event_id", eventId,
                    "event_type", eventType,
                    "processed_at", String.valueOf(System.currentTimeMillis()),
                    "execution_time_ms", String.valueOf(executionTime)
                ));
            } else {
                logger.warn("Failed to process webhook {}", eventId);
                return ResponseEntity.internalServerError().body(Map.of(
                    "error", "processing_failed",
                    "message", "Failed to process webhook",
                    "event_id", eventId
                ));
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Error processing Fintoc webhook: {}", e.getMessage(), e);
            
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "internal_error",
                "message", "Internal server error processing webhook",
                "execution_time_ms", String.valueOf(executionTime)
            ));
        }
    }

    /**
     * Health check endpoint for webhook service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "webhook_controller",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    /**
     * Test endpoint to simulate webhook reception
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@RequestBody Map<String, Object> testData) {
        logger.info("Test webhook received: {}", testData);
        
        return ResponseEntity.ok(Map.of(
            "status", "test_received",
            "received_data", testData,
            "timestamp", System.currentTimeMillis()
        ));
    }
}