package com.congty.ecommerce.model;

public class Voucher {
    private String code;
    private double discountAmount;
    private boolean isActive;

    public Voucher(String code, double discountAmount, boolean isActive) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.isActive = isActive;
    }

    public double getDiscountAmount() { return discountAmount; }
    public boolean isActive() { return isActive; }
    public String getCode() { return code; }
}
