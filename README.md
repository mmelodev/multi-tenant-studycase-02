# multi-tenant-studycase

Estudo de caso de uma API REST **multi-tenant** em Spring Boot, usando a estratégia de
**um schema PostgreSQL por tenant** (schema-per-tenant). O isolamento é feito pelo
mecanismo nativo de multi-tenancy do Hibernate (`multi_tenancy: SCHEMA`): a cada
requisição autenticada, a conexão tem seu `search_path` apontado para o schema do
tenant dono do token.

> Este README documenta a **API**. Para o racional de arquitetura, veja os comentários
> no código de `config/` (`MultiTenantConnectionProviderImpl`, `CurrentTenantIdentifierResolverImpl`,
> `TenantSchemaResolver`).

---

## Stack

- Java / Spring Boot (`web`, `data-jpa`, `validation`, `security`, `flyway`)
- PostgreSQL
- Flyway (migrations versionadas, `common` + `tenant`)
- JWT assinado com par de chaves RSA (`jjwt`)
- springdoc / Swagger UI
- Lombok, Maven (via wrapper `./mvnw`)

---

## Como a multi-tenancy funciona

Há dois níveis de schema no mesmo banco:

- **`public`** — schema global. Guarda as tabelas `tenants` e `users`.
- **`tenant_<company_code>`** — um schema por tenant, criado no momento da aprovação.
  Guarda os dados de negócio: `categories`, `products`, `stock_mvts`.

Fluxo de ponta a ponta:

1. Uma empresa se registra (`POST /api/v1/auth/register`) e nasce com status `PENDING`.
2. Um administrador da plataforma aprova (`POST /api/v1/tenants/approve/{tenant-id}`).
   A aprovação cria o schema `tenant_<company_code>`, roda as migrations de tenant
   nesse schema e cria o usuário administrador inicial da empresa (`ROLE_COMPANY_ADMIN`).
3. O usuário faz login (`POST /api/v1/auth/login`) e recebe um **JWT**.
4. Em cada chamada seguinte, o token vai no header `Authorization: Bearer <token>`.
   O filtro de autenticação lê o `tenantId` do token, resolve o schema correspondente
   e o Hibernate direciona todas as queries para o schema daquele tenant.

**Não existe mais o header `X-Tenant-ID`.** O tenant é determinado pelo conteúdo do JWT.

---

## Autenticação

Todas as rotas exigem autenticação, **exceto** as de `/api/v1/auth/**` e as de documentação
(`/swagger-ui/**`, `/v3/api-docs/**`).

Envie o token em todas as rotas protegidas:

```
Authorization: Bearer <accessToken>
```

Algumas rotas ainda exigem papéis específicos via `@PreAuthorize` (indicado por endpoint abaixo).
Papéis disponíveis (`UserRole`): `ROLE_PLATFORM_ADMIN`, `ROLE_COMPANY_ADMIN`,
`ROLE_ADMINISTRATOR`, `ROLE_USER`, `ROLE_SALE_OPERATOR`.

---

## Como rodar localmente

1. Suba um PostgreSQL em `localhost` e crie o database `multitenant-study-02`.
2. Gere um par de chaves RSA e aponte as variáveis de ambiente para elas (ou use os
   defaults `certs/private_key.pem` / `certs/public_key.pem`).
3. Defina as variáveis de ambiente:

   | Variável | Descrição | Default |
   |---|---|---|
   | `DB_PORT` | Porta do Postgres | — |
   | `DB_USER` | Usuário do banco | — |
   | `DB_PASS` | Senha do banco | — |
   | `SERVER_PORT` | Porta da aplicação | `8080` |
   | `SPRING_PROFILES_ACTIVE` | Profile ativo | `dev` |
   | `JWT_PRIVATE_KEY_PATH` | Chave privada RSA (PEM) | `certs/private_key.pem` |
   | `JWT_PUBLIC_KEY_PATH` | Chave pública RSA (PEM) | `certs/public_key.pem` |
   | `JWT_ACCESS_TOKEN_EXPIRATION` | Validade do token (ms) | `86400000` (24h) |

4. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   O Flyway aplica automaticamente as migrations de `public` (`classpath:db/migration/common`)
   no start. As migrations de cada tenant (`classpath:db/migration/tenant`) são aplicadas
   ao aprovar o tenant. O `ddl-auto` é `none` — o schema é 100% gerido por Flyway.

API sobe em `http://localhost:8080`. Swagger UI em `http://localhost:8080/swagger-ui.html`.

---

## Convenções gerais

- **Paginação:** endpoints de listagem aceitam `?page=<n>&size=<n>` (defaults `0` e `10`)
  e retornam um envelope `PageResponse`:

  ```json
  {
    "content": [ /* ... */ ],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": false
  }
  ```

