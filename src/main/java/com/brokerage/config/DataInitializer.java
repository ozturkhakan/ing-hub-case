package com.brokerage.config;

import com.brokerage.model.Customer;
import com.brokerage.repository.CustomerRepository;
import com.brokerage.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssetService assetService;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            log.info("Initializing test data...");

            // Create admin user
            Customer admin = Customer.builder()
                    .customerId("ADMIN1")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();
            customerRepository.save(admin);

            // Create test user
            Customer user = Customer.builder()
                    .customerId("CUST1")
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role("USER")
                    .build();
            customerRepository.save(user);

            // Add initial TRY balance for test user
            assetService.deposit("CUST1", 10000.0);

            log.info("Test data initialized");
        }
    }
}
