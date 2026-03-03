package com.kewen.GerenciamentoFarmacia.repositories;

import com.kewen.GerenciamentoFarmacia.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByRegistrationDateAfter(LocalDate date);
    List<Customer> findByRegistrationDateBefore(LocalDate date);
}
