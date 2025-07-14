package com.example.myShop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayApproveResponseDto {
    private String tid;
    private String itemName;
    private String partnerOrderId;
    private String partnerUserId;
    private int totalAmount;
    private String approvedTime;
}
