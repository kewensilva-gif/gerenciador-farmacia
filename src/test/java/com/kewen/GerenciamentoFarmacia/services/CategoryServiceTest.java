package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.repositories.CategoryRepository;
import com.kewen.GerenciamentoFarmacia.repositories.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");
        category.setEnabled(true);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar a categoria")
    void save_deveSalvarERetornarCategoria() {
        when(categoryRepository.existsByNameAndEnabledTrue("Medicamentos")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.save(category);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Medicamentos");
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("save - deve lançar exceção para nome nulo")
    void save_deveLancarExcecaoParaNomeNulo() {
        category.setName(null);

        assertThatThrownBy(() -> categoryService.save(category))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da categoria é obrigatório");
    }

    @Test
    @DisplayName("save - deve lançar exceção para nome em branco")
    void save_deveLancarExcecaoParaNomeEmBranco() {
        category.setName("   ");

        assertThatThrownBy(() -> categoryService.save(category))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da categoria é obrigatório");
    }

    @Test
    @DisplayName("save - deve lançar exceção para nome duplicado")
    void save_deveLancarExcecaoParaNomeDuplicado() {
        when(categoryRepository.existsByNameAndEnabledTrue("Medicamentos")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.save(category))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma categoria com o nome");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar categoria ativa")
    void findById_deveRetornarCategoriaAtiva() {
        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Medicamentos");
        verify(categoryRepository).findByIdAndEnabledTrue(1L);
    }

    @Test
    @DisplayName("findById - deve retornar vazio para categoria não encontrada")
    void findById_deveRetornarVazioQuandoNaoEncontrada() {
        when(categoryRepository.findByIdAndEnabledTrue(99L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar apenas categorias ativas")
    void findAll_deveRetornarCategoriasAtivas() {
        when(categoryRepository.findByEnabledTrue()).thenReturn(List.of(category));

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        verify(categoryRepository).findByEnabledTrue();
    }

    @Test
    @DisplayName("findByName - deve buscar por nome com enabled")
    void findByName_deveBuscarPorNomeEEnabled() {
        when(categoryRepository.findByNameAndEnabledTrue("Medicamentos")).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findByName("Medicamentos");

        assertThat(result).isPresent();
        verify(categoryRepository).findByNameAndEnabledTrue("Medicamentos");
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar categoria existente")
    void update_deveAtualizarCategoriaExistente() {
        Category updated = new Category();
        updated.setName("Cosméticos");

        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameAndEnabledTrue("Cosméticos")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.update(1L, updated);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("update - deve lançar exceção para categoria não encontrada")
    void update_deveLancarExcecaoParaCategoriaNaoEncontrada() {
        Category updated = new Category();
        updated.setName("Cosméticos");

        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(1L, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Categoria não encontrada");
    }

    @Test
    @DisplayName("update - deve lançar exceção para nome duplicado de outra categoria")
    void update_deveLancarExcecaoParaNomeDuplicado() {
        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Cosméticos");

        Category updated = new Category();
        updated.setName("Cosméticos");

        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameAndEnabledTrue("Cosméticos")).thenReturn(Optional.of(otherCategory));

        assertThatThrownBy(() -> categoryService.update(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma categoria com o nome");
    }

    @Test
    @DisplayName("update - deve lançar exceção para nome em branco")
    void update_deveLancarExcecaoParaNomeEmBranco() {
        Category updated = new Category();
        updated.setName("");

        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.update(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome da categoria é obrigatório");
    }

    // ======================== SOFT DELETE ========================

    @Test
    @DisplayName("deleteById - deve desativar categoria (soft delete)")
    void deleteById_deveDesativarCategoria() {
        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryIdAndEnabledTrue(1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.deleteById(1L);

        assertThat(category.getEnabled()).isFalse();
        verify(categoryRepository).save(category);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando categoria tem produtos ativos")
    void deleteById_deveLancarExcecaoQuandoTemProdutosAtivos() {
        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryIdAndEnabledTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("possui produtos ativos vinculados");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para categoria não encontrada")
    void deleteById_deveLancarExcecaoParaCategoriaNaoEncontrada() {
        when(categoryRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Categoria não encontrada");
    }
}
