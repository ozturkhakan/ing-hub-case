package com.brokerage.model;

import com.brokerage.model.enums.OrderSide;
import com.brokerage.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String customerId;
    private String assetName;
    
    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;
    
    private Double size;
    private Double price;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private LocalDateTime createDate;

    @Override
    public String toString() {
        return "Order{" +
            "id=" + id +
            ", customerId='" + customerId + '\'' +
            ", assetName='" + assetName + '\'' +
            ", orderSide=" + orderSide +
            ", size=" + size +
            ", price=" + price +
            ", status=" + status +
            ", createDate=" + createDate +
            '}';
    }
}