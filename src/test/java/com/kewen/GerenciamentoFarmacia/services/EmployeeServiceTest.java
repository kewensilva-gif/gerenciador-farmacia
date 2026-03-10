package com.kewen.GerenciamentoFarmacia.services;

import com.kewen.GerenciamentoFarmacia.entities.Employee;
import com.kewen.GerenciamentoFarmacia.entities.Person;
import com.kewen.GerenciamentoFarmacia.repositories.EmployeeRepository;
import com.kewen.GerenciamentoFarmacia.repositories.SaleRepository;

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
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PersonService personService;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1L);
        person.setFirstname("Maria");
        person.setLastname("Santos");
        person.setCpf("98765432100");
        person.setEmployee(null);
        person.setCustomer(null);

        employee = new Employee();
        employee.setId(1L);
        employee.setHiringDate(LocalDate.of(2023, 3, 1));
        employee.setTerminationDate(null);
        employee.setSalary(new BigDecimal("3000.00"));
        employee.setPerson(person);
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("save - deve salvar e retornar o funcionário")
    void save_deveSalvarERetornarFuncionario() {
        when(personService.findById(1L)).thenReturn(Optional.of(person));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.save(employee);

        assertThat(result).isNotNull();
        assertThat(result.getSalary()).isEqualByComparingTo(new BigDecimal("3000.00"));
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa é nula")
    void save_deveLancarExcecaoQuandoPessoaNula() {
        employee.setPerson(null);

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O funcionário precisa estar vinculado a uma pessoa");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa não encontrada")
    void save_deveLancarExcecaoQuandoPessoaNaoEncontrada() {
        when(personService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pessoa não encontrada");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando pessoa já é funcionário")
    void save_deveLancarExcecaoQuandoPessoaJaEhFuncionario() {
        person.setEmployee(new Employee());
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esta pessoa já está cadastrada como funcionário");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando salário é zero")
    void save_deveLancarExcecaoQuandoSalarioZero() {
        employee.setSalary(BigDecimal.ZERO);
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O salário deve ser maior que zero");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando salário é negativo")
    void save_deveLancarExcecaoQuandoSalarioNegativo() {
        employee.setSalary(new BigDecimal("-100.00"));
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O salário deve ser maior que zero");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando data de contratação é nula")
    void save_deveLancarExcecaoQuandoDataContratacaoNula() {
        employee.setHiringDate(null);
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de contratação é obrigatória");
    }

    @Test
    @DisplayName("save - deve lançar exceção quando data desligamento é anterior à contratação")
    void save_deveLancarExcecaoQuandoDataDesligamentoAnterior() {
        employee.setTerminationDate(LocalDate.of(2022, 1, 1));
        when(personService.findById(1L)).thenReturn(Optional.of(person));

        assertThatThrownBy(() -> employeeService.save(employee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de desligamento não pode ser anterior à data de contratação");
    }

    // ======================== FIND ========================

    @Test
    @DisplayName("findById - deve retornar funcionário quando encontrado")
    void findById_deveRetornarFuncionario() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findAll - deve retornar lista de funcionários")
    void findAll_deveRetornarListaDeFuncionarios() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findActiveEmployees - deve retornar funcionários ativos")
    void findActiveEmployees_deveRetornarFuncionariosAtivos() {
        when(employeeRepository.findByTerminationDateIsNull()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findActiveEmployees();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findInactiveEmployees - deve retornar funcionários inativos")
    void findInactiveEmployees_deveRetornarFuncionariosInativos() {
        when(employeeRepository.findByTerminationDateIsNotNull()).thenReturn(List.of());

        List<Employee> result = employeeService.findInactiveEmployees();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByHiringAfter - deve retornar funcionários contratados após data")
    void findByHiringAfter_deveRetornarFuncionarios() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        when(employeeRepository.findByHiringDateAfter(date)).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findByHiringAfter(date);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByHiringBefore - deve retornar funcionários contratados antes da data")
    void findByHiringBefore_deveRetornarFuncionarios() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        when(employeeRepository.findByHiringDateBefore(date)).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findByHiringBefore(date);

        assertThat(result).hasSize(1);
    }

    // ======================== UPDATE ========================

    @Test
    @DisplayName("update - deve atualizar funcionário existente")
    void update_deveAtualizarFuncionario() {
        Employee updated = new Employee();
        updated.setHiringDate(LocalDate.of(2023, 3, 1));
        updated.setSalary(new BigDecimal("3500.00"));
        updated.setPerson(person);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.update(1L, updated);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("update - deve lançar exceção para funcionário não encontrado")
    void update_deveLancarExcecaoParaFuncionarioNaoEncontrado() {
        Employee updated = new Employee();
        updated.setHiringDate(LocalDate.of(2023, 3, 1));
        updated.setSalary(new BigDecimal("3500.00"));
        updated.setPerson(person);

        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(1L, updated))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Funcionário não encontrado");
    }

    @Test
    @DisplayName("update - deve lançar exceção para salário zero no update")
    void update_deveLancarExcecaoParaSalarioZero() {
        Employee updated = new Employee();
        updated.setHiringDate(LocalDate.of(2023, 3, 1));
        updated.setSalary(BigDecimal.ZERO);
        updated.setPerson(person);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.update(1L, updated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O salário deve ser maior que zero");
    }

    // ======================== DELETE ========================

    @Test
    @DisplayName("deleteById - deve excluir funcionário com sucesso")
    void deleteById_deveExcluirFuncionario() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(saleRepository.existsByEmployeeId(1L)).thenReturn(false);

        employeeService.deleteById(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção quando funcionário tem vendas")
    void deleteById_deveLancarExcecaoQuandoTemVendas() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(saleRepository.existsByEmployeeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.deleteById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("possui vendas vinculadas");
    }

    @Test
    @DisplayName("deleteById - deve lançar exceção para funcionário não encontrado")
    void deleteById_deveLancarExcecaoParaFuncionarioNaoEncontrado() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Funcionário não encontrado");
    }
}
