package edu.monash.domain;


public class Store {
    public String id;
    public String name;
    public String address;
    public String hours;
    public String phone;


    public Store(String id, String name, String address, String hours, String phone) {
        this.id = id; this.name = name; this.address = address; this.hours = hours; this.phone = phone;
    }
}
