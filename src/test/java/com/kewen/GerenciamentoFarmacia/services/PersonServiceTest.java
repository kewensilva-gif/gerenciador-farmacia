package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Customer;
import com.kewen.GerenciamentoFarmacia.entities.Employee;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.repositories.PersonRepository;

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
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setFirstname("João");
        person.setLastname("Silva");
        person.setCpf("12345678901");
        person.setEmployee(null);
        person.setCustomer(null);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar a pessoa")
    void save_deveSalvarERetornarPessoa() {
        when(personRepository.existsByCpf("12345678901")).thenReturn(false);
        when(personRepository.save(any(Person.class))).thenReturn(person);

        Person result = personService.save(person);

        assertThat(result).isNotNull();
        assertThat(result.getFirstname()).isEqualTo("João");
        verify(personRepository).save(person);
    }

    @Test
    @DisplayName("save - deve lançar exceção para CPF nulo")
    void save_deveLancarExcecaoParaCpfNulo() {
        person.setCpf(null);

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF é obrigatório");
    }

    @Test
    @DisplayName("save - deve lançar exceção para CPF em branco")
    void save_deveLancarExcecaoParaCpfEmBranco() {
        person.setCpf("   ");

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF é obrigatório");
    }

    @Test
    @DisplayName("save - deve lançar exceção para CPF com formato inválido")
    void save_deveLancarExcecaoParaCpfFormatoInvalido() {
        person.setCpf("123456789");

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF deve conter exatamente 11 dígitos numéricos");
    }

    @Test
    @DisplayName("save - deve lançar exceção para CPF com caracteres não numéricos")
    void save_deveLancarExcecaoParaCpfComCaracteres() {
        person.setCpf("123.456.789");

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O CPF deve conter exatamente 11 dígitos numéricos");
    }

    @Test
    @DisplayName("save - deve lançar exceção para CPF duplicado")
    void save_deveLancarExcecaoParaCpfDuplicado() {
        when(personRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF já cadastrado");
    }

    @Test
    @DisplayName("save - deve lançar exceção para primeiro nome nulo")
    void save_deveLancarExcecaoParaPrimeiroNomeNulo() {
        person.setFirstname(null);
        when(personRepository.existsByCpf("12345678901")).thenReturn(false);

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O primeiro nome é obrigatório");
    }

    @Test
    @DisplayName("save - deve lançar exceção para sobrenome nulo")
    void save_deveLancarExcecaoParaSobrenomeNulo() {
        person.setLastname(null);
        when(personRepository.existsByCpf("12345678901")).thenReturn(false);

        assertThatThrownBy(() -> personService.save(person))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O sobrenome é obrigatório");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar pessoa quando encontrada")
    void findById_deveRetornarPessoa() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        Optional<Person> result = personService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("findById - deve retornar vazio quando não encontrada")
    void findById_deveRetornarVazioQuandoNaoEncontrada() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Person> result = personService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll - deve retornar lista de pessoas")
    void findAll_deveRetornarListaDePessoas() {
        when(personRepository.findAll()).thenReturn(List.of(person));

        List<Person> result = personService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByCpf - deve retornar pessoa por CPF")
    void findByCpf_deveRetornarPessoaPorCpf() {
        when(personRepository.findByCpf("12345678901")).thenReturn(Optional.of(person));

        Optional<Person> result = personService.findByCpf("12345678901");

        assertThat(result).isPresent();
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar pessoa existente")
    void update_deveAtualizarPessoa() {
        Person updated = new Person();
        updated.setFirstname("João");
        updated.setLastname("Santos");
        updated.setCpf("12345678901");

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.findByCpf("12345678901")).thenReturn(Optional.of(person));
        when(personRepository.save(any(Person.class))).thenReturn(person);

        Person result = personService.update(1L, updated);

        assertThat(result).isNotNull();
        verify(personRepository).save(any(Person.class));
    }

    @Test
    @DisplayName("update - deve lançar exceção para pessoa não encontrada")
    void update_deveLancarExcecaoParaPessoaNaoEncontrada() {
        Person updated = new Person();
        updated.setFirstname("João");
        updated.setLastname("Santos");
        updated.setCpf("12345678901");

        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.update(1L, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pessoa não encontrada");
    }

    @Test
    @DisplayName("update - deve lançar exceção para CPF duplicado de outra pessoa")
    void update_deveLancarExcecaoParaCpfDuplicado() {
        Person otherPerson = new Person();
        otherPerson.setId(2L);
        otherPerson.setCpf("99988877766");

        Person updated = new Person();
        updated.setFirstname("João");
        updated.setLastname("Santos");
        updated.setCpf("99988877766");

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.findByCpf("99988877766")).thenReturn(Optional.of(otherPerson));

        assertThatThrownBy(() -> personService.update(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF já cadastrado para outra pessoa");
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve excluir pessoa com sucesso")
    void deleteById_deveExcluirPessoa() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        personService.deleteById(1L);

        verify(personRepository).delete(person);
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando pessoa tem funcionário vinculado")
    void deleteById_deveLancarExcecaoQuandoTemFuncionario() {
        person.setEmployee(new Employee());
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> personService.deleteById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vinculada a um funcionário");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando pessoa tem cliente vinculado")
    void deleteById_deveLancarExcecaoQuandoTemCliente() {
        person.setCustomer(new Customer());
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> personService.deleteById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vinculada a um cliente");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para pessoa não encontrada")
    void deleteById_deveLancarExcecaoParaPessoaNaoEncontrada() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Pessoa não encontrada");
    }
}
