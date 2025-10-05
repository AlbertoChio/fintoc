package com.fintoc.logger.dto;

/**
 * DTO for account validation request
 * Used for API requests that don't require full entity structure
 */
public class ValidationRequestDto {
    
    private String accountId;
    private String validationType;
    private String institutionId;
    
    // Default constructor
    public ValidationRequestDto() {}
    
    // Constructor with parameters
    public ValidationRequestDto(String accountId, String validationType, String institutionId) {
        this.accountId = accountId;
        this.validationType = validationType;
        this.institutionId = institutionId;
    }
    
    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getValidationType() {
        return validationType;
    }
    
    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }
    
    public String getInstitutionId() {
        return institutionId;
    }
    
    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }
    
    @Override
    public String toString() {
        return "ValidationRequestDto{" +
                "accountId='" + accountId + '\'' +
                ", validationType='" + validationType + '\'' +
                ", institutionId='" + institutionId + '\'' +
                '}';
    }
}