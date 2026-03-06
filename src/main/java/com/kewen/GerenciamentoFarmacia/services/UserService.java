package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.User;
import com.kewen.GerenciamentoFarmacia.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User save(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalAccessError("Username ou email já existe");
        }
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    public List<User> findEnabled() {
        return userRepository.findByEnabledTrue();
    }

    public List<User> findDisabled() {
        return userRepository.findByEnabledFalse();
    }

    public User update(UUID id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validateUsername(userDetails.getUsername(), existingUser);
        validateEmail(userDetails.getEmail(), existingUser);

        updateFields(existingUser, userDetails);

        return userRepository.save(existingUser);
    }

    private void validateUsername(String newUsername, User existingUser) {
        if (newUsername == null) return;

        boolean usernameChanged = !newUsername.equals(existingUser.getUsername());

        if (usernameChanged && userRepository.existsByUsername(newUsername)) {
            throw new IllegalAccessError("Username já existe");
        }
    }

    private void validateEmail(String newEmail, User existingUser) {
        if (newEmail == null) return;

        boolean emailChanged = !newEmail.equals(existingUser.getEmail());

        if (emailChanged && userRepository.existsByEmail(newEmail)) {
            throw new IllegalAccessError("Email já existe");
        }
    }

    private void updateFields(User existingUser, User userDetails) {
        if (userDetails.getUsername() != null) {
            existingUser.setUsername(userDetails.getUsername());
        }

        if (userDetails.getEmail() != null) {
            existingUser.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPassword() != null) {
            existingUser.setPassword(userDetails.getPassword());
        }

        if (userDetails.getEnabled() != null) {
            existingUser.setEnabled(userDetails.getEnabled());
        }

        if (userDetails.getRole() != null) {
            existingUser.setRole(userDetails.getRole());
        }
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
