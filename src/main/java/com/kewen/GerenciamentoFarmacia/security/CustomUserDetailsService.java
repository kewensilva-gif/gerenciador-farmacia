package com.kewen.GerenciamentoFarmacia.security;

import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.entities.User;
import com.kewen.GerenciamentoFarmacia.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(Boolean.FALSE.equals(user.getEnabled()))
                .authorities(mapAuthorities(user))
                .build();
    }

    private List<GrantedAuthority> mapAuthorities(User user) {
        if (user.getRoles() == null) {
            return List.of();
        }

        return user.getRoles().stream()
                .map(Role::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
