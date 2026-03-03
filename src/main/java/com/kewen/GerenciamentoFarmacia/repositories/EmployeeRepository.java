package com.kewen.GerenciamentoFarmacia.repositories;

import com.kewen.GerenciamentoFarmacia.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByHiringDateAfter(LocalDate date);
    List<Employee> findByHiringDateBefore(LocalDate date);
    List<Employee> findByTerminationDateIsNull();
    List<Employee> findByTerminationDateIsNotNull();
}
