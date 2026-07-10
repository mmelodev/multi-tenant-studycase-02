# multi-tenant-studycase

Estudo de caso de uma API REST multi-tenant em **Spring Boot 4.1.0 / Java 25**, usando a estratégia de **schema compartilhado com coluna discriminadora** (`tenant_id`). Para uma análise aprofundada da arquitetura, riscos conhecidos e roadmap, veja [`arquitetura-multitenant.md`](./arquitetura-multitenant.md).

---

## Stack

- Java 25, Spring Boot 4.1.0 (`web`, `data-jpa`, `validation`)
- PostgreSQL (driver `org.postgresql`)
- Lombok
- Maven (via wrapper `./mvnw`)

## Como rodar localmente

1. Suba um PostgreSQL acessível em `localhost` (o `docker-compose.yml` do projeto ainda está vazio — hoje é preciso provisionar o banco manualmente) e crie o database `multitenant-study`.
2. Defina as variáveis de ambiente exigidas por `application.properties`:

   | Variável | Descrição |
   |---|---|
   | `DB_PORT` | Porta do Postgres (ex.: `5432`) |
   | `DB_USER` | Usuário do banco |
   | `DB_PASS` | Senha do banco |

3. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   `spring.jpa.hibernate.ddl-auto=update` está ativo, então as tabelas são criadas/atualizadas automaticamente a partir das entidades.

A API sobe, por padrão do Spring Boot, em `http://localhost:8080`.

---

## Multi-tenancy: o header obrigatório

**Toda requisição, para qualquer rota, precisa do header `X-Tenant-ID`.** Ele é validado pelo `TenantFilter`, que roda antes de qualquer controller:

```
X-Tenant-ID: acme
```

- O valor é normalizado para minúsculas internamente.
- Se o header estiver ausente ou vazio, a API responde **`400 Bad Request`** antes mesmo de chegar ao controller:

  ```json
  { "error": "Tenant ID is missing in the request header." }
  ```

> **Nota de status:** o `X-Tenant-ID` já é exigido e propagado internamente (`TenantContext`), mas o filtro do Hibernate que deveria aplicar `tenant_id = :tenantId` automaticamente em cada query ainda não está ativo por uma dependência faltando no `pom.xml` (`spring-boot-starter-aop`). Ou seja: **o header é validado, mas o isolamento de dados entre tenants ainda não está garantido de ponta a ponta.** Detalhes em [`arquitetura-multitenant.md`](./arquitetura-multitenant.md).

---

## Endpoints — `Category`

Base path: `/api/categories`

### `POST /api/categories`

Cria uma categoria.

**Request body:**

```json
{
  "name": "Eletrônicos",
  "description": "Produtos eletrônicos em geral"
}
```

| Campo | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `name` | string | recomendado | **sem validação `@NotBlank`/`@Size` declarada hoje** — apesar do `@Valid` no controller, `CategoryRequest` não tem nenhuma anotação de Bean Validation nos campos, então um `name` nulo ou vazio passa pela validação HTTP e só falha (se falhar) na constraint `NOT NULL` do banco, com `500`. |
| `description` | string | não | livre |

**Respostas:**

| Status | Quando |
|---|---|
| `200 OK` | Categoria criada (sem corpo de resposta) |
| `400 Bad Request` | Header `X-Tenant-ID` ausente |
| `500 Internal Server Error` | Nome duplicado (regra de negócio lança `RuntimeException` sem tratamento HTTP dedicado) ou violação de constraint no banco |

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "X-Tenant-ID: acme" \
  -H "Content-Type: application/json" \
  -d '{"name":"Eletrônicos","description":"Produtos eletrônicos em geral"}'
```

---

### `GET /api/categories`

Lista todas as categorias.

**Resposta `200 OK`:**

```json
[
  { "id": "b2e1...", "name": "Eletrônicos", "description": "..." },
  { "id": "f9a4...", "name": "Livros", "description": null }
]
```

```bash
curl http://localhost:8080/api/categories -H "X-Tenant-ID: acme"
```

---

### `GET /api/categories/{category-id}`

Busca uma categoria por id.

| Status | Quando |
|---|---|
| `200 OK` | Encontrada — corpo é um `CategoryResponse` |
| `500 Internal Server Error` | Não encontrada (`EntityNotFoundException` sem `@ControllerAdvice`, hoje não vira `404`) |

```bash
curl http://localhost:8080/api/categories/b2e1... -H "X-Tenant-ID: acme"
```

---

### `PUT /api/categories/{category-id}`

Atualiza uma categoria existente.

**Request body:** igual ao `POST`.

⚠️ **Bug conhecido:** a implementação atual recria a entidade do zero a partir do request e só copia o `id` antigo — os campos `tenantId`, `deleted` e auditoria não vêm junto. Na prática, isso tende a quebrar com erro de constraint (`NOT NULL`) no banco. Não considere este endpoint confiável até a correção (ver `arquitetura-multitenant.md`, seção "itens pendentes").

```bash
curl -X PUT http://localhost:8080/api/categories/b2e1... \
  -H "X-Tenant-ID: acme" \
  -H "Content-Type: application/json" \
  -d '{"name":"Eletrônicos e Informática","description":"..."}'
```

---

### `DELETE /api/categories/{category-id}`

Remove uma categoria (**exclusão física**, não soft delete — apesar de existir um campo `deleted` na entidade, ele não é usado nesta operação).

⚠️ **Bug conhecido:** o `@PathVariable` deste endpoint está anotado incorretamente (`@PathVariable("/{category-id}")` em vez de `@PathVariable("category-id")`), o que deve impedir o Spring de casar a variável de path corretamente. Endpoint não confiável até a correção.

```bash
curl -X DELETE http://localhost:8080/api/categories/b2e1... -H "X-Tenant-ID: acme"
```

---

## Modelo de dados — `CategoryResponse`

```json
{
  "id": "string (UUID)",
  "name": "string",
  "description": "string | null"
}
```

Internamente, toda entidade (via `AbstractEntity`) também guarda `tenantId`, `createdAt`, `updatedAt` e `deleted`, mas esses campos **não são expostos** na API — só existem no banco.

---

## Limitações conhecidas (resumo)

Para o detalhamento completo, ver [`arquitetura-multitenant.md`](./arquitetura-multitenant.md). Em resumo, hoje:

- O isolamento por tenant nas queries ainda não está ativo de ponta a ponta (dependência Maven faltando).
- `PUT` e `DELETE` têm bugs que provavelmente causam erro em runtime.
- Não há tratamento global de exceções — erros de negócio viram `500` em vez de `404`/`409`.
- `CategoryRequest` não tem validação de campos declarada, apesar do `@Valid`.
- `docker-compose.yml` está vazio; setup do banco é manual.
