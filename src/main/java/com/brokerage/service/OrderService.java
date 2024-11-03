package com.brokerage.service;

import com.brokerage.model.Asset;
import com.brokerage.model.Order;
import com.brokerage.model.dto.OrderRequest;
import com.brokerage.model.enums.OrderSide;
import com.brokerage.model.enums.OrderStatus;
import com.brokerage.repository.AssetRepository;
import com.brokerage.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    public Order createOrder(OrderRequest request) {
        log.info("Creating new order from request: {}", request);

        // Validate and update balances first
        validateAndUpdateBalance(request);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .assetName(request.getAssetName())
                .orderSide(request.getSide())
                .size(request.getSize())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        log.info("Saving order: {}", order);
        return orderRepository.save(order);
    }

    private void validateAndUpdateBalance(OrderRequest request) {
        if (request.getSide() == OrderSide.BUY) {
            // For buy orders, check and reserve TRY balance
            Asset tryAsset = assetService.getAsset(request.getCustomerId(), "TRY");
            double requiredAmount = request.getSize() * request.getPrice();

            if (tryAsset.getUsableSize() < requiredAmount) {
                throw new RuntimeException("Insufficient TRY balance for order");
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize() - requiredAmount);
            assetService.updateAsset(tryAsset);
        } else {
            // For sell orders, check and reserve asset balance
            Asset asset = assetService.getAsset(request.getCustomerId(), request.getAssetName());

            if (asset.getUsableSize() < request.getSize()) {
                throw new RuntimeException("Insufficient asset balance for order");
            }

            asset.setUsableSize(asset.getUsableSize() - request.getSize());
            assetService.updateAsset(asset);
        }
    }

    // Bonus 2: Order Matching
    public Order matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be matched");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            executeBuyOrder(order);
        } else {
            executeSellOrder(order);
        }

        order.setStatus(OrderStatus.MATCHED);
        return orderRepository.save(order);
    }

    private void executeBuyOrder(Order order) {
        Asset tryAsset = assetService.getAsset(order.getCustomerId(), "TRY");
        Asset assetToBuy = assetService.getOrCreateAsset(order.getCustomerId(), order.getAssetName());

        double totalCost = order.getSize() * order.getPrice();

        // Update TRY balance
        tryAsset.setSize(tryAsset.getSize() - totalCost);
        assetService.updateAsset(tryAsset);

        // Update bought asset
        assetToBuy.setSize(assetToBuy.getSize() + order.getSize());
        assetToBuy.setUsableSize(assetToBuy.getUsableSize() + order.getSize());
        assetService.updateAsset(assetToBuy);
    }

    private void executeSellOrder(Order order) {
        Asset tryAsset = assetService.getAsset(order.getCustomerId(), "TRY");
        Asset assetToSell = assetService.getAsset(order.getCustomerId(), order.getAssetName());

        double totalEarning = order.getSize() * order.getPrice();

        // Update asset balance
        assetToSell.setSize(assetToSell.getSize() - order.getSize());
        assetService.updateAsset(assetToSell);

        // Update TRY balance
        tryAsset.setSize(tryAsset.getSize() + totalEarning);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + totalEarning);
        assetService.updateAsset(tryAsset);
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByDateRange(String customerId,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(
                customerId, startDate, endDate);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }


    public Order cancelOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be canceled");
        }

        // Return reserved balances based on order side
        if (order.getOrderSide() == OrderSide.BUY) {
            // Return reserved TRY
            Asset tryAsset = assetService.getAsset(order.getCustomerId(), "TRY");
            double reservedAmount = order.getSize() * order.getPrice();
            tryAsset.setUsableSize(tryAsset.getUsableSize() + reservedAmount);
            assetService.updateAsset(tryAsset);
            log.info("Returned reserved TRY amount: {} for order: {}", reservedAmount, order.getId());
        } else {
            // Return reserved asset amount
            Asset asset = assetService.getAsset(order.getCustomerId(), order.getAssetName());
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetService.updateAsset(asset);
            log.info("Returned reserved {} amount: {} for order: {}",
                    order.getAssetName(), order.getSize(), order.getId());
        }

        order.setStatus(OrderStatus.CANCELED);
        return orderRepository.save(order);
    }

}