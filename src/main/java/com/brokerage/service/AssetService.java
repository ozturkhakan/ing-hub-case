package com.brokerage.service;

import com.brokerage.model.Asset;
import com.brokerage.repository.AssetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class AssetService {
    
    @Autowired
    private AssetRepository assetRepository;

    public void deposit(String customerId, Double amount) {
        log.info("Depositing {} for customer {}", amount, customerId);
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
            .orElse(new Asset(null, customerId, "TRY", 0.0, 0.0));
        
        tryAsset.setSize(tryAsset.getSize() + amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + amount);
        assetRepository.save(tryAsset);
    }

    public void withdraw(String customerId, Double amount, String iban) {
        log.info("Withdrawing {} for customer {} to IBAN {}", amount, customerId, iban);
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY")
            .orElseThrow(() -> new RuntimeException("No TRY asset found for customer"));
            
        if (tryAsset.getUsableSize() < amount) {
            throw new RuntimeException("Insufficient balance for withdrawal");
        }
        
        tryAsset.setSize(tryAsset.getSize() - amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() - amount);
        assetRepository.save(tryAsset);
    }

    public List<Asset> listAssets(String customerId) {
        return assetRepository.findByCustomerId(customerId);
    }

    public Asset getAsset(String customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
            .orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    public Asset getOrCreateAsset(String customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElse(new Asset(null, customerId, assetName, 0.0, 0.0));
    }

    public void updateAsset(Asset asset) {
        assetRepository.save(asset);
    }
}