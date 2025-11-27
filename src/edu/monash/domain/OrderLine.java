package edu.monash.domain;


public class OrderLine {
    public String productId;
    public int qty;
    public double unitPrice;
    public double lineTotal;


    public OrderLine(String productId, int qty, double unitPrice) {
        this.productId = productId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.lineTotal = qty * unitPrice;
    }
}
