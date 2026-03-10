package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Role;
import com.kewen.GerenciamentoFarmacia.repositories.RoleRepository;
import com.kewen.GerenciamentoFarmacia.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();

        role = new Role();
        role.setUuid(roleId);
        role.setName("ADMIN");
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar a role")
    void save_deveSalvarERetornarRole() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role result = roleService.save(role);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ADMIN");
        verify(roleRepository).save(role);
    }

    @Test
    @DisplayName("save - deve lançar exceção para nome duplicado")
    void save_deveLancarExcecaoParaNomeDuplicado() {
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> roleService.save(role))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Já existe uma Role com nome");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar role quando encontrada")
    void findById_deveRetornarRole() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        Optional<Role> result = roleService.findById(roleId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("findById - deve retornar vazio quando não encontrada")
    void findById_deveRetornarVazioQuandoNaoEncontrada() {
        UUID randomId = UUID.randomUUID();
        when(roleRepository.findById(randomId)).thenReturn(Optional.empty());

        Optional<Role> result = roleService.findById(randomId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar lista de roles")
    void findAll_deveRetornarListaDeRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<Role> result = roleService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByName - deve retornar role por nome")
    void findByName_deveRetornarRolePorNome() {
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        Optional<Role> result = roleService.findByName("ADMIN");

        assertThat(result).isPresent();
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar role existente")
    void update_deveAtualizarRole() {
        Role updated = new Role();
        updated.setName("SUPER_ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("SUPER_ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role result = roleService.update(roleId, updated);

        assertThat(result).isNotNull();
        verify(roleRepository).save(role);
    }

    @Test
    @DisplayName("update - deve lançar exceção para role não encontrada")
    void update_deveLancarExcecaoParaRoleNaoEncontrada() {
        Role updated = new Role();
        updated.setName("SUPER_ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.update(roleId, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role não encontrada");
    }

    @Test
    @DisplayName("update - deve lançar exceção para nome nulo")
    void update_deveLancarExcecaoParaNomeNulo() {
        Role updated = new Role();
        updated.setName(null);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.update(roleId, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome da role é obrigatório");
    }

    @Test
    @DisplayName("update - deve lançar exceção para nome duplicado de outra role")
    void update_deveLancarExcecaoParaNomeDuplicado() {
        Role updated = new Role();
        updated.setName("EMPLOYEE");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsByName("EMPLOYEE")).thenReturn(true);

        assertThatThrownBy(() -> roleService.update(roleId, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma Role com nome");
    }

    @Test
    @DisplayName("update - não deve verificar duplicata quando nome não alterou")
    void update_naoDeveVerificarDuplicataQuandoNaoAlterou() {
        Role updated = new Role();
        updated.setName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role result = roleService.update(roleId, updated);

        assertThat(result).isNotNull();
        verify(roleRepository, never()).existsByName(anyString());
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve excluir role com sucesso")
    void deleteById_deveExcluirRole() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoleUuid(roleId)).thenReturn(false);

        roleService.deleteById(roleId);

        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando role tem usuários vinculados")
    void deleteById_deveLancarExcecaoQuandoTemUsuarios() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoleUuid(roleId)).thenReturn(true);

        assertThatThrownBy(() -> roleService.deleteById(roleId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vinculada a usuários");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para role não encontrada")
    void deleteById_deveLancarExcecaoParaRoleNaoEncontrada() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteById(roleId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role não encontrada");
    }
}
