package edu.monash.domain;

public class Payment {
    public double preBalance, postBalance;
    public String orderId;

    public Payment(String id, double pre, double post) {
        orderId = id;
        preBalance = pre;
        postBalance = post;
    }
}