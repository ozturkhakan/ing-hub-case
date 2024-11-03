package com.brokerage.security;

import com.brokerage.model.Customer;
import com.brokerage.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// AuthService.java
@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        return "Login successful"; // In real implementation, return JWT token
    }
}