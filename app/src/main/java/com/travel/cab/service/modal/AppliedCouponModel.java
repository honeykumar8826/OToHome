package com.travel.cab.service.modal;

public class AppliedCouponModel {
    private String coupon_code;

    public String getPush_key_coupon() {
        return push_key_coupon;
    }

    public void setPush_key_coupon(String push_key_coupon) {
        this.push_key_coupon = push_key_coupon;
    }

    private String push_key_coupon;

    public String getCoupon_code() {
        return coupon_code;
    }

    public void setCoupon_code(String coupon_code) {
        this.coupon_code = coupon_code;
    }

    public String getCoupon_count() {
        return coupon_count;
    }

    public void setCoupon_count(String coupon_count) {
        this.coupon_count = coupon_count;
    }

    private String coupon_count;
}
