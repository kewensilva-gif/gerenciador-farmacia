package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Customer;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.repositories.CustomerRepository;
import com.kewen.GerenciamentoFarmacia.repositories.SaleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PersonService personService;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setFirstname("João");
        person.setLastname("Silva");
        person.setCpf("12345678901");
        person.setCustomer(null);
        person.setEmployee(null);

        customer = new Customer();
        customer.setId(1L);
        customer.setRegistrationDate(LocalDate.of(2024, 1, 15));
        customer.setPerson(person);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar o cliente")
    void save_deveSalvarERetornarCliente() {
        when(personService.findById(1L)).thenReturn(Optional.of(person));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.save(customer);

        assertThat(result).isNotNull();
        assertThat(result.getRegistrationDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa é nula")
    void save_deveLancarExcecaoQuandoPessoaNula() {
        customer.setPerson(null);

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O cliente precisa estar vinculado a uma pessoa");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa não encontrada")
    void save_deveLancarExcecaoQuandoPessoaNaoEncontrada() {
        when(personService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pessoa não encontrada");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa já é cliente")
    void save_deveLancarExcecaoQuandoPessoaJaEhCliente() {
        person.setCustomer(new Customer());
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esta pessoa já está cadastrada como cliente");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando data de cadastro é nula")
    void save_deveLancarExcecaoQuandoDataCadastroNula() {
        customer.setRegistrationDate(null);
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de cadastro é obrigatória");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando data de cadastro é futura")
    void save_deveLancarExcecaoQuandoDataCadastroFutura() {
        customer.setRegistrationDate(LocalDate.now().plusDays(1));
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> customerService.save(customer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de cadastro não pode ser futura");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar cliente quando encontrado")
    void findById_deveRetornarCliente() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findById(1L);

        assertThat(result).isPresent();
        verify(customerRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - deve retornar vazio quando não encontrado")
    void findById_deveRetornarVazioQuandoNaoEncontrado() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar lista de clientes")
    void findAll_deveRetornarListaDeClientes() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByRegistrationAfter - deve retornar clientes cadastrados após a data")
    void findByRegistrationAfter_deveRetornarClientes() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        when(customerRepository.findByRegistrationDateAfter(date)).thenReturn(List.of(customer));

        List<Customer> result = customerService.findByRegistrationAfter(date);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByRegistrationBefore - deve retornar clientes cadastrados antes da data")
    void findByRegistrationBefore_deveRetornarClientes() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(customerRepository.findByRegistrationDateBefore(date)).thenReturn(List.of(customer));

        List<Customer> result = customerService.findByRegistrationBefore(date);

        assertThat(result).hasSize(1);
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar cliente existente")
    void update_deveAtualizarCliente() {
        Customer updated = new Customer();
        updated.setRegistrationDate(LocalDate.of(2024, 6, 1));
        updated.setPerson(person);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.update(1L, updated);

        assertThat(result).isNotNull();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("update - deve lançar exceção para cliente não encontrado")
    void update_deveLancarExcecaoParaClienteNaoEncontrado() {
        Customer updated = new Customer();
        updated.setRegistrationDate(LocalDate.of(2024, 6, 1));
        updated.setPerson(person);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(1L, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cliente não encontrado");
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve excluir cliente com sucesso")
    void deleteById_deveExcluirCliente() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(saleRepository.existsByCustomerId(1L)).thenReturn(false);

        customerService.deleteById(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando cliente tem vendas")
    void deleteById_deveLancarExcecaoQuandoTemVendas() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(saleRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.deleteById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("possui vendas vinculadas");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para cliente não encontrado")
    void deleteById_deveLancarExcecaoParaClienteNaoEncontrado() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cliente não encontrado");
    }
}
