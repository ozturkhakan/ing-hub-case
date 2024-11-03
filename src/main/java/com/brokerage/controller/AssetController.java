package com.brokerage.controller;

import com.brokerage.model.Asset;
import com.brokerage.model.dto.DepositRequest;
import com.brokerage.model.dto.WithdrawRequest;
import com.brokerage.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@Slf4j
public class AssetController {
    
    @Autowired
    private AssetService assetService;

    @GetMapping
    public ResponseEntity<List<Asset>> listAssets(@RequestParam String customerId) {
        log.info("Fetching assets for customer: {}", customerId);
        return ResponseEntity.ok(assetService.listAssets(customerId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestBody DepositRequest request) {
        log.info("Processing deposit request: {}", request);
        assetService.deposit(request.getCustomerId(), request.getAmount());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestBody WithdrawRequest request) {
        log.info("Processing withdraw request: {}", request);
        assetService.withdraw(request.getCustomerId(), request.getAmount(), request.getIban()); //everything is based on TRY
        return ResponseEntity.ok().build();
    }
}