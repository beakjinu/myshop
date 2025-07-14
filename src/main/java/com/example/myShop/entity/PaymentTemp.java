package com.example.myShop.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_payment_temp")
@Getter
@Setter
@NoArgsConstructor
public class PaymentTemp {

    @Id
    private String tid;
    private Long itemId;
    private int count;
    private String email;
    private String partnerOrderId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
