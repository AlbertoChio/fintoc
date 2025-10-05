package com.fintoc.logger.dto;

import java.time.LocalDateTime;

/**
 * DTO for validation summary statistics
 * Used for API responses that provide aggregated data
 */
public class ValidationSummaryDto {
    
    private String validationType;
    private Long totalValidations;
    private Long successfulValidations;
    private Long failedValidations;
    private Double successRate;
    private LocalDateTime lastValidation;
    
    // Default constructor
    public ValidationSummaryDto() {}
    
    // Constructor with parameters
    public ValidationSummaryDto(String validationType, Long totalValidations, 
                               Long successfulValidations, Long failedValidations, 
                               Double successRate, LocalDateTime lastValidation) {
        this.validationType = validationType;
        this.totalValidations = totalValidations;
        this.successfulValidations = successfulValidations;
        this.failedValidations = failedValidations;
        this.successRate = successRate;
        this.lastValidation = lastValidation;
    }
    
    // Getters and Setters
    public String getValidationType() {
        return validationType;
    }
    
    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }
    
    public Long getTotalValidations() {
        return totalValidations;
    }
    
    public void setTotalValidations(Long totalValidations) {
        this.totalValidations = totalValidations;
    }
    
    public Long getSuccessfulValidations() {
        return successfulValidations;
    }
    
    public void setSuccessfulValidations(Long successfulValidations) {
        this.successfulValidations = successfulValidations;
    }
    
    public Long getFailedValidations() {
        return failedValidations;
    }
    
    public void setFailedValidations(Long failedValidations) {
        this.failedValidations = failedValidations;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
    
    public LocalDateTime getLastValidation() {
        return lastValidation;
    }
    
    public void setLastValidation(LocalDateTime lastValidation) {
        this.lastValidation = lastValidation;
    }
    
    @Override
    public String toString() {
        return "ValidationSummaryDto{" +
                "validationType='" + validationType + '\'' +
                ", totalValidations=" + totalValidations +
                ", successfulValidations=" + successfulValidations +
                ", failedValidations=" + failedValidations +
                ", successRate=" + successRate +
                ", lastValidation=" + lastValidation +
                '}';
    }
}