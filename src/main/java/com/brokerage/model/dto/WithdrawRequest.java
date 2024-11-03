package com.brokerage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    private String customerId;
    private Double amount;
    private String iban;
}
