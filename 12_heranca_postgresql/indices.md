# Indíces no PostgreSQL

### 🧩 Tipos principais de índices no PostgreSQL

1. **B-Tree (padrão)**

   * ✅ Usado automaticamente se você não especificar o tipo.
   * Ideal para igualdade (`=`) e comparações de ordem (`<`, `>`, `BETWEEN`, `ORDER BY`).
   * Exemplo:

     ```sql
     CREATE INDEX idx_users_name ON users (name);
     ```

2. **Hash**

   * Bom para comparações de igualdade (`=`) apenas.
   * Mais rápido que B-Tree em alguns casos específicos.
   * Exemplo:

     ```sql
     CREATE INDEX idx_users_email_hash ON users USING hash (email);
     ```

3. **GIN (Generalized Inverted Index)**

   * Usado para **arrays**, **JSONB** e **buscas de texto completo**.
   * Exemplo (para JSONB):

     ```sql
     CREATE INDEX idx_users_data_gin ON users USING gin (data);
     ```
   * Exemplo (para texto):

     ```sql
     CREATE INDEX idx_posts_content_gin ON posts USING gin (to_tsvector('portuguese', content));
     ```

4. **GiST (Generalized Search Tree)**

   * Mais flexível; usado para buscas espaciais (PostGIS), texto aproximado, etc.
   * Exemplo:

     ```sql
     CREATE INDEX idx_points_gist ON locations USING gist (geom);
     ```

5. **BRIN (Block Range INdex)**

   * Leve e eficiente para **tabelas muito grandes** e **colunas ordenadas naturalmente** (como timestamps).
   * Exemplo:

     ```sql
     CREATE INDEX idx_logs_date_brin ON logs USING brin (created_at);
     ```

---

### ⚙️ Boas práticas

* **Crie índices com propósito**: cada índice ocupa espaço e torna inserções/atualizações mais lentas.
* **Evite muitos índices**: cada `INSERT`, `UPDATE` e `DELETE` precisa atualizar todos os índices da tabela.
* **Use EXPLAIN ANALYZE** para verificar se o índice está sendo usado:

  ```sql
  EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'a@b.com';
  ```
* **Índices únicos (UNIQUE)** garantem que não haja duplicatas:

  ```sql
  CREATE UNIQUE INDEX idx_users_email_unique ON users (email);
  ```
* **Índices parciais** (para subconjuntos de dados):

  ```sql
  CREATE INDEX idx_active_users ON users (email) WHERE active = true;
  ```

---

### ✅ O que acontece ao criar uma `PRIMARY KEY`

Quando você define uma `PRIMARY KEY` em uma tabela, o PostgreSQL automaticamente:

1. Cria uma **restrição de unicidade** (ou seja, não podem existir valores duplicados nessa coluna);
2. Cria **um índice B-Tree único** para fazer essa verificação de forma eficiente.

Exemplo:

```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name TEXT,
  email TEXT
);
```

O comando acima é equivalente a:

```sql
CREATE TABLE users (
  id SERIAL,
  name TEXT,
  email TEXT,
  CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- E internamente o PostgreSQL faz algo como:
CREATE UNIQUE INDEX users_pkey ON users (id);
```

---

### ⚙️ Em resumo

| Tipo de restrição | Cria índice automaticamente? | Tipo de índice                        |
| ----------------- | ---------------------------- | ------------------------------------- |
| PRIMARY KEY       | ✅ Sim                        | B-Tree único                          |
| UNIQUE            | ✅ Sim                        | B-Tree único                          |
| FOREIGN KEY       | ❌ Não                        | (mas usa o índice da PK referenciada) |

---

### 💡 Dica prática

Você **não precisa criar um índice manualmente** na coluna da chave primária — ele já existe.
Mas, se você costuma filtrar por outras colunas (como `email` ou `created_at`), aí sim vale criar índices adicionais.

---
