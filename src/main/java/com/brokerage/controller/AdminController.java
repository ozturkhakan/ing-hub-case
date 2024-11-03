package com.brokerage.controller;

import com.brokerage.model.Order;
import com.brokerage.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Slf4j
public class AdminController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping("/{orderId}/match")
    public ResponseEntity<Order> matchOrder(@PathVariable Long orderId) {
        try {
            log.info("Matching order with ID: {}", orderId);
            return ResponseEntity.ok(orderService.matchOrder(orderId));
        } catch (Exception e) {
            log.error("Error matching order: ", e);
            throw e;
        }
    }
}