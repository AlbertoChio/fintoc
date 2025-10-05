package com.fintoc.logger.controller;

import com.fintoc.logger.entity.AccountValidationLog;
import com.fintoc.logger.service.AccountValidationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/validation-logs")
public class ValidationLogsController {

    private final AccountValidationLogService validationLogService;

    @Autowired
    public ValidationLogsController(AccountValidationLogService validationLogService) {
        this.validationLogService = validationLogService;
    }

    @GetMapping
    public ResponseEntity<Page<AccountValidationLog>> getAllValidationLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountValidationLog> logs = validationLogService.getAllValidationLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<AccountValidationLog>> getValidationLogsByAccount(@PathVariable String accountId) {
        List<AccountValidationLog> logs = validationLogService.getValidationLogsByAccountId(accountId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/type/{validationType}")
    public ResponseEntity<List<AccountValidationLog>> getValidationLogsByType(@PathVariable String validationType) {
        List<AccountValidationLog> logs = validationLogService.getValidationLogsByType(validationType);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/failed")
    public ResponseEntity<Page<AccountValidationLog>> getFailedValidations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AccountValidationLog> failedLogs = validationLogService.getRecentFailedValidations(page, size);
        return ResponseEntity.ok(failedLogs);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AccountValidationLog>> getPendingValidations() {
        List<AccountValidationLog> pendingLogs = validationLogService.getPendingValidations();
        return ResponseEntity.ok(pendingLogs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AccountValidationLog>> searchValidationLogs(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String validationType,
            @RequestParam(required = false) String validationResult,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AccountValidationLog> results;

        if (accountId != null) {
            if (startDate != null && endDate != null) {
                results = validationLogService.getValidationLogsByAccountAndDateRange(accountId, startDate, endDate);
            } else {
                results = validationLogService.getValidationLogsByAccountId(accountId);
            }
        } else if (validationType != null) {
            results = validationLogService.getValidationLogsByType(validationType);
        } else if (validationResult != null) {
            results = validationLogService.getValidationLogsByResult(validationResult);
        } else if (success != null) {
            results = validationLogService.getValidationLogsBySuccess(success);
        } else if (startDate != null && endDate != null) {
            results = validationLogService.getValidationLogsByDateRange(startDate, endDate);
        } else {
            results = validationLogService.getAllValidationLogs(PageRequest.of(0, 100)).getContent();
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<List<Object[]>> getValidationSummary() {
        List<Object[]> summary = validationLogService.getValidationSummaryByAccount();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/stats/by-type")
    public ResponseEntity<List<Object[]>> getValidationStatsByType() {
        List<Object[]> stats = validationLogService.countValidationsByType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getValidationCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> counts = new HashMap<>();
        
        if (startDate != null && endDate != null) {
            Long countInRange = validationLogService.getValidationLogsCountInDateRange(startDate, endDate);
            counts.put("count", countInRange);
            counts.put("startDate", startDate);
            counts.put("endDate", endDate);
        } else {
            Long totalCount = validationLogService.getTotalValidationLogsCount();
            counts.put("totalCount", totalCount);
        }
        
        return ResponseEntity.ok(counts);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldValidationLogs(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        validationLogService.deleteOldValidationLogs(daysToKeep);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cleanup completed for validation logs older than " + daysToKeep + " days");
        
        return ResponseEntity.ok(response);
    }
}