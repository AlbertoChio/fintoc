package com.fintoc.logger.service;

import com.fintoc.logger.entity.AccountValidationLog;
import com.fintoc.logger.repository.AccountValidationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountValidationLogService {

    private static final Logger logger = LoggerFactory.getLogger(AccountValidationLogService.class);

    private final AccountValidationLogRepository validationLogRepository;

    @Autowired
    public AccountValidationLogService(AccountValidationLogRepository validationLogRepository) {
        this.validationLogRepository = validationLogRepository;
    }

    /**
     * Save an account validation log entry
     */
    public AccountValidationLog saveValidationLog(AccountValidationLog validationLog) {
        try {
            AccountValidationLog savedLog = validationLogRepository.save(validationLog);
            logger.debug("Saved validation log: {} for account: {}", savedLog.getId(), savedLog.getAccountId());
            return savedLog;
        } catch (Exception e) {
            logger.error("Error saving validation log for account: {}", validationLog.getAccountId(), e);
            throw e;
        }
    }

    /**
     * Get all validation logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AccountValidationLog> getAllValidationLogs(Pageable pageable) {
        return validationLogRepository.findAll(pageable);
    }

    /**
     * Get validation logs by account ID
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByAccountId(String accountId) {
        return validationLogRepository.findByAccountId(accountId);
    }

    /**
     * Get validation logs by validation type
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByType(String validationType) {
        return validationLogRepository.findByValidationType(validationType);
    }

    /**
     * Get validation logs by result
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByResult(String validationResult) {
        return validationLogRepository.findByValidationResult(validationResult);
    }

    /**
     * Get validation logs by success status
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsBySuccess(Boolean success) {
        return validationLogRepository.findBySuccess(success);
    }

    /**
     * Get validation logs by date range
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return validationLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get recent failed validations
     */
    @Transactional(readOnly = true)
    public Page<AccountValidationLog> getRecentFailedValidations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return validationLogRepository.findRecentFailedValidations(pageable);
    }

    /**
     * Get slowest validations
     */
    @Transactional(readOnly = true)
    public Page<AccountValidationLog> getSlowestValidations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return validationLogRepository.findSlowestValidations(pageable);
    }

    /**
     * Get pending validations
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getPendingValidations() {
        return validationLogRepository.findPendingValidations();
    }

    /**
     * Get validation logs by link ID
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByLinkId(String linkId) {
        return validationLogRepository.findByLinkId(linkId);
    }

    /**
     * Get validation logs by account and date range
     */
    @Transactional(readOnly = true)
    public List<AccountValidationLog> getValidationLogsByAccountAndDateRange(String accountId, 
                                                                           LocalDateTime startDate, 
                                                                           LocalDateTime endDate) {
        return validationLogRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    /**
     * Get validation summary by account
     */
    @Transactional(readOnly = true)
    public List<Object[]> getValidationSummaryByAccount() {
        return validationLogRepository.getValidationSummaryByAccount();
    }

    /**
     * Count validations by type
     */
    @Transactional(readOnly = true)
    public List<Object[]> countValidationsByType() {
        return validationLogRepository.countValidationsByType();
    }

    /**
     * Get average execution time by validation type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageExecutionTimeByType() {
        return validationLogRepository.averageExecutionTimeByValidationType();
    }

    /**
     * Get success rate by validation type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getSuccessRateByType() {
        return validationLogRepository.successRateByValidationType();
    }

    /**
     * Count total validation logs
     */
    @Transactional(readOnly = true)
    public long getTotalValidationLogsCount() {
        return validationLogRepository.count();
    }

    /**
     * Count validation logs in date range
     */
    @Transactional(readOnly = true)
    public Long getValidationLogsCountInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return validationLogRepository.countValidationsBetweenDates(startDate, endDate);
    }

    /**
     * Delete old validation logs (older than specified days)
     */
    @Transactional
    public void deleteOldValidationLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<AccountValidationLog> oldLogs = validationLogRepository.findByCreatedAtBetween(
            LocalDateTime.of(2000, 1, 1, 0, 0), cutoffDate);
        
        if (!oldLogs.isEmpty()) {
            validationLogRepository.deleteAll(oldLogs);
            logger.info("Deleted {} old validation logs", oldLogs.size());
        }
    }

    /**
     * Create a validation log entry for account validation API call
     */
    public AccountValidationLog createValidationLog(String accountId, String validationType, 
                                                   String requestHeaders, String requestBody, 
                                                   Integer responseStatus, String responseHeaders, 
                                                   String responseBody, Long executionTimeMs, 
                                                   String apiKeyUsed, Boolean success, 
                                                   String errorMessage, String linkId) {
        
        AccountValidationLog log = new AccountValidationLog(accountId, validationType);
        log.setRequestHeaders(requestHeaders);
        log.setRequestBody(requestBody);
        log.setResponseStatus(responseStatus);
        log.setResponseHeaders(responseHeaders);
        log.setResponseBody(responseBody);
        log.setExecutionTimeMs(executionTimeMs);
        log.setApiKeyUsed(apiKeyUsed);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        log.setLinkId(linkId);
        
        // Extract validation result from response
        if (success && responseStatus >= 200 && responseStatus < 300) {
            log.setValidationResult("SUCCESS");
        } else if (responseStatus >= 400) {
            log.setValidationResult("FAILED");
        } else {
            log.setValidationResult("PENDING");
        }
        
        return saveValidationLog(log);
    }
}