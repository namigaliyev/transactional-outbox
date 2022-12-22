package com.example.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer customerId;

    private String productId;

    private BigDecimal price;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
