package com.brokerage.repository;

import com.brokerage.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(String customerId);
    List<Order> findByCustomerIdAndCreateDateBetween(
            String customerId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}