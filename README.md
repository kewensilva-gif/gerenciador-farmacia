# Gerenciamento Farmácia — API REST

API REST para gerenciamento de farmácia desenvolvida com **Spring Boot 4**. Oferece controle completo de produtos, categorias, clientes, funcionários, vendas e autenticação baseada em JWT, com soft delete, gestão de estoque, validação de regras de negócio e cobertura de testes automatizados.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Security | 7.0.3 |
| Spring Data JPA | — |
| PostgreSQL | — |
| Flyway | — |
| JWT (jjwt) | 0.12.7 |
| Springdoc OpenAPI | 2.8.14 |
| Lombok | — |
| Maven | — |

---

## Funcionalidades Principais

- **Soft Delete** — Produtos, vendas e categorias usam exclusão lógica (campo `enabled`). Todas as consultas filtram automaticamente por registros ativos.
- **Gestão de Estoque** — Débito automático no momento da venda; restauração ao cancelar venda ou remover item. Impede venda com estoque insuficiente.
- **Controle de Validade** — Bloqueia venda de produtos vencidos; endpoint dedicado para listar produtos expirados.
- **Alerta de Estoque Baixo** — Endpoint com limite configurável para consultar produtos com estoque abaixo do threshold.
- **Cálculo Automático de Totais** — Total da venda calculado a partir de (itens × preço unitário) − desconto.
- **Snapshot de Preço** — Itens de venda capturam o preço unitário do produto no momento da venda.
- **Validações em Cascata** — Categoria não pode ser desativada se possuir produtos ativos; funcionário demitido não pode registrar vendas; pessoa não pode ser removida se possuir cliente/funcionário vinculado.
- **Autenticação JWT** — Assinatura HMAC-SHA com expiração configurável; login por username ou email.
- **Controle de Acesso por Papel** — 3 papéis: ADMIN (acesso total), EMPLOYEE (vendas e clientes), CUSTOMER (somente leitura).
- **Registro Administrativo** — Endpoint transacional que cria usuário + pessoa + funcionário/cliente em uma única operação.
- **Documentação Interativa** — Swagger UI em `/swagger-ui.html`.
- **Bean Validation** — `@Valid` nos DTOs de autenticação e registro administrativo.
- **Busca Avançada** — Filtros por código de barras, nome (ILIKE), CPF, faixas de data, método de pagamento, faixas de preço, username e email.

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL em execução

---

## Configuração

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=farmacia
DB_USER=seu_usuario
DB_PASSWORD=sua_senha

JWT_SECRET=sua_chave_secreta_base64
JWT_EXPIRATION_MS=86400000
```

> O projeto usa [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) para carregar o `.env` automaticamente.

### Banco de Dados

O Flyway executa as migrations automaticamente ao iniciar a aplicação:

| Migration | Descrição |
|---|---|
| `V1__create_tables.sql` | Criação do ENUM `payment_method`, extensão `pgcrypto` e todas as tabelas (user, role, user\_role, person, category, product, customer, employee, sale, sale\_product) |
| `V2__seed_roles.sql` | Inserção dos papéis padrão: ADMIN, EMPLOYEE, CUSTOMER |
| `V3__refactor_user_role_to_single.sql` | Refatoração de muitos-para-muitos para papel único: adiciona `role_id` em user, remove tabela `user_role` |
| `V4__seed_data.sql` | Dados de desenvolvimento: 7 usuários, 7 pessoas, 3 funcionários, 4 clientes, 8 categorias, 16 produtos, 5 vendas e 12 itens de venda |
| `V5__add_enabled_column_soft_delete.sql` | Adiciona coluna `enabled BOOLEAN DEFAULT TRUE` em product, sale e category para soft delete |

---

## Executando o Projeto

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd farmacia-springboot

# Executar
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## Documentação da API

O Swagger UI está disponível em:

```
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI (JSON) está em:

```
http://localhost:8080/v3/api-docs
```

---

## Modelo de Dados

