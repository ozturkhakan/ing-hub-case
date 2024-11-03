package com.brokerage.model.dto;

import com.brokerage.model.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// OrderRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String customerId;
    private String assetName;
    private OrderSide side;
    private Double size;
    private Double price;

    @Override
    public String toString() {
        return "OrderRequest{" +
            "customerId='" + customerId + '\'' +
            ", assetName='" + assetName + '\'' +
            ", side=" + side +
            ", size=" + size +
            ", price=" + price +
            '}';
    }
}