# Gerenciamento Farmácia — API REST

API REST para gerenciamento de farmácia desenvolvida com Spring Boot. Oferece controle de produtos, categorias, clientes, funcionários, vendas e autenticação baseada em JWT.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Security | — |
| Spring Data JPA | — |
| PostgreSQL | — |
| Flyway | — |
| JWT (jjwt) | 0.12.7 |
| Springdoc OpenAPI | 2.8.14 |
| Lombok | — |
| Maven | — |

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
| `V1__create_tables.sql` | Criação das tabelas |
| `V2__seed_roles.sql` | Inserção dos papéis padrão |
| `V3__refactor_user_role_to_single.sql` | Refatoração do relacionamento usuário-papel |

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

## Endpoints

### Autenticação — `/api/auth`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/register` | Registrar novo usuário | Público |
| POST | `/api/auth/login` | Autenticar e obter token JWT | Público |

### Categorias — `/api/categories`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/categories` | Listar todas as categorias | Autenticado |
| GET | `/api/categories/{id}` | Buscar categoria por ID | Autenticado |
| GET | `/api/categories/search/name` | Buscar categoria por nome | Autenticado |
| POST | `/api/categories` | Criar categoria | ADMIN |
| PUT | `/api/categories/{id}` | Atualizar categoria | ADMIN |
| DELETE | `/api/categories/{id}` | Remover categoria | ADMIN |

### Produtos — `/api/products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/products` | Listar todos os produtos | Autenticado |
| GET | `/api/products/{id}` | Buscar produto por ID | Autenticado |
| POST | `/api/products` | Criar produto | ADMIN |
| PUT | `/api/products/{id}` | Atualizar produto | ADMIN |
| DELETE | `/api/products/{id}` | Remover produto | ADMIN |

### Clientes — `/api/customers`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/customers` | Listar todos os clientes | ADMIN, EMPLOYEE, CUSTOMER |
| GET | `/api/customers/{id}` | Buscar cliente por ID | ADMIN, EMPLOYEE, CUSTOMER |
| POST | `/api/customers` | Criar cliente | ADMIN, EMPLOYEE |
| PUT | `/api/customers/{id}` | Atualizar cliente | ADMIN, EMPLOYEE |
| DELETE | `/api/customers/{id}` | Remover cliente | ADMIN |

### Funcionários — `/api/employees`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/employees` | Listar todos os funcionários | ADMIN |
| GET | `/api/employees/{id}` | Buscar funcionário por ID | ADMIN |
| GET | `/api/employees/active` | Listar funcionários ativos | ADMIN |
| GET | `/api/employees/search/after` | Buscar por data de contratação (após) | ADMIN |
| GET | `/api/employees/search/before` | Buscar por data de contratação (antes) | ADMIN |
| POST | `/api/employees` | Criar funcionário | ADMIN |
| PUT | `/api/employees/{id}` | Atualizar funcionário | ADMIN |
| DELETE | `/api/employees/{id}` | Remover funcionário | ADMIN |

### Pessoas — `/api/people`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/people` | Listar todas as pessoas | ADMIN |
| GET | `/api/people/{id}` | Buscar pessoa por ID | ADMIN |
| GET | `/api/people/search/cpf` | Buscar por CPF | ADMIN |
| GET | `/api/people/search/exists/cpf` | Verificar existência por CPF | ADMIN |
| POST | `/api/people` | Criar pessoa | ADMIN |
| PUT | `/api/people/{id}` | Atualizar pessoa | ADMIN |
| DELETE | `/api/people/{id}` | Remover pessoa | ADMIN |

### Vendas — `/api/sales`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sales` | Listar todas as vendas | ADMIN, EMPLOYEE |
| GET | `/api/sales/{id}` | Buscar venda por ID | ADMIN, EMPLOYEE |
| POST | `/api/sales` | Registrar venda | ADMIN, EMPLOYEE |
| PUT | `/api/sales/{id}` | Atualizar venda | ADMIN, EMPLOYEE |
| DELETE | `/api/sales/{id}` | Remover venda | ADMIN, EMPLOYEE |

### Itens de Venda — `/api/sale-products`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/sale-products` | Listar todos os itens | ADMIN, EMPLOYEE |
| GET | `/api/sale-products/{id}` | Buscar item por ID | ADMIN, EMPLOYEE |
| POST | `/api/sale-products` | Adicionar item à venda | ADMIN, EMPLOYEE |
| PUT | `/api/sale-products/{id}` | Atualizar item | ADMIN, EMPLOYEE |
| DELETE | `/api/sale-products/{id}` | Remover item | ADMIN, EMPLOYEE |

### Usuários — `/api/users`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/users` | Listar todos os usuários | ADMIN |
| GET | `/api/users/{id}` | Buscar usuário por ID | ADMIN |
| GET | `/api/users/enabled` | Listar usuários ativos | ADMIN |
| GET | `/api/users/search/username` | Buscar por username | ADMIN |
| GET | `/api/users/search/email` | Buscar por email | ADMIN |
| POST | `/api/users` | Criar usuário | ADMIN |
| PUT | `/api/users/{id}` | Atualizar usuário | ADMIN |
| DELETE | `/api/users/{id}` | Remover usuário | ADMIN |

### Papéis — `/api/roles`

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/roles` | Listar todos os papéis | ADMIN |
| GET | `/api/roles/{id}` | Buscar papel por ID | ADMIN |
| GET | `/api/roles/search/name` | Buscar por nome | ADMIN |
| GET | `/api/roles/search/exists/name` | Verificar existência por nome | ADMIN |
| POST | `/api/roles` | Criar papel | ADMIN |
| PUT | `/api/roles/{id}` | Atualizar papel | ADMIN |
| DELETE | `/api/roles/{id}` | Remover papel | ADMIN |

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
  "username": "admin",
  "password": "senha123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

## Papéis de Acesso

| Papel | Descrição |
|---|---|
| `ADMIN` | Acesso total ao sistema |
| `EMPLOYEE` | Gerenciamento de vendas, clientes e produtos (leitura) |
| `CUSTOMER` | Visualização de produtos e próprio histórico |

---

## Estrutura do Projeto

```
src/
└── main/
    ├── java/com/kewen/GerenciamentoFarmacia/
    │   ├── config/          # Configurações (Security, Swagger)
    │   ├── controllers/     # Controladores REST
    │   ├── dto/             # Data Transfer Objects
    │   ├── entities/        # Entidades JPA
    │   ├── repositories/    # Repositórios Spring Data
    │   ├── security/        # JWT Filter, UserDetails
    │   └── services/        # Regras de negócio
    └── resources/
        ├── application.properties
        └── db/migration/    # Scripts Flyway
```

---

## Testes

```bash
./mvnw test
```

Os relatórios são gerados em `target/surefire-reports/`.

---

## Licença

Uso interno.
