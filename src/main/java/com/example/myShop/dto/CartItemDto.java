package com.example.myShop.dto;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CartItemDto {
    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Long itemId;

    @Min(value = 1, message="최소 1개 이상 담아 주세요.")
    private int count;
}
