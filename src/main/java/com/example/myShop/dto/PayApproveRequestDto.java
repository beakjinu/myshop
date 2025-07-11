package com.example.myShop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayApproveRequestDto {
    private String tid;
    private String pgToken;
    private String partnerOrderId;
    private String partnerUserId;

}
