package com.brokerage.controller;

import com.brokerage.model.Customer;
import com.brokerage.model.Order;
import com.brokerage.model.dto.OrderRequest;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    private void validateCustomerAccess(String customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Admin can access everything
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Not authorized to access this data");
        }
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        log.info("Creating order: {}", request);
        validateCustomerAccess(request.getCustomerId());

        Order order = orderService.createOrder(request);
        log.info("Created order: {}", order);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // If customerId is provided, validate access
        if (customerId != null) {
            validateCustomerAccess(customerId);
        } else {
            // If no customerId provided, only admin can see all orders
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                throw new RuntimeException("Not authorized to access all orders");
            }
        }

        if (customerId != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(orderService.getOrdersByDateRange(customerId, startDate, endDate));
        } else if (customerId != null) {
            return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
        } else {
            return ResponseEntity.ok(orderService.getAllOrders());
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order: {}", orderId);

        // Get the order first
        Order order = orderService.getOrder(orderId);
        // Validate access before cancelling
        validateCustomerAccess(order.getCustomerId());

        Order cancelledOrder = orderService.cancelOrder(orderId);
        log.info("Cancelled order: {}", cancelledOrder);
        return ResponseEntity.ok(cancelledOrder);
    }
}