package edu.monash.domain;

import java.time.LocalDate;


public class VIPMembership {

    public final String email;

    public String type;

    public LocalDate startDate;

    public LocalDate endDate;

    public String status;


    public VIPMembership(String email, String type, LocalDate startDate, LocalDate endDate, String status) {
        this.email = email;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
