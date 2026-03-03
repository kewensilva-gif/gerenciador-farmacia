package com.kewen.GerenciamentoFarmacia.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "person", schema = "public")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstname;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastname;

    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @OneToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;
}
