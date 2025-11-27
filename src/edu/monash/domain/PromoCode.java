package edu.monash.domain;

import java.time.LocalDate;


public class PromoCode {
    public String code;
    public int percent;
    public String scope; // e.g., "ALL" or "PICKUP"
    public LocalDate expiry;


    public PromoCode(String code, int percent, String scope, LocalDate expiry) {
        this.code = code;
        this.percent = percent;
        this.scope = scope;
        this.expiry = expiry;
    }


    public boolean isExpired() {
        return expiry != null && expiry.isBefore(LocalDate.now());
    }
}
