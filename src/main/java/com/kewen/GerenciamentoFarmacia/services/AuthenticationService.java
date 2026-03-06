package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.dto.auth.AuthRequest;
import com.kewen.GerenciamentoFarmacia.dto.auth.AuthResponse;
import com.kewen.GerenciamentoFarmacia.dto.auth.RegisterRequest;
import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.entities.User;
import com.kewen.GerenciamentoFarmacia.repositories.RoleRepository;
import com.kewen.GerenciamentoFarmacia.repositories.UserRepository;
import com.kewen.GerenciamentoFarmacia.security.CustomUserDetailsService;
import com.kewen.GerenciamentoFarmacia.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username já está em uso");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        Role defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("CUSTOMER");
                    return roleRepository.save(role);
                });

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(savedUser.getUsername()));

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );

        User user = userRepository.findByUsernameOrEmail(request.login(), request.login())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
