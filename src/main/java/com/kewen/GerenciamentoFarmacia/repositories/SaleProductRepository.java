package com.kewen.GerenciamentoFarmacia.repositories;

import com.kewen.GerenciamentoFarmacia.entities.SaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleProductRepository extends JpaRepository<SaleProduct, Long> {
    List<SaleProduct> findBySaleId(Long saleId);
    List<SaleProduct> findByProductId(Long productId);
}
