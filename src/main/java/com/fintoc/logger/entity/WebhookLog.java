package com.fintoc.logger.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to store webhook event logs from Fintoc
 */
@Entity
@Table(name = "webhook_logs")
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "mode", length = 20)
    private String mode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "raw_body", columnDefinition = "TEXT")
    private String rawBody;

    @Column(name = "signature_header", length = 500)
    private String signatureHeader;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    // Account verification specific fields
    @Column(name = "account_verification_id", length = 100)
    private String accountVerificationId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "receipt_url", length = 1000)
    private String receiptUrl;

    @Column(name = "transfer_id", length = 100)
    private String transferId;

    @Column(name = "transaction_date", length = 50)
    private String transactionDate;

    // Counterparty fields
    @Column(name = "holder_id", length = 100)
    private String holderId;

    @Column(name = "holder_name", length = 255)
    private String holderName;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "account_type", length = 50)
    private String accountType;

    // Institution fields
    @Column(name = "institution_id", length = 100)
    private String institutionId;

    @Column(name = "institution_name", length = 255)
    private String institutionName;

    @Column(name = "institution_country", length = 10)
    private String institutionCountry;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // Constructors
    public WebhookLog() {
        this.createdAt = LocalDateTime.now();
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public String getSignatureHeader() {
        return signatureHeader;
    }

    public void setSignatureHeader(String signatureHeader) {
        this.signatureHeader = signatureHeader;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public String getAccountVerificationId() {
        return accountVerificationId;
    }

    public void setAccountVerificationId(String accountVerificationId) {
        this.accountVerificationId = accountVerificationId;
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

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getHolderId() {
        return holderId;
    }

    public void setHolderId(String holderId) {
        this.holderId = holderId;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionCountry() {
        return institutionCountry;
    }

    public void setInstitutionCountry(String institutionCountry) {
        this.institutionCountry = institutionCountry;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "WebhookLog{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", mode='" + mode + '\'' +
                ", status='" + status + '\'' +
                ", accountVerificationId='" + accountVerificationId + '\'' +
                ", holderName='" + holderName + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", institutionName='" + institutionName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}