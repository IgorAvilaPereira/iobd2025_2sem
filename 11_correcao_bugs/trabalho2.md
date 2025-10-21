# 🧾 **Trabalho 2: Controle de Acesso e Modelagem Hierárquica no Sistema de Gerenciamento de Eventos**

## **1. Introdução**

Este trabalho apresenta a **modelagem e implementação do controle de acesso e hierarquia de dados** em um **Sistema de Gerenciamento de Eventos**, utilizando o **PostgreSQL** como sistema gerenciador de banco de dados.


O projeto aplica conceitos de **Data Control Language (DCL)** para controle de segurança, juntamente com **herança de tabelas** para modelagem de diferentes tipos de eventos, além do uso de **colunas JSONB** para maior flexibilidade na representação dos dados.

O sistema visa permitir o **cadastro e gerenciamento de eventos**, o **armazenamento de arquivos associados**, e o **controle de permissões de acesso**, assegurando **integridade, segurança e escalabilidade** no tratamento das informações.

---

## **2. Objetivo**

O objetivo é projetar um modelo de banco de dados que combine **segurança, modularidade e flexibilidade**.
Isso é alcançado através do uso de **roles** para restringir o acesso por perfil de usuário, e de **tabelas herdadas** para representar especializações de eventos, com **colunas JSONB** para armazenar dados dinâmicos, como metadados e configurações específicas de cada evento.

---

## **3. Desenvolvimento**

### **3.1 Estrutura de Perfis e Permissões**

O sistema adota um **modelo de segurança baseado em papéis (roles)**, onde cada tipo de usuário possui permissões distintas definidas por comandos DCL.

Os principais perfis são: **Administrador**, **Organizador**, **Participante** e **Somente Leitura**.

---

### **3.1.1 Administrador (`app_admin`)**

**Função:**
Gerencia o sistema integralmente, incluindo criação de usuários, eventos e permissões.

**Permissões:**

* Acesso total a todas as tabelas e sequências.
* Criação, modificação e remoção de usuários.

<!--
**Comandos DCL:**

```sql
CREATE ROLE app_admin LOGIN PASSWORD 'senha_admin';
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_admin;
ALTER ROLE app_admin WITH SUPERUSER;  -- opcional
```
-->
---

### **3.1.2 Organizador (`app_organizer`)**

**Função:**
Cria e administra seus próprios eventos e arquivos relacionados.

**Permissões:**

* Inserir novos eventos e anexos.
* Atualizar e excluir apenas seus próprios eventos.
* Consultar eventos e arquivos sob sua responsabilidade.

<!--
**Comandos DCL:**

```sql
CREATE ROLE app_organizer LOGIN PASSWORD 'senha_organizador';
GRANT CONNECT ON DATABASE eventos_db TO app_organizer;
GRANT USAGE ON SCHEMA public TO app_organizer;
GRANT SELECT, INSERT, UPDATE, DELETE ON events, attachments, tickets TO app_organizer;
```
-->
---

### **3.1.3 Participante (`app_attendee`)**

**Função:**
Visualiza e participa de eventos.

**Permissões:**

* Consultar eventos nos quais está inscrito.
* Inserir registros de inscrição .
* Ler arquivos vinculados aos eventos que participa.
* Sem permissão de modificação ou exclusão de eventos.

<!--
**Comandos DCL:**

```sql
CREATE ROLE app_attendee LOGIN PASSWORD 'senha_participante';
GRANT CONNECT ON DATABASE eventos_db TO app_attendee;
GRANT USAGE ON SCHEMA public TO app_attendee;
GRANT SELECT ON events, tickets, attachments TO app_attendee;
GRANT INSERT ON tickets TO app_attendee;
```
-->
---

### **3.1.4 Usuário de Somente Leitura (`app_viewer`)**

**Função:**
Destinado à auditoria e geração de relatórios.

**Permissões:**

* Apenas leitura (SELECT).
* Sem permissão para alterar dados.

<!--
**Comandos DCL:**

```sql
CREATE ROLE app_viewer LOGIN PASSWORD 'senha_viewer';
GRANT CONNECT ON DATABASE eventos_db TO app_viewer;
GRANT USAGE ON SCHEMA public TO app_viewer;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_viewer;
```

---

### **3.1.5 Resumo Estruturado de Permissões**

