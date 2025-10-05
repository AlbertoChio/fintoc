package com.fintoc.logger.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Response Entity for Fintoc Account Validation API
 * Matches the actual Fintoc API v2 response structure and stores it in database
 */
@Entity
@Table(name = "account_validation")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountValidationResponse {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "object_type")
    private String object;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "reason")
    private String reason;
    
    @JsonProperty("transfer_id")
    @Column(name = "transfer_id")
    private String transferId;
    
    @Embedded
    private Counterparty counterparty;
    
    @Column(name = "mode")
    private String mode;
    
    @JsonProperty("receipt_url")
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;
    
    @JsonProperty("transaction_date")
    @Column(name = "transaction_date")
    private String transactionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public AccountValidationResponse() {}
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getTransferId() {
        return transferId;
    }
    
    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }
    
    public Counterparty getCounterparty() {
        return counterparty;
    }
    
    public void setCounterparty(Counterparty counterparty) {
        this.counterparty = counterparty;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getReceiptUrl() {
        return receiptUrl;
    }
    
    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
    
    public String getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isSucceeded() {
        return "succeeded".equals(status);
    }
    
    public boolean hasFailed() {
        return "failed".equals(status);
    }
    
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isTestMode() {
        return "test".equals(mode);
    }
    
    public boolean isProductionMode() {
        return "live".equals(mode);
    }
    
    @Override
    public String toString() {
        return "AccountValidationResponse{" +
                "id='" + id + '\'' +
                ", object='" + object + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                ", transferId='" + transferId + '\'' +
                ", mode='" + mode + '\'' +
                ", receiptUrl='" + receiptUrl + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", counterparty=" + counterparty +
                '}';
    }
}