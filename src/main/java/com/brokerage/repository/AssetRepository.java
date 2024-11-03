package com.brokerage.repository;

import com.brokerage.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// AssetRepository.java
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerIdAndAssetName(String customerId, String assetName);
    List<Asset> findByCustomerId(String customerId);
}