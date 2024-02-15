package com.project.MyRh.Stripe;

import lombok.Data;

@Data
public class Checkout {
    private String priceId;
    private String successUrl;
    private String cancelUrl;

}