| **Perfil**          | **Pode Ler**          | **Pode Inserir**     | **Pode Atualizar**  | **Pode Excluir**    | **Escopo / Observações**    |
| ------------------- | --------------------- | -------------------- | ------------------- | ------------------- | --------------------------- |
| **Administrador**   | Todas as tabelas      | Todas as tabelas     | Todas as tabelas    | Todas as tabelas    | Acesso total, ignora RLS    |
| **Organizador**     | Seus eventos e anexos | Novos eventos/anexos | Seus eventos/anexos | Seus eventos/anexos | Escopo limitado por RLS     |
| **Participante**    | Eventos inscritos     | Tickets              | —                   | —                   | Restrito às suas inscrições |
| **Somente leitura** | Todas as tabelas      | —                    | —                   | —                   | Apenas consulta             |

-->

---

### **3.2 Hierarquia de Tabelas: Eventos e Eventos Privados**

O sistema utiliza **herança de tabelas** do PostgreSQL para modelar especializações de eventos, permitindo que `evento_privado` herde os atributos comuns de `evento`.
Essa estrutura favorece a **reutilização**, **consistência de dados** e a **extensão futura** do modelo.

---

#### **3.2.1 Estrutura Hierárquica**

1. **Tabela Pai – `evento`**
   Contém os atributos principais de todos os eventos, incluindo arquivos, metadados e um campo JSONB (`detalhes`) que armazena informações estruturadas e específicas de cada evento.

2. **Tabela Filha – `evento_privado`**
   Herda os campos da tabela `evento` e adiciona colunas específicas, como senha de acesso e lista de convidados.

<!--
---

#### **3.2.2 Estrutura das Tabelas (SQL)**

```sql
-- Tabela Pai: Evento
CREATE TABLE evento (
    id_evento SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_evento DATE NOT NULL,
    local VARCHAR(200) NOT NULL,
    id_usuario INTEGER REFERENCES usuario(id_usuario),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_evento VARCHAR(20) CHECK (tipo_evento IN ('publico', 'privado')),
    arquivo BYTEA,                           -- arquivo binário (imagem, PDF, etc.)
    metadados JSONB DEFAULT '{}',            -- metadados técnicos (tipo, formato, etc.)
    detalhes JSONB DEFAULT '{
        "programacao": [],
        "contatos": {"email": null, "telefone": null},
        "links": []
    }'                                       -- informações estruturadas do evento
);

-- Tabela Filha: Evento Privado
CREATE TABLE evento_privado (
    senha_acesso VARCHAR(100),               -- senha para acessar o evento
    convidados JSONB DEFAULT '[]',           -- lista de IDs ou e-mails de convidados
    acesso_restrito BOOLEAN DEFAULT TRUE
) INHERITS (evento);
```
-->

---

#### **3.2.2 Exemplos de Uso do Campo JSONB - Tabela Pai Evento**

```sql
-- Atualizar detalhes de um evento com programação e links
UPDATE evento
SET detalhes = jsonb_build_object(
    'programacao', jsonb_build_array(
        jsonb_build_object('hora', '10:00', 'atividade', 'Palestra de Abertura'),
        jsonb_build_object('hora', '14:00', 'atividade', 'Workshop de PostgreSQL')
    ),
    'contatos', jsonb_build_object('email', 'contato@evento.com', 'telefone', '(11) 99999-9999'),
    'links', jsonb_build_array('https://evento.com/inscricao', 'https://evento.com/programacao')
)
WHERE id_evento = 1;

-- Consultar apenas os contatos do evento
SELECT detalhes->'contatos' AS contatos
FROM evento
WHERE id_evento = 1;
```

---

<!--

#### **3.2.4 Diagrama Conceitual Simplificado**

```
                ┌────────────────────┐
                │      evento        │
                │--------------------│
                │ id_evento (PK)     │
                │ titulo             │
                │ data_evento        │
                │ local              │
                │ tipo_evento        │
                │ arquivo (BYTEA)    │
                │ metadados (JSONB)  │
                │ detalhes (JSONB)   │
                └────────┬───────────┘
                         │
                         │
                 ┌────────┴─────────┐
                 │ evento_privado   │
                 │------------------│
                 │ senha_acesso     │
                 │ convidados       │
                 │ acesso_restrito  │
                 └──────────────────┘
```

---
-->

## **4. Conclusão**

A integração entre **herança de tabelas**, **colunas JSONB** e **políticas de acesso (DCL)** no PostgreSQL proporciona uma solução de **alto nível técnico e flexível** para o gerenciamento de eventos.
O uso do campo `detalhes` em formato JSONB amplia a capacidade de adaptação do sistema, permitindo armazenar informações dinâmicas sem comprometer o modelo relacional.

A estrutura proposta garante **segurança, extensibilidade e clareza** na definição de responsabilidades, tornando-se uma base sólida para sistemas acadêmicos e corporativos que demandam controle refinado de acesso e organização hierárquica de dados.

