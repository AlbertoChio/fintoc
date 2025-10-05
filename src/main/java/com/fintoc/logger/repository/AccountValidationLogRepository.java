package com.fintoc.logger.repository;

import com.fintoc.logger.entity.AccountValidationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountValidationLogRepository extends JpaRepository<AccountValidationLog, Long> {

    // Find by account ID
    List<AccountValidationLog> findByAccountId(String accountId);

    // Find by validation type
    List<AccountValidationLog> findByValidationType(String validationType);

    // Find by validation result
    List<AccountValidationLog> findByValidationResult(String validationResult);

    // Find by success status
    List<AccountValidationLog> findBySuccess(Boolean success);

    // Find by link ID
    List<AccountValidationLog> findByLinkId(String linkId);

    // Find by date range
    List<AccountValidationLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by response status
    List<AccountValidationLog> findByResponseStatus(Integer responseStatus);

    // Find recent failed validations
    @Query("SELECT v FROM AccountValidationLog v WHERE v.success = false ORDER BY v.createdAt DESC")
    Page<AccountValidationLog> findRecentFailedValidations(Pageable pageable);

    // Find slowest validations
    @Query("SELECT v FROM AccountValidationLog v WHERE v.executionTimeMs IS NOT NULL ORDER BY v.executionTimeMs DESC")
    Page<AccountValidationLog> findSlowestValidations(Pageable pageable);

    // Count validations by type
    @Query("SELECT v.validationType, COUNT(v) FROM AccountValidationLog v GROUP BY v.validationType")
    List<Object[]> countValidationsByType();

    // Average execution time by validation type
    @Query("SELECT v.validationType, AVG(v.executionTimeMs) FROM AccountValidationLog v WHERE v.executionTimeMs IS NOT NULL GROUP BY v.validationType")
    List<Object[]> averageExecutionTimeByValidationType();

    // Success rate by validation type
    @Query("SELECT v.validationType, " +
           "SUM(CASE WHEN v.validationResult = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
           "COUNT(v) as totalCount " +
           "FROM AccountValidationLog v GROUP BY v.validationType")
    List<Object[]> successRateByValidationType();

    // Find validations with specific error messages
    @Query("SELECT v FROM AccountValidationLog v WHERE v.success = false AND v.errorMessage LIKE %:errorText%")
    List<AccountValidationLog> findByErrorMessageContaining(@Param("errorText") String errorText);

    // Count validations by date range
    @Query("SELECT COUNT(v) FROM AccountValidationLog v WHERE v.createdAt BETWEEN :startDate AND :endDate")
    Long countValidationsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find validations by response status range
    @Query("SELECT v FROM AccountValidationLog v WHERE v.responseStatus BETWEEN :minStatus AND :maxStatus ORDER BY v.createdAt DESC")
    List<AccountValidationLog> findByResponseStatusBetween(@Param("minStatus") Integer minStatus, @Param("maxStatus") Integer maxStatus);

    // Find pending validations
    @Query("SELECT v FROM AccountValidationLog v WHERE v.validationResult = 'PENDING' ORDER BY v.createdAt ASC")
    List<AccountValidationLog> findPendingValidations();

    // Find validations by account and date range
    @Query("SELECT v FROM AccountValidationLog v WHERE v.accountId = :accountId AND v.createdAt BETWEEN :startDate AND :endDate ORDER BY v.createdAt DESC")
    List<AccountValidationLog> findByAccountIdAndDateRange(@Param("accountId") String accountId, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);

    // Get validation summary by account
    @Query("SELECT v.accountId, " +
           "COUNT(v) as totalValidations, " +
           "SUM(CASE WHEN v.validationResult = 'SUCCESS' THEN 1 ELSE 0 END) as successfulValidations, " +
           "SUM(CASE WHEN v.validationResult = 'FAILED' THEN 1 ELSE 0 END) as failedValidations, " +
           "MAX(v.createdAt) as lastValidationDate " +
           "FROM AccountValidationLog v " +
           "GROUP BY v.accountId " +
           "ORDER BY totalValidations DESC")
    List<Object[]> getValidationSummaryByAccount();
}