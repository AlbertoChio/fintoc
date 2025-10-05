package com.fintoc.logger.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Counterparty information in the Account Validation response
 * Embeddable entity for nested storage in AccountValidationResponse
 */
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class Counterparty {
    
    @JsonProperty("account_number")
    @Column(name = "counterparty_account_number")
    private String accountNumber;
    
    @JsonProperty("holder_id")
    @Column(name = "counterparty_holder_id")
    private String holderId;
    
    @JsonProperty("holder_name")
    @Column(name = "counterparty_holder_name")
    private String holderName;
    
    @JsonProperty("account_type")
    @Column(name = "counterparty_account_type")
    private String accountType;
    
    @Embedded
    private Institution institution;
    
    // Default constructor
    public Counterparty() {}
    
    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public Institution getInstitution() {
        return institution;
    }
    
    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    // Helper methods
    public boolean isClabe() {
        return "clabe".equals(accountType);
    }
    
    public boolean isCard() {
        return "card".equals(accountType);
    }
    
    @Override
    public String toString() {
        return "Counterparty{" +
                "accountNumber='" + accountNumber + '\'' +
                ", holderId='" + holderId + '\'' +
                ", holderName='" + holderName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", institution=" + institution +
                '}';
    }
}