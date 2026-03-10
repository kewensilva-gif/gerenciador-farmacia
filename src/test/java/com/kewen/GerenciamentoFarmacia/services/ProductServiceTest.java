package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.entities.Product;
import com.kewen.GerenciamentoFarmacia.repositories.ProductRepository;
import com.kewen.GerenciamentoFarmacia.repositories.SaleProductRepository;

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

    @Mock
    private CategoryService categoryService;

    @Mock
    private SaleProductRepository saleProductRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");
        category.setEnabled(true);

        product = new Product();
        product.setId(1L);
        product.setName("Paracetamol 500mg");
        product.setBarcode("7891234560001");
        product.setUnitPrice(new BigDecimal("12.50"));
        product.setStockQuantity(100);
        product.setExpirationDate(LocalDate.of(2026, 12, 31));
        product.setCategory(category);
        product.setEnabled(true);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar o produto")
    void save_deveSalvarERetornarProduto() {
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByBarcode("7891234560001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.save(product);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Paracetamol 500mg");
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("save - deve lançar exceção para preço zero")
    void save_deveLancarExcecaoParaPrecoZero() {
        product.setUnitPrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O preço unitário não pode ser zero ou negativo");
    }

    @Test
    @DisplayName("save - deve lançar exceção para preço negativo")
    void save_deveLancarExcecaoParaPrecoNegativo() {
        product.setUnitPrice(new BigDecimal("-5.00"));

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O preço unitário não pode ser zero ou negativo");
    }

    @Test
    @DisplayName("save - deve lançar exceção para estoque negativo")
    void save_deveLancarExcecaoParaEstoqueNegativo() {
        product.setStockQuantity(-1);

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade em estoque não pode ser negativa");
    }

    @Test
    @DisplayName("save - deve lançar exceção para data de validade expirada")
    void save_deveLancarExcecaoParaDataValidadeExpirada() {
        product.setExpirationDate(LocalDate.of(2020, 1, 1));

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de validade não pode ser anterior à data atual");
    }

    @Test
    @DisplayName("save - deve lançar exceção para categoria inexistente")
    void save_deveLancarExcecaoParaCategoriaInexistente() {
        when(categoryService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Categoria não encontrada");
    }

    @Test
    @DisplayName("save - deve lançar exceção para código de barras duplicado")
    void save_deveLancarExcecaoParaCodigoBarrasDuplicado() {
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByBarcode("7891234560001")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.save(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de barras já cadastrado");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar produto ativo")
    void findById_deveRetornarProdutoAtivo() {
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Paracetamol 500mg");
        verify(productRepository).findByIdAndEnabledTrue(1L);
    }

    @Test
    @DisplayName("findById - deve retornar vazio para produto não encontrado")
    void findById_deveRetornarVazioQuandoNaoEncontrado() {
        when(productRepository.findByIdAndEnabledTrue(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar apenas produtos ativos")
    void findAll_deveRetornarProdutosAtivos() {
        when(productRepository.findByEnabledTrue()).thenReturn(List.of(product));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        verify(productRepository).findByEnabledTrue();
    }

    @Test
    @DisplayName("findByBarcode - deve buscar por código de barras com enabled")
    void findByBarcode_deveBuscarPorBarcodeEEnabled() {
        when(productRepository.findByBarcodeAndEnabledTrue("7891234560001")).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findByBarcode("7891234560001");

        assertThat(result).isPresent();
        verify(productRepository).findByBarcodeAndEnabledTrue("7891234560001");
    }

    @Test
    @DisplayName("findByName - deve buscar por nome com enabled")
    void findByName_deveBuscarPorNomeEEnabled() {
        when(productRepository.findByNameContainingIgnoreCaseAndEnabledTrue("Paracetamol")).thenReturn(List.of(product));

        List<Product> result = productService.findByName("Paracetamol");

        assertThat(result).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCaseAndEnabledTrue("Paracetamol");
    }

    @Test
    @DisplayName("findByCategory - deve buscar por categoria com enabled")
    void findByCategory_deveBuscarPorCategoriaEEnabled() {
        when(productRepository.findByCategoryIdAndEnabledTrue(1L)).thenReturn(List.of(product));

        List<Product> result = productService.findByCategory(1L);

        assertThat(result).hasSize(1);
        verify(productRepository).findByCategoryIdAndEnabledTrue(1L);
    }

    @Test
    @DisplayName("findExpiredProducts - deve buscar produtos vencidos ativos")
    void findExpiredProducts_deveBuscarProdutosVencidosAtivos() {
        when(productRepository.findByExpirationDateBeforeAndEnabledTrue(any(LocalDate.class))).thenReturn(List.of(product));

        List<Product> result = productService.findExpiredProducts();

        assertThat(result).hasSize(1);
        verify(productRepository).findByExpirationDateBeforeAndEnabledTrue(any(LocalDate.class));
    }

    @Test
    @DisplayName("findLowStockProducts - deve buscar produtos com estoque baixo ativos")
    void findLowStockProducts_deveBuscarProdutosEstoqueBaixoAtivos() {
        when(productRepository.findByStockQuantityLessThanAndEnabledTrue(10)).thenReturn(List.of(product));

        List<Product> result = productService.findLowStockProducts(10);

        assertThat(result).hasSize(1);
        verify(productRepository).findByStockQuantityLessThanAndEnabledTrue(10);
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar produto existente")
    void update_deveAtualizarProduto() {
        Product updated = new Product();
        updated.setName("Paracetamol 750mg");
        updated.setUnitPrice(new BigDecimal("15.00"));
        updated.setBarcode("7891234560001");
        updated.setExpirationDate(LocalDate.of(2027, 6, 30));
        updated.setCategory(category);

        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByBarcode("7891234560001")).thenReturn(Optional.of(product));
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.update(1L, updated);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("update - deve lançar exceção para produto não encontrado")
    void update_deveLancarExcecaoParaProdutoNaoEncontrado() {
        Product updated = new Product();
        updated.setUnitPrice(new BigDecimal("15.00"));
        updated.setBarcode("7891234560002");
        updated.setExpirationDate(LocalDate.of(2027, 6, 30));
        updated.setCategory(category);

        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByBarcode("7891234560002")).thenReturn(Optional.empty());
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Produto não encontrado");
    }

    @Test
    @DisplayName("update - deve lançar exceção para barcode duplicado de outro produto")
    void update_deveLancarExcecaoParaBarcodeDuplicado() {
        Product otherProduct = new Product();
        otherProduct.setId(2L);
        otherProduct.setBarcode("7891234560001");

        Product updated = new Product();
        updated.setUnitPrice(new BigDecimal("15.00"));
        updated.setBarcode("7891234560001");
        updated.setExpirationDate(LocalDate.of(2027, 6, 30));
        updated.setCategory(category);

        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByBarcode("7891234560001")).thenReturn(Optional.of(otherProduct));

        assertThatThrownBy(() -> productService.update(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de barras já cadastrado");
    }

    // ======================== STOCK ========================

    @Test
    @DisplayName("debitStock - deve debitar estoque com sucesso")
    void debitStock_deveDebitarEstoque() {
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.debitStock(1L, 10);

        assertThat(product.getStockQuantity()).isEqualTo(90);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("debitStock - deve lançar exceção para quantidade zero ou negativa")
    void debitStock_deveLancarExcecaoParaQuantidadeInvalida() {
        assertThatThrownBy(() -> productService.debitStock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade a debitar deve ser positiva");
    }

    @Test
    @DisplayName("debitStock - deve lançar exceção para estoque insuficiente")
    void debitStock_deveLancarExcecaoParaEstoqueInsuficiente() {
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.debitStock(1L, 200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque insuficiente");
    }

    @Test
    @DisplayName("addStock - deve adicionar estoque com sucesso")
    void addStock_deveAdicionarEstoque() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.addStock(1L, 50);

        assertThat(product.getStockQuantity()).isEqualTo(150);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("addStock - deve lançar exceção para quantidade zero ou negativa")
    void addStock_deveLancarExcecaoParaQuantidadeInvalida() {
        assertThatThrownBy(() -> productService.addStock(1L, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade a adicionar deve ser positiva");
    }

    @Test
    @DisplayName("addStock - usa findById para permitir reposição em produtos desativados")
    void addStock_usaFindByIdParaProdutosDesativados() {
        product.setEnabled(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.addStock(1L, 10);

        verify(productRepository).findById(1L);
    }

    // ======================== SOFT DELETE ========================

    @Test
    @DisplayName("deleteById - deve desativar produto (soft delete)")
    void deleteById_deveDesativarProduto() {
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteById(1L);

        assertThat(product.getEnabled()).isFalse();
        verify(productRepository).save(product);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para produto não encontrado")
    void deleteById_deveLancarExcecaoParaProdutoNaoEncontrado() {
        when(productRepository.findByIdAndEnabledTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Produto não encontrado");
    }
}
