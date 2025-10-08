package com.fintoc.logger.controller;

import com.fintoc.logger.entity.WebhookLog;
import com.fintoc.logger.repository.WebhookLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for viewing and managing webhook logs
 */
@RestController
@RequestMapping("/webhook-logs")
public class WebhookLogsController {

    private final WebhookLogRepository webhookLogRepository;

    @Autowired
    public WebhookLogsController(WebhookLogRepository webhookLogRepository) {
        this.webhookLogRepository = webhookLogRepository;
    }

    /**
     * Get all webhook logs with pagination
     */
    @GetMapping
    public ResponseEntity<Page<WebhookLog>> getAllWebhookLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WebhookLog> webhookLogs = webhookLogRepository.findRecentWebhooks(pageable);
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get webhook log by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebhookLog> getWebhookLogById(@PathVariable Long id) {
        Optional<WebhookLog> webhookLog = webhookLogRepository.findById(id);
        return webhookLog.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get webhook log by event ID
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<WebhookLog> getWebhookLogByEventId(@PathVariable String eventId) {
        Optional<WebhookLog> webhookLog = webhookLogRepository.findByEventId(eventId);
        return webhookLog.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get webhook logs by event type
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<WebhookLog>> getWebhookLogsByEventType(@PathVariable String eventType) {
        List<WebhookLog> webhookLogs = webhookLogRepository.findByEventType(eventType);
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get webhook logs by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WebhookLog>> getWebhookLogsByStatus(@PathVariable String status) {
        List<WebhookLog> webhookLogs = webhookLogRepository.findByStatus(status);
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get webhook logs by account number
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<WebhookLog>> getWebhookLogsByAccountNumber(@PathVariable String accountNumber) {
        List<WebhookLog> webhookLogs = webhookLogRepository.findByAccountNumber(accountNumber);
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get unprocessed webhook logs
     */
    @GetMapping("/unprocessed")
    public ResponseEntity<List<WebhookLog>> getUnprocessedWebhookLogs() {
        List<WebhookLog> webhookLogs = webhookLogRepository.findUnprocessedWebhooks();
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get failed webhook logs
     */
    @GetMapping("/failed")
    public ResponseEntity<List<WebhookLog>> getFailedWebhookLogs() {
        List<WebhookLog> webhookLogs = webhookLogRepository.findFailedWebhooks();
        return ResponseEntity.ok(webhookLogs);
    }

    /**
     * Get webhook statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getWebhookStats() {
        Long totalWebhooks = webhookLogRepository.countTotalWebhooks();
        List<Object[]> eventTypeStats = webhookLogRepository.countWebhooksByEventType();
        List<Object[]> statusStats = webhookLogRepository.countWebhooksByStatus();
        
        return ResponseEntity.ok(Map.of(
            "total_webhooks", totalWebhooks,
            "by_event_type", eventTypeStats,
            "by_status", statusStats,
            "generated_at", LocalDateTime.now()
        ));
    }

    /**
     * Search webhook logs by multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<WebhookLog>> searchWebhookLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String institutionId,
            @RequestParam(required = false) Boolean processed) {
        
        List<WebhookLog> results;
        
        if (eventType != null && status != null) {
            results = webhookLogRepository.findByEventTypeAndStatus(eventType, status);
        } else if (eventType != null) {
            results = webhookLogRepository.findByEventType(eventType);
        } else if (status != null) {
            results = webhookLogRepository.findByStatus(status);
        } else if (accountNumber != null) {
            results = webhookLogRepository.findByAccountNumber(accountNumber);
        } else if (institutionId != null) {
            results = webhookLogRepository.findByInstitutionId(institutionId);
        } else if (processed != null) {
            results = webhookLogRepository.findByProcessed(processed);
        } else {
            // Default to recent webhooks if no criteria provided
            Pageable pageable = PageRequest.of(0, 50);
            results = webhookLogRepository.findRecentWebhooks(pageable).getContent();
        }
        
        return ResponseEntity.ok(results);
    }

    /**
     * Get webhook logs count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWebhookLogsCount() {
        Long totalCount = webhookLogRepository.countTotalWebhooks();
        return ResponseEntity.ok(Map.of("total_count", totalCount));
    }
}