- **Erros:** tratados por um handler global e retornados no formato:

  ```json
  {
    "code": "NOT_FOUND",
    "message": "Category Not Found",
    "path": "/api/categories/abc",
    "validationErrors": null
  }
  ```

  | Status | Quando |
  |---|---|
  | `400 Bad Request` | Falha de validação de campos (`@Valid`) — vem em `validationErrors` |
  | `401 Unauthorized` | Credenciais inválidas / não autenticado |
  | `404 Not Found` | Entidade inexistente |
  | `409 Conflict` | Violação de regra de negócio (ex.: recurso duplicado) |

---

## Endpoints

### Authentication — `/api/v1/auth` (público)

#### `POST /api/v1/auth/register`

Registra uma nova empresa (tenant). Nasce com status `PENDING`, sem schema ainda.

Request (`RegisterTenantRequest`) — todos os campos `@NotBlank`:

```json
{
  "companyName": "ACME Ltda",
  "companyCode": "acme",
  "email": "contato@acme.com",
  "adminFullName": "Maria Silva",
  "adminEmail": "maria@acme.com",
  "adminUsername": "maria",
  "adminPassword": "senhaForte123"
}
```

Resposta: `200 OK` (sem corpo).

#### `POST /api/v1/auth/login`

Autentica um usuário e devolve o JWT.

Request (`LoginRequest`):

```json
{ "username": "maria", "password": "senhaForte123" }
```

Resposta `200 OK` (`LoginResponse`):

```json
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer" }
```

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"maria","password":"senhaForte123"}'
```

---

### Tenants — `/api/v1/tenants` (autenticado; gestão da plataforma)

| Método | Rota | Efeito | Resposta |
|---|---|---|---|
| `POST` | `/approve/{tenant-id}` | Ativa o tenant, **provisiona o schema** e cria o admin inicial | `200 OK` |
| `PATCH` | `/activate/{tenant-id}` | `PENDING` → `ACTIVE` | `200 OK` |
| `PATCH` | `/deactivate/{tenant-id}` | `ACTIVE` → `INACTIVE` | `200 OK` |
| `PATCH` | `/suspend/{tenant-id}` | `ACTIVE` → `SUSPENDED` | `200 OK` |
| `GET` | `/` | Lista tenants (paginado) | `PageResponse<TenantResponse>` |

`TenantResponse`:

```json
{
  "tenantId": "uuid",
  "companyName": "ACME Ltda",
  "companyCode": "acme",
  "email": "contato@acme.com",
  "adminFullName": "Maria Silva",
  "adminEmail": "maria@acme.com",
  "adminUsername": "maria",
  "adminPassword": "<hash>",
  "createdAt": "2026-07-15T10:00:00",
  "status": "ACTIVE"
}
```

Status possíveis (`TenantStatus`): `PENDING`, `ACTIVE`, `SUSPENDED`, `INACTIVE`.

---

### Users — `/api/v1/users` (autenticado + papéis)

| Método | Rota | Papel exigido | Corpo | Resposta |
|---|---|---|---|---|
| `POST` | `/` | `COMPANY_ADMIN` | `UserRequest` | `201 Created` |
| `GET` | `/` | `COMPANY_ADMIN`, `ADMINISTRATOR` | — | `PageResponse<UserResponse>` |
| `GET` | `/{user-id}` | `COMPANY_ADMIN`, `ADMINISTRATOR` | — | `UserResponse` |
| `PUT` | `/{user-id}` | `COMPANY_ADMIN` | `UserRequest` | `202 Accepted` |
| `DELETE` | `/{user-id}` | `COMPANY_ADMIN` | — | `204 No Content` |
| `PUT` | `/{user-id}/enable` | `COMPANY_ADMIN` | — | `202 Accepted` |
| `PUT` | `/{user-id}/disable` | `COMPANY_ADMIN` | — | `202 Accepted` |

Request (`UserRequest`):

```json
{
  "username": "joao",
  "email": "joao@acme.com",
  "password": "min8chars",
  "firstName": "João",
  "lastName": "Souza",
  "role": "ROLE_USER"
}
```

Validações: `username`, `email`, `firstName`, `lastName` são `@NotBlank`; `password` mínimo 8 caracteres; `role` `@NotNull`.

`UserResponse`:

```json
{
  "id": "uuid",
  "username": "joao",
  "email": "joao@acme.com",
  "password": "<hash>",
  "firstName": "João",
  "lastName": "Souza",
  "role": "ROLE_USER"
}
```

---

### Categories — `/api/categories` (autenticado)

> Observação: esta base é `/api/categories` (sem `/v1`), diferente do restante da API.

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| `POST` | `/` | `CategoryRequest` | `200 OK` |
| `GET` | `/` | — | `PageResponse<CategoryResponse>` |
| `GET` | `/{category-id}` | — | `CategoryResponse` |
| `PUT` | `/{category-id}` | `CategoryRequest` | `200 OK` |
| `DELETE` | `/{category-id}` | — | `200 OK` |

Request (`CategoryRequest`):

```json
{ "name": "Eletrônicos", "description": "Produtos eletrônicos em geral" }
```

`CategoryResponse`:

```json
{ "id": "uuid", "name": "Eletrônicos", "description": "..." }
```

```bash
curl http://localhost:8080/api/categories \
  -H "Authorization: Bearer <token>"
