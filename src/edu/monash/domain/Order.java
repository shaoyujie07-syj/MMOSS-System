package edu.monash.domain;

import java.util.*;


public class Order {
    public String id;
    public String email;
    public String fulfilment; // PICKUP or DELIVERY
    public String where;      // store name/address or delivery descriptor
    public java.util.List<OrderLine> lines = new ArrayList<>();
    public double subtotal;
    public double discount;
    public double fee;
    public double total;
}