```
User (UUID PK) ──ManyToOne──► Role (UUID PK)
     │
     └── OneToOne ──► Person (BIGINT PK)
                          ├── OneToOne ──► Employee (salary, hiring/termination dates)
                          └── OneToOne ──► Customer (registration date)

Category ◄── ManyToOne ── Product (barcode, stock, expiration, enabled)
                              │
Sale ── OneToMany ──► SaleProduct ── ManyToOne ──► Product
  │                     (quantity, unit_price snapshot)
  ├── ManyToOne ──► Employee
  └── ManyToOne ──► Customer (nullable — venda anônima)
```

**Métodos de pagamento:** `CREDITCARD`, `DEBITCARD`, `PIX`, `CASH`

---

## Endpoints

### Autenticação — `/api/auth`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/register` | Registrar novo usuário (papel CUSTOMER por padrão), retorna JWT | Público |
| POST | `/api/auth/login` | Autenticar por username ou email, retorna JWT | Público |

### Registro Administrativo — `/api/admin/register`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/admin/register` | Registrar usuário + pessoa + funcionário/cliente em transação única | ADMIN |

### Produtos — `/api/products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/products` | Listar todos os produtos ativos | Autenticado |
| GET | `/api/products/{id}` | Buscar produto por ID | Autenticado |
| GET | `/api/products/search/barcode?barcode=` | Buscar por código de barras | Autenticado |
| GET | `/api/products/search/name?name=` | Buscar por nome (ILIKE) | Autenticado |
| GET | `/api/products/category/{categoryId}` | Filtrar por categoria | Autenticado |
| GET | `/api/products/expired` | Listar produtos vencidos | Autenticado |
| GET | `/api/products/low-stock?quantity=10` | Listar produtos com estoque baixo | Autenticado |
| POST | `/api/products` | Criar produto | ADMIN |
| PUT | `/api/products/{id}` | Atualizar produto | ADMIN |
| DELETE | `/api/products/{id}` | Desativar produto (soft delete) | ADMIN |

### Categorias — `/api/categories`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/categories` | Listar todas as categorias ativas | Autenticado |
| GET | `/api/categories/{id}` | Buscar categoria por ID | Autenticado |
| GET | `/api/categories/search/name?name=` | Buscar por nome | Autenticado |
| POST | `/api/categories` | Criar categoria | ADMIN |
| PUT | `/api/categories/{id}` | Atualizar categoria | ADMIN |
| DELETE | `/api/categories/{id}` | Desativar categoria (bloqueado se há produtos ativos) | ADMIN |

### Vendas — `/api/sales`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sales` | Listar todas as vendas ativas | ADMIN, EMPLOYEE |
| GET | `/api/sales/{id}` | Buscar venda por ID | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/payment-method?paymentMethod=` | Filtrar por método de pagamento | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/price-greater?price=` | Vendas acima do valor | ADMIN, EMPLOYEE |
| GET | `/api/sales/search/price-less?price=` | Vendas abaixo do valor | ADMIN, EMPLOYEE |
| POST | `/api/sales` | Registrar venda (debita estoque automaticamente) | ADMIN, EMPLOYEE |
| PUT | `/api/sales/{id}` | Atualizar venda (desconto, método de pagamento) | ADMIN, EMPLOYEE |
| DELETE | `/api/sales/{id}` | Cancelar venda (soft delete, restaura estoque) | ADMIN, EMPLOYEE |

### Itens de Venda — `/api/sale-products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sale-products` | Listar todos os itens | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/{id}` | Buscar item por ID | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/search/sale/{saleId}` | Itens por venda | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/search/product/{productId}` | Itens por produto | ADMIN, EMPLOYEE |
| DELETE | `/api/sale-products/{id}` | Remover item (restaura estoque) | ADMIN, EMPLOYEE |

### Clientes — `/api/customers`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/customers` | Listar todos os clientes | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/{id}` | Buscar cliente por ID | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/search/after?date=` | Clientes registrados após a data | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/search/before?date=` | Clientes registrados antes da data | ADMIN, EMPLOYEE, CUSTOMER |
| POST | `/api/customers` | Criar cliente | ADMIN, EMPLOYEE |
| PUT | `/api/customers/{id}` | Atualizar cliente | ADMIN, EMPLOYEE |
| DELETE | `/api/customers/{id}` | Remover cliente (bloqueado se há dependências) | ADMIN |