```

---

### Products — `/api/v1/products` (autenticado)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| `POST` | `/` | `ProductRequest` | `200 OK` |
| `GET` | `/` | — | `PageResponse<ProductResponse>` |
| `GET` | `/{product-id}` | — | `ProductResponse` |
| `PUT` | `/{product-id}` | `ProductRequest` | `202 Accepted` |
| `DELETE` | `/{product-id}` | — | `204 No Content` |

Request (`ProductRequest`):

```json
{
  "name": "Notebook",
  "reference": "NB-001",
  "description": "14 polegadas",
  "alertThreshold": 5,
  "price": 3999.90,
  "categoryId": "uuid-da-categoria"
}
```

`ProductResponse` (inclui nome da categoria e quantidade disponível calculada):

```json
{
  "id": "uuid",
  "name": "Notebook",
  "reference": "NB-001",
  "description": "14 polegadas",
  "alertThreshold": 5,
  "price": 3999.90,
  "categoryName": "Eletrônicos",
  "availableQuantity": 12
}
```

---

### Stock Movements — `/api/v1/stocks` (autenticado)

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| `POST` | `/` | `StockMvtRequest` | `200 OK` |
| `GET` | `/` | — | `PageResponse<StockMvtResponse>` |
| `GET` | `/{stock-mvt-id}` | — | `StockMvtResponse` |
| `GET` | `/product/{product-id}` | — | `PageResponse<StockMvtResponse>` |
| `PUT` | `/{stock-mvt-id}` | `StockMvtRequest` | `202 Accepted` |
| `DELETE` | `/{stock-mvt-id}` | — | `204 No Content` |

Request (`StockMvtRequest`) — `typeMvt` é `IN` (entrada) ou `OUT` (saída):

```json
{
  "typeMvt": "IN",
  "quantity": 10,
  "dateMvt": "2026-07-15",
  "comment": "Reposição de estoque",
  "productId": "uuid-do-produto"
}
```

`StockMvtResponse`:

```json
{
  "id": "uuid",
  "typeMvt": "IN",
  "quantity": 10,
  "dateMvt": "2026-07-15",
  "comment": "Reposição de estoque"
}
```

---

## Exemplo de fluxo completo

```bash
BASE=http://localhost:8080

# 1. Registrar a empresa
curl -X POST $BASE/api/v1/auth/register -H "Content-Type: application/json" -d '{
  "companyName":"ACME Ltda","companyCode":"acme","email":"contato@acme.com",
  "adminFullName":"Maria Silva","adminEmail":"maria@acme.com",
  "adminUsername":"maria","adminPassword":"senhaForte123"
}'

# 2. (Admin da plataforma) aprovar o tenant -> provisiona o schema
curl -X POST $BASE/api/v1/tenants/approve/<tenant-id> -H "Authorization: Bearer <platform-token>"

# 3. Login como admin da empresa
TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"username":"maria","password":"senhaForte123"}' | jq -r .accessToken)

# 4. Operar dentro do tenant (schema resolvido pelo token)
curl -X POST $BASE/api/categories -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"name":"Eletrônicos","description":"..."}'
```

---

## Limitações e inconsistências conhecidas

- **Senha exposta nas respostas:** `UserResponse.password` e `TenantResponse.adminPassword`
  devolvem o hash da senha. Devem ser removidos do payload de saída.
- **Base path inconsistente:** `Category` usa `/api/categories`, enquanto os demais recursos
  usam `/api/v1/...`.
- **`CategoryRequest` sem Bean Validation:** apesar do `@Valid` no controller, os campos não têm
  `@NotBlank`/`@Size`; um `name` vazio só é barrado (se for) pela constraint `NOT NULL` do banco.
- **Enum de papel divergente:** `UserRole` define `ROLE_SALE_OPERATOR`, mas o `check` da tabela
  `users` espera `ROLE_SALES_OPERATOR` — criar um usuário com esse papel viola a constraint.
- **Versão do springdoc:** a dependência foi adicionada com uma versão da linha do Spring Boot 3;
  ao fixar a versão do Spring Boot 4, alinhe a versão do `springdoc-openapi-starter-webmvc-ui`.
- **`search_path` por concatenação:** o schema é injetado via `SET search_path TO <schema>`;
  recomenda-se validar o identificador (ex.: `^[a-z0-9_]+$`) antes de aplicar.
