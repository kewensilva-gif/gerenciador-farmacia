package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.entities.Product;
import com.kewen.GerenciamentoFarmacia.repositories.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");

        product = new Product();
        product.setId(1L);
        product.setName("Paracetamol 500mg");
        product.setBarcode("7891234560001");
        product.setUnitPrice(new BigDecimal("12.50"));
        product.setStockQuantity(100);
        product.setExpirationDate(LocalDate.of(2026, 12, 31));
        product.setCategory(category);
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("save - deve salvar e retornar o produto")
    void save_deveSalvarERetornarProduto() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.save(product);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Paracetamol 500mg");
        assertThat(result.getBarcode()).isEqualTo("7891234560001");
        verify(productRepository, times(1)).save(product);
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById - deve retornar Optional com produto quando encontrado")
    void findById_deveRetornarProdutoQuandoEncontrado() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Paracetamol 500mg");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - deve retornar Optional vazio quando não encontrado")
    void findById_deveRetornarVazioQuandoNaoEncontrado() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(99L);

        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(99L);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll - deve retornar lista de produtos")
    void findAll_deveRetornarListaDeProdutos() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        verify(productRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // findByBarcode
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByBarcode - deve retornar produto pelo código de barras")
    void findByBarcode_deveRetornarProduto() {
        when(productRepository.findByBarcode("7891234560001")).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findByBarcode("7891234560001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(productRepository, times(1)).findByBarcode("7891234560001");
    }

    @Test
    @DisplayName("findByBarcode - deve retornar Optional vazio quando código de barras não existe")
    void findByBarcode_deveRetornarVazioQuandoNaoExiste() {
        when(productRepository.findByBarcode("0000000000000")).thenReturn(Optional.empty());

        Optional<Product> result = productService.findByBarcode("0000000000000");

        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findByBarcode("0000000000000");
    }

    // -------------------------------------------------------------------------
    // findByName
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByName - deve retornar lista de produtos pelo nome")
    void findByName_deveRetornarListaPorNome() {
        when(productRepository.findByNameContainingIgnoreCase("paracetamol")).thenReturn(List.of(product));

        List<Product> result = productService.findByName("paracetamol");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("paracetamol");
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("paracetamol");
    }

    // -------------------------------------------------------------------------
    // findByCategory
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCategory - deve retornar produtos da categoria informada")
    void findByCategory_deveRetornarProdutosDaCategoria() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        List<Product> result = productService.findByCategory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Medicamentos");
        verify(productRepository, times(1)).findByCategoryId(1L);
    }

    // -------------------------------------------------------------------------
    // findExpiredProducts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findExpiredProducts - deve retornar produtos vencidos")
    void findExpiredProducts_deveRetornarProdutosVencidos() {
        Product vencido = new Product();
        vencido.setId(2L);
        vencido.setExpirationDate(LocalDate.of(2020, 1, 1));

        when(productRepository.findByExpirationDateBefore(any(LocalDate.class))).thenReturn(List.of(vencido));

        List<Product> result = productService.findExpiredProducts();

        assertThat(result).hasSize(1);
        verify(productRepository, times(1)).findByExpirationDateBefore(any(LocalDate.class));
    }

    // -------------------------------------------------------------------------
    // findLowStockProducts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findLowStockProducts - deve retornar produtos com estoque baixo")
    void findLowStockProducts_deveRetornarProdutosComEstoqueBaixo() {
        Product baixoEstoque = new Product();
        baixoEstoque.setId(3L);
        baixoEstoque.setStockQuantity(2);

        when(productRepository.findByStockQuantityLessThan(10)).thenReturn(List.of(baixoEstoque));

        List<Product> result = productService.findLowStockProducts(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockQuantity()).isLessThan(10);
        verify(productRepository, times(1)).findByStockQuantityLessThan(10);
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("update - deve atualizar e retornar o produto quando encontrado")
    void update_deveAtualizarProdutoQuandoEncontrado() {
        Product detalhes = new Product();
        detalhes.setName("Paracetamol 750mg");
        detalhes.setUnitPrice(new BigDecimal("15.00"));
        detalhes.setBarcode("7891234560001");
        detalhes.setStockQuantity(80);
        detalhes.setExpirationDate(LocalDate.of(2027, 6, 1));
        detalhes.setCategory(category);

        Product atualizado = new Product();
        atualizado.setId(1L);
        atualizado.setName("Paracetamol 750mg");
        atualizado.setUnitPrice(new BigDecimal("15.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(atualizado);

        Product result = productService.update(1L, detalhes);

        assertThat(result.getName()).isEqualTo("Paracetamol 750mg");
        assertThat(result.getUnitPrice()).isEqualByComparingTo("15.00");
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("update - deve lançar RuntimeException quando produto não encontrado")
    void update_deveLancarExcecaoQuandoNaoEncontrado() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, new Product()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Produto não encontrado");

        verify(productRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteById / existsById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteById - deve chamar deleteById no repositório")
    void deleteById_deveChamarDeleteById() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteById(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("existsById - deve retornar true quando produto existe")
    void existsById_deveRetornarTrueQuandoExiste() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertThat(productService.existsById(1L)).isTrue();
        verify(productRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("existsById - deve retornar false quando produto não existe")
    void existsById_deveRetornarFalseQuandoNaoExiste() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThat(productService.existsById(99L)).isFalse();
        verify(productRepository, times(1)).existsById(99L);
    }
}