### Funcionários — `/api/employees`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/employees` | Listar todos os funcionários | ADMIN |
| GET | `/api/employees/{id}` | Buscar funcionário por ID | ADMIN |
| GET | `/api/employees/search/after?date=` | Contratados após a data | ADMIN |
| GET | `/api/employees/search/before?date=` | Contratados antes da data | ADMIN |
| GET | `/api/employees/active` | Listar funcionários ativos | ADMIN |
| GET | `/api/employees/inactive` | Listar funcionários inativos (demitidos) | ADMIN |
| POST | `/api/employees` | Criar funcionário | ADMIN |
| PUT | `/api/employees/{id}` | Atualizar funcionário | ADMIN |
| DELETE | `/api/employees/{id}` | Remover funcionário (bloqueado se há dependências) | ADMIN |

### Pessoas — `/api/people`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/people` | Listar todas as pessoas | Autenticado |
| GET | `/api/people/{id}` | Buscar pessoa por ID | Autenticado |
| GET | `/api/people/search/cpf?cpf=` | Buscar por CPF | Autenticado |
| GET | `/api/people/search/exists/cpf?cpf=` | Verificar existência por CPF | Autenticado |
| POST | `/api/people` | Criar pessoa | Autenticado |
| PUT | `/api/people/{id}` | Atualizar pessoa | Autenticado |
| DELETE | `/api/people/{id}` | Remover pessoa (bloqueado se há dependências) | Autenticado |

### Usuários — `/api/users`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/users` | Listar todos os usuários | ADMIN |
| GET | `/api/users/{id}` | Buscar usuário por UUID | ADMIN |
| GET | `/api/users/search/username?username=` | Buscar por username | ADMIN |
| GET | `/api/users/search/email?email=` | Buscar por email | ADMIN |
| GET | `/api/users/enabled` | Listar usuários ativos | ADMIN |
| GET | `/api/users/disabled` | Listar usuários inativos | ADMIN |
| GET | `/api/users/search/exists/username?username=` | Verificar existência por username | ADMIN |
| GET | `/api/users/search/exists/email?email=` | Verificar existência por email | ADMIN |
| POST | `/api/users` | Criar usuário | ADMIN |
| PUT | `/api/users/{id}` | Atualizar usuário | ADMIN |
| DELETE | `/api/users/{id}` | Remover usuário | ADMIN |

### Papéis — `/api/roles`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/roles` | Listar todos os papéis | ADMIN |
| GET | `/api/roles/{id}` | Buscar papel por UUID | ADMIN |
| GET | `/api/roles/search/name?name=` | Buscar por nome | ADMIN |
| GET | `/api/roles/search/exists/name?name=` | Verificar existência por nome | ADMIN |
| POST | `/api/roles` | Criar papel | ADMIN |
| PUT | `/api/roles/{id}` | Atualizar papel | ADMIN |
| DELETE | `/api/roles/{id}` | Remover papel (bloqueado se em uso) | ADMIN |

---

## Autenticação JWT

Após realizar o login, inclua o token retornado no cabeçalho de todas as requisições protegidas:

```
Authorization: Bearer <token>
```

### Exemplo de login

**Request:**
```json
POST /api/auth/login
{
  "login": "admin",
  "password": "senha123"
}
```

