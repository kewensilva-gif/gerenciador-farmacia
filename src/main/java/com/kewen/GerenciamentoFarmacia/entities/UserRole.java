package com.kewen.GerenciamentoFarmacia.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_role", schema = "public")
public class UserRole {
    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "role_uuid", nullable = false)
    private Role role;
}
