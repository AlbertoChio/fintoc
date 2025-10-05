package com.fintoc.logger.repository;

import com.fintoc.logger.entity.AccountValidationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountValidationResponseRepository extends JpaRepository<AccountValidationResponse, String> {
    
    /**
     * Find validation responses by status
     */
    List<AccountValidationResponse> findByStatus(String status);
    
    /**
     * Find validation responses by mode (test/live)
     */
    List<AccountValidationResponse> findByMode(String mode);
    
    /**
     * Find validation responses by transfer ID
     */
    Optional<AccountValidationResponse> findByTransferId(String transferId);
    
    /**
     * Find validation responses by counterparty holder name
     */
    @Query("SELECT v FROM AccountValidationResponse v WHERE v.counterparty.holderName LIKE %:holderName%")
    List<AccountValidationResponse> findByHolderNameContaining(@Param("holderName") String holderName);
    
    /**
     * Find validation responses by institution
     */
    @Query("SELECT v FROM AccountValidationResponse v WHERE v.counterparty.institution.id = :institutionId")
    List<AccountValidationResponse> findByInstitutionId(@Param("institutionId") String institutionId);
    
    /**
     * Find validation responses by account type
     */
    @Query("SELECT v FROM AccountValidationResponse v WHERE v.counterparty.accountType = :accountType")
    List<AccountValidationResponse> findByAccountType(@Param("accountType") String accountType);
    
    /**
     * Find validation responses within a date range
     */
    List<AccountValidationResponse> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count validations by status
     */
    @Query("SELECT COUNT(v) FROM AccountValidationResponse v WHERE v.status = :status")
    Long countByStatus(@Param("status") String status);
    
    /**
     * Count validations by mode
     */
    @Query("SELECT COUNT(v) FROM AccountValidationResponse v WHERE v.mode = :mode")
    Long countByMode(@Param("mode") String mode);
    
    /**
     * Find recent validations (last N days)
     */
    @Query("SELECT v FROM AccountValidationResponse v WHERE v.createdAt >= :since ORDER BY v.createdAt DESC")
    List<AccountValidationResponse> findRecentValidations(@Param("since") LocalDateTime since);
}