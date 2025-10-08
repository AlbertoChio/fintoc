package com.fintoc.logger.repository;

import com.fintoc.logger.entity.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WebhookLog entity
 */
@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    /**
     * Find webhook log by event ID
     */
    Optional<WebhookLog> findByEventId(String eventId);

    /**
     * Find webhook logs by event type
     */
    List<WebhookLog> findByEventType(String eventType);

    /**
     * Find webhook logs by status
     */
    List<WebhookLog> findByStatus(String status);

    /**
     * Find webhook logs by mode (test/live)
     */
    List<WebhookLog> findByMode(String mode);

    /**
     * Find webhook logs by processed status
     */
    List<WebhookLog> findByProcessed(Boolean processed);

    /**
     * Find webhook logs by account verification ID
     */
    List<WebhookLog> findByAccountVerificationId(String accountVerificationId);

    /**
     * Find webhook logs by transfer ID
     */
    List<WebhookLog> findByTransferId(String transferId);

    /**
     * Find webhook logs by account number
     */
    List<WebhookLog> findByAccountNumber(String accountNumber);

    /**
     * Find webhook logs by holder ID
     */
    List<WebhookLog> findByHolderId(String holderId);

    /**
     * Find webhook logs by institution ID
     */
    List<WebhookLog> findByInstitutionId(String institutionId);

    /**
     * Find webhook logs created within date range
     */
    List<WebhookLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent webhook logs (paginated)
     */
    @Query("SELECT w FROM WebhookLog w ORDER BY w.createdAt DESC")
    Page<WebhookLog> findRecentWebhooks(Pageable pageable);

    /**
     * Find unprocessed webhook logs
     */
    @Query("SELECT w FROM WebhookLog w WHERE w.processed = false ORDER BY w.createdAt ASC")
    List<WebhookLog> findUnprocessedWebhooks();

    /**
     * Find webhook logs by event type and status
     */
    List<WebhookLog> findByEventTypeAndStatus(String eventType, String status);

    /**
     * Count webhook logs by event type
     */
    @Query("SELECT w.eventType, COUNT(w) FROM WebhookLog w GROUP BY w.eventType")
    List<Object[]> countWebhooksByEventType();

    /**
     * Count webhook logs by status
     */
    @Query("SELECT w.status, COUNT(w) FROM WebhookLog w GROUP BY w.status")
    List<Object[]> countWebhooksByStatus();

    /**
     * Find webhook logs by institution and date range
     */
    @Query("SELECT w FROM WebhookLog w WHERE w.institutionId = :institutionId AND w.createdAt BETWEEN :startDate AND :endDate")
    List<WebhookLog> findByInstitutionAndDateRange(
            @Param("institutionId") String institutionId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count total webhook logs
     */
    @Query("SELECT COUNT(w) FROM WebhookLog w")
    Long countTotalWebhooks();

    /**
     * Find webhook logs with failed processing
     */
    @Query("SELECT w FROM WebhookLog w WHERE w.processed = false OR w.status = 'failed' ORDER BY w.createdAt DESC")
    List<WebhookLog> findFailedWebhooks();

    /**
     * Check if event ID already exists (for duplicate prevention)
     */
    boolean existsByEventId(String eventId);

    /**
     * Find recent webhook logs by account number
     */
    @Query("SELECT w FROM WebhookLog w WHERE w.accountNumber = :accountNumber ORDER BY w.createdAt DESC")
    List<WebhookLog> findRecentByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);
}