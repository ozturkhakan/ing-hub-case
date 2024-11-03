package com.brokerage;

import com.brokerage.model.Asset;
import com.brokerage.model.Order;
import com.brokerage.model.dto.OrderRequest;
import com.brokerage.model.enums.OrderSide;
import com.brokerage.model.enums.OrderStatus;
import com.brokerage.repository.AssetRepository;
import com.brokerage.repository.OrderRepository;
import com.brokerage.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private AssetRepository assetRepository;

    private static final String CUSTOMER_ID = "CUST1";
    private static final String ASSET_NAME = "AAPL";
    private static final double INITIAL_TRY_BALANCE = 10000.0;

    @BeforeEach
    void setUp() {
        // Setup initial TRY asset
        Asset tryAsset = Asset.builder()
                .customerId(CUSTOMER_ID)
                .assetName("TRY")
                .size(INITIAL_TRY_BALANCE)
                .usableSize(INITIAL_TRY_BALANCE)
                .build();

        // Setup mock responses
        when(assetRepository.findByCustomerIdAndAssetName(CUSTOMER_ID, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(Asset.class)))
                .thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    @DisplayName("Should create buy order when sufficient TRY balance exists")
    void createBuyOrder_withSufficientBalance_shouldSucceed() {
        // Arrange
        OrderRequest request = new OrderRequest(CUSTOMER_ID, ASSET_NAME, OrderSide.BUY, 10.0, 150.0);
        Order expectedOrder = Order.builder()
                .customerId(CUSTOMER_ID)
                .assetName(ASSET_NAME)
                .orderSide(OrderSide.BUY)
                .size(10.0)
                .price(150.0)
                .status(OrderStatus.PENDING)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertEquals(OrderStatus.PENDING, result.getStatus());

        // Verify order creation
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verify all TRY asset interactions
        verify(assetRepository, atLeastOnce()).findByCustomerIdAndAssetName(eq(CUSTOMER_ID), eq("TRY"));

        // Verify final asset state
        verify(assetRepository, atLeastOnce()).save(argThat(asset ->
                asset.getCustomerId().equals(CUSTOMER_ID) &&
                        asset.getAssetName().equals("TRY") &&
                        asset.getUsableSize() == INITIAL_TRY_BALANCE - (request.getSize() * request.getPrice())
        ));
    }

    @Test
    @DisplayName("Should fail to create buy order when insufficient TRY balance")
    void createBuyOrder_withInsufficientBalance_shouldThrowException() {
        // Arrange
        OrderRequest request = new OrderRequest(CUSTOMER_ID, ASSET_NAME, OrderSide.BUY, 1000.0, 150.0);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request),
                "Insufficient TRY balance for order");

        // Verify no order was saved
        verify(orderRepository, never()).save(any(Order.class));
    }
}