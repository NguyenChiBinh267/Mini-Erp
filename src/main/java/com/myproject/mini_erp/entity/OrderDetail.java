package com.myproject.mini_erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_details"
)

@Setter
@Getter
@NoArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "price", nullable = false, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_amount", scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "total", nullable = false, scale = 2)
    private BigDecimal total;
}
