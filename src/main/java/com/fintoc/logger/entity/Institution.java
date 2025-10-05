package com.fintoc.logger.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

/**
 * Institution information in the Counterparty object
 * Embeddable entity for nested storage
 */
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class Institution {
    
    @Column(name = "institution_id")
    private String id;
    
    @Column(name = "institution_name")
    private String name;
    
    @Column(name = "institution_country")
    private String country;
    
    // Default constructor
    public Institution() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    // Helper methods
    public boolean isMexican() {
        return "mx".equals(country);
    }
    
    public boolean isChilean() {
        return "cl".equals(country);
    }
    
    public boolean isColombian() {
        return "co".equals(country);
    }
    
    @Override
    public String toString() {
        return "Institution{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}