> O campo `login` aceita tanto username quanto email.

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "email": "admin@farmacia.com"
}
```

### Exemplo de registro público

**Request:**
```json
POST /api/auth/register
{
  "username": "novousuario",
  "email": "novo@email.com",
  "password": "senha123"
}
```

### Exemplo de registro administrativo

**Request (requer ADMIN):**
```json
POST /api/admin/register
{
  "firstName": "Maria",
  "lastName": "Santos",
  "cpf": "12345678901",
  "username": "maria.santos",
  "email": "maria@farmacia.com",
  "password": "senha123",
  "roleName": "EMPLOYEE",
  "registrationDate": null,
  "hiringDate": "2024-01-15",
  "salary": 3500.00
}
```

---

## Papéis de Acesso

| Papel | Descrição |
|---|---|
| `ADMIN` | Acesso total ao sistema |
| `EMPLOYEE` | Gerenciamento de vendas e clientes; leitura de produtos e categorias |
| `CUSTOMER` | Leitura de clientes e produtos |

### Regras de Segurança

| Recurso | GET | POST / PUT | DELETE |
|---|---|---|---|
| `/api/auth/**` | — | Público | — |
| `/api/products/**`, `/api/categories/**` | Autenticado | ADMIN | ADMIN |
| `/api/sales/**`, `/api/sale-products/**` | ADMIN, EMPLOYEE | ADMIN, EMPLOYEE | ADMIN, EMPLOYEE |
| `/api/customers/**` | ADMIN, EMPLOYEE, CUSTOMER | ADMIN, EMPLOYEE | ADMIN |
| `/api/users/**`, `/api/roles/**`, `/api/employees/**` | ADMIN | ADMIN | ADMIN |
| `/api/admin/**` | ADMIN | ADMIN | ADMIN |
| Demais endpoints | Autenticado | Autenticado | Autenticado |

---

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/kewen/GerenciamentoFarmacia/
│   │   ├── config/          # SecurityConfig, CorsConfig, Swagger
│   │   ├── controllers/     # 11 controladores REST
│   │   ├── dto/             # DTOs de autenticação e registro
│   │   │   └── auth/        # AuthRequest, AuthResponse, RegisterRequest
│   │   ├── entities/        # Entidades JPA (User, Role, Person, etc.)
│   │   ├── repositories/    # Repositórios Spring Data JPA
│   │   ├── security/        # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
│   │   └── services/        # 10 services com regras de negócio
│   └── resources/
│       ├── application.properties
│       └── db/migration/    # V1 a V5 — Scripts Flyway
└── test/
    └── java/com/kewen/GerenciamentoFarmacia/
        ├── controllers/     # 11 testes de integração (MockMvc + Security)
        └── services/        # 9 testes unitários (Mockito)
```

---

## Testes

O projeto possui **316 testes automatizados** divididos em duas camadas:

```bash
./mvnw test
```

### Testes de Integração — Controllers (160 testes)

Utilizam `@WebMvcTest` + `MockMvc` com segurança real (`SecurityConfig`) e serviços mockados. Validam: rotas, status HTTP, serialização JSON, regras de autorização (403 para roles sem permissão) e respostas de erro.

| Classe de Teste | Testes |
|---|---|
| AuthControllerTest | 9 |
| CategoryControllerTest | 13 |
| CustomerControllerTest | 18 |
| EmployeeControllerTest | 18 |
| PersonControllerTest | 16 |
| ProductControllerTest | 17 |
| RoleControllerTest | 16 |
| SaleControllerTest | 16 |
| SaleProductControllerTest | 10 |
| UserControllerTest | 19 |
| UserRegistrationControllerTest | 8 |

### Testes Unitários — Services (155 testes)

Utilizam `@ExtendWith(MockitoExtension.class)` com repositórios mockados. Validam: regras de negócio, validações, soft delete, gestão de estoque e tratamento de exceções.

| Classe de Teste | Testes |
|---|---|
| CategoryServiceTest | 15 |
| CustomerServiceTest | 16 |
| EmployeeServiceTest | 20 |
| PersonServiceTest | 19 |
| ProductServiceTest | 26 |
| RoleServiceTest | 14 |
| SaleProductServiceTest | 11 |
| SaleServiceTest | 19 |
| UserServiceTest | 15 |

### Teste de Contexto

| Classe de Teste | Testes |
|---|---|
| GerenciamentoFarmaciaApplicationTests | 1 |

Os relatórios são gerados em `target/surefire-reports/`.

---

## Dados de Desenvolvimento

A migration `V4__seed_data.sql` insere dados para testes manuais:

| Usuário | Senha | Papel |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `joao.silva` | `func123` | EMPLOYEE |
| `maria.santos` | `func123` | EMPLOYEE |
| `carlos.oliveira` | `cli123` | CUSTOMER |
| `ana.souza` | `cli123` | CUSTOMER |
| `pedro.lima` | `cli123` | CUSTOMER |
| `lucia.ferreira` | `cli123` | CUSTOMER |

---

## Licença

Uso interno.
