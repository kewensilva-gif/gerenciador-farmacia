package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.entities.User;
import com.kewen.GerenciamentoFarmacia.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        role = new Role();
        role.setUuid(UUID.randomUUID());
        role.setName("ADMIN");

        user = new User();
        user.setUuid(userId);
        user.setUsername("admin");
        user.setEmail("admin@farmacia.com");
        user.setPassword("password123");
        user.setEnabled(true);
        user.setRole(role);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar usuário com senha codificada")
    void save_deveSalvarUsuarioComSenhaCodificada() {
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.save(user);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("save - deve lançar exceção para username ou email duplicado")
    void save_deveLancarExcecaoParaDuplicata() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.save(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username ou email já existe");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar usuário quando encontrado")
    void findById_deveRetornarUsuario() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findById - deve retornar vazio quando não encontrado")
    void findById_deveRetornarVazioQuandoNaoEncontrado() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(randomId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar lista de usuários")
    void findAll_deveRetornarListaDeUsuarios() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByUsername - deve retornar usuário por username")
    void findByUsername_deveRetornarUsuarioPorUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("admin");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findByEmail - deve retornar usuário por email")
    void findByEmail_deveRetornarUsuarioPorEmail() {
        when(userRepository.findByEmail("admin@farmacia.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("admin@farmacia.com");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findEnabled - deve retornar usuários ativos")
    void findEnabled_deveRetornarUsuariosAtivos() {
        when(userRepository.findByEnabledTrue()).thenReturn(List.of(user));

        List<User> result = userService.findEnabled();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findDisabled - deve retornar usuários inativos")
    void findDisabled_deveRetornarUsuariosInativos() {
        when(userRepository.findByEnabledFalse()).thenReturn(List.of());

        List<User> result = userService.findDisabled();

        assertThat(result).isEmpty();
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar usuário existente")
    void update_deveAtualizarUsuario() {
        User updated = new User();
        updated.setUsername("admin_updated");
        updated.setEmail("updated@farmacia.com");
        updated.setPassword("newpassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("admin_updated")).thenReturn(false);
        when(userRepository.existsByEmail("updated@farmacia.com")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.update(userId, updated);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("update - deve lançar exceção para usuário não encontrado")
    void update_deveLancarExcecaoParaUsuarioNaoEncontrado() {
        User updated = new User();
        updated.setUsername("admin_updated");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    @DisplayName("update - deve lançar exceção para username duplicado")
    void update_deveLancarExcecaoParaUsernameDuplicado() {
        User updated = new User();
        updated.setUsername("other_user");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("other_user")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username já existe");
    }

    @Test
    @DisplayName("update - deve lançar exceção para email duplicado")
    void update_deveLancarExcecaoParaEmailDuplicado() {
        User updated = new User();
        updated.setUsername("admin");
        updated.setEmail("other@farmacia.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@farmacia.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(userId, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email já existe");
    }

    @Test
    @DisplayName("update - não deve validar username quando não alterado")
    void update_naoDeveValidarUsernameQuandoNaoAlterado() {
        User updated = new User();
        updated.setUsername("admin");
        updated.setEmail("admin@farmacia.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.update(userId, updated);

        assertThat(result).isNotNull();
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve deletar usuário")
    void deleteById_deveDeletarUsuario() {
        userService.deleteById(userId);

        verify(userRepository).deleteById(userId);
    }
}
