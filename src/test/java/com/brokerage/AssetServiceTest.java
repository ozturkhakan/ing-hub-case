package com.brokerage;

import com.brokerage.model.Asset;
import com.brokerage.repository.AssetRepository;
import com.brokerage.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AssetServiceTest {
    @Autowired
    private AssetService assetService;
    
    @MockBean
    private AssetRepository assetRepository;

    private static final String CUSTOMER_ID = "CUST1";
    private static final double INITIAL_BALANCE = 1000.0;

    @BeforeEach
    void setUp() {
        Asset tryAsset = Asset.builder()
            .customerId(CUSTOMER_ID)
            .assetName("TRY")
            .size(INITIAL_BALANCE)
            .usableSize(INITIAL_BALANCE)
            .build();
        when(assetRepository.findByCustomerIdAndAssetName(CUSTOMER_ID, "TRY"))
            .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(Asset.class)))
            .thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    @DisplayName("Should successfully deposit money")
    void deposit_shouldIncreaseBalance() {
        // Arrange
        double depositAmount = 500.0;

        // Act
        assetService.deposit(CUSTOMER_ID, depositAmount);

        // Assert
        verify(assetRepository).save(argThat(asset -> 
            asset.getSize() == INITIAL_BALANCE + depositAmount &&
            asset.getUsableSize() == INITIAL_BALANCE + depositAmount));
    }

    @Test
    @DisplayName("Should fail withdrawal when insufficient balance")
    void withdraw_withInsufficientBalance_shouldThrowException() {
        // Arrange
        double excessAmount = INITIAL_BALANCE + 100.0;

        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> assetService.withdraw(CUSTOMER_ID, excessAmount, "TR123456789"));
    }
}