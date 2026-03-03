package com.kewen.GerenciamentoFarmacia.repositories;

import com.kewen.GerenciamentoFarmacia.entities.Sale;
import com.kewen.GerenciamentoFarmacia.enums.PaymentMethodEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByPaymentMethod(PaymentMethodEnum paymentMethod);
    List<Sale> findByTotalPriceGreaterThan(BigDecimal price);
    List<Sale> findByTotalPriceLessThan(BigDecimal price);
}
