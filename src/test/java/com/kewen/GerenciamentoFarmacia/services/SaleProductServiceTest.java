package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Category;
import com.kewen.GerenciamentoFarmacia.entities.Product;
import com.kewen.GerenciamentoFarmacia.entities.SaleProduct;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleProductServiceTest {

    @Mock
    private SaleProductRepository saleProductRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private SaleProductService saleProductService;

    private Product product;
    private SaleProduct saleProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Medicamentos");
        category.setEnabled(true);

        product = new Product();
        product.setId(1L);
        product.setName("Paracetamol 500mg");
        product.setUnitPrice(new BigDecimal("12.50"));
        product.setStockQuantity(100);
        product.setExpirationDate(LocalDate.of(2026, 12, 31));
        product.setCategory(category);
        product.setEnabled(true);

        saleProduct = new SaleProduct();
        saleProduct.setId(1L);
        saleProduct.setProduct(product);
        saleProduct.setQuantity(5L);
    }

    // ======================== PREPARE ITEM ========================

    @Test
    @DisplayName("prepareItem - deve preparar item com sucesso")
    void prepareItem_devePrepararItemComSucesso() {
        when(productService.findById(1L)).thenReturn(Optional.of(product));

        saleProductService.prepareItem(saleProduct);

        assertThat(saleProduct.getUnitPrice()).isEqualTo(new BigDecimal("12.50"));
        verify(productService).debitStock(1L, 5);
    }

    @Test
    @DisplayName("prepareItem - deve lançar exceção para produto não encontrado ou desativado")
    void prepareItem_deveLancarExcecaoParaProdutoNaoEncontrado() {
        when(productService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleProductService.prepareItem(saleProduct))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Produto não encontrado ou desativado");
    }

    @Test
    @DisplayName("prepareItem - deve lançar exceção para produto vencido")
    void prepareItem_deveLancarExcecaoParaProdutoVencido() {
        product.setExpirationDate(LocalDate.of(2020, 1, 1));
        when(productService.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> saleProductService.prepareItem(saleProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produto vencido não pode ser vendido");
    }

    @Test
    @DisplayName("prepareItem - deve lançar exceção para quantidade zero ou negativa")
    void prepareItem_deveLancarExcecaoParaQuantidadeInvalida() {
        saleProduct.setQuantity(0L);

        assertThatThrownBy(() -> saleProductService.prepareItem(saleProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade do produto da venda deve ser positiva");
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve restaurar estoque e deletar item")
    void deleteById_deveRestaurarEstoqueEDeletar() {
        when(saleProductRepository.findById(1L)).thenReturn(Optional.of(saleProduct));

        saleProductService.deleteById(1L);

        verify(productService).addStock(1L, 5);
        verify(saleProductRepository).delete(saleProduct);
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para item não encontrado")
    void deleteById_deveLancarExcecaoParaItemNaoEncontrado() {
        when(saleProductRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleProductService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item não encontrado");
    }

    // ======================== RESTORE STOCK ========================

    @Test
    @DisplayName("restoreStock - deve restaurar estoque do produto")
    void restoreStock_deveRestaurarEstoque() {
        saleProductService.restoreStock(saleProduct);

        verify(productService).addStock(1L, 5);
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar item quando encontrado")
    void findById_deveRetornarItem() {
        when(saleProductRepository.findById(1L)).thenReturn(Optional.of(saleProduct));

        Optional<SaleProduct> result = saleProductService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findAll - deve retornar todos os itens")
    void findAll_deveRetornarTodos() {
        when(saleProductRepository.findAll()).thenReturn(List.of(saleProduct));

        List<SaleProduct> result = saleProductService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findBySaleId - deve retornar itens da venda")
    void findBySaleId_deveRetornarItensDaVenda() {
        when(saleProductRepository.findBySaleId(1L)).thenReturn(List.of(saleProduct));

        List<SaleProduct> result = saleProductService.findBySaleId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByProductId - deve retornar itens do produto")
    void findByProductId_deveRetornarItensDoProduto() {
        when(saleProductRepository.findByProductId(1L)).thenReturn(List.of(saleProduct));

        List<SaleProduct> result = saleProductService.findByProductId(1L);

        assertThat(result).hasSize(1);
    }
}
