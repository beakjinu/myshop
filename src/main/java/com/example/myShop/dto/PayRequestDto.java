package com.example.myShop.dto;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Getter
@Setter
public class PayRequestDto {
    private Long itemId;
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 999, message = "최대 수량은 999개까지 입니다.")
    private int count;


    private String partnerOrderId;

    private String email;
    private String name;
    private String itemName;
    private LocalDateTime orderDateTime;
}
