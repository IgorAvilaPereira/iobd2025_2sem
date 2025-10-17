# MainWeb

## 🧨 **1. SQL Injection (grave)**

👉 Exemplo:

```java
String sql = "SELECT * FROM participante where nome ILIKE '" + nome + "%'";
```

e

```java
String sql = "UPDATE participante SET nome = '" + nome + "' where id =" + id;
```

e também vários outros (`DELETE`, `INSERT`, `SELECT`…).

❌ Problema: concatenação direta de parâmetros do usuário → **SQL Injection**.

✅ Correto:

```java
PreparedStatement ps = conexao.prepareStatement(
    "SELECT * FROM participante WHERE nome ILIKE ?");
ps.setString(1, nome + "%");
```

---

## 🔓 **2. Transações manuais mal tratadas**

Exemplo:

```java
String sql = "BEGIN; DELETE FROM inscricao WHERE participante_id = " + id
        + ";DELETE FROM participante WHERE id = " + id + "; COMMIT;";
conexao.prepareStatement(sql).execute();
```

❌ Problema:

* Transações abertas via SQL inline são perigosas e difíceis de controlar em caso de erro.
* Não há tratamento de rollback.

✅ Correto:

```java
conexao.setAutoCommit(false);
try (PreparedStatement ps1 = conexao.prepareStatement("DELETE FROM inscricao WHERE participante_id = ?");
     PreparedStatement ps2 = conexao.prepareStatement("DELETE FROM participante WHERE id = ?")) {
    ps1.setInt(1, id);
    ps1.executeUpdate();
    ps2.setInt(1, id);
    ps2.executeUpdate();
    conexao.commit();
} catch (Exception e) {
    conexao.rollback();
}
```

---

## 🧹 **3. Conexões abertas manualmente em cada rota**

👉 Várias rotas:

```java
Connection conexao = DriverManager.getConnection(url, username, password);
```

❌ Problema:

* **Sem pool de conexões** → lentidão e falhas sob carga.
* Nenhum `try-with-resources` para fechar corretamente em caso de exceção.

✅ Correto:

* Usar **HikariCP** ou outro pool.
* Fechar `Connection` com `try-with-resources`.

---

## 🔥 **4. Sem tratamento adequado de exceções**

👉 Nenhuma rota usa `try/catch` nas queries.
Se algo falhar, a aplicação cai ou retorna 500 genérico.

✅ Correto:

* Adicionar tratamento com logging.
* Retornar mensagens de erro amigáveis no `ctx`.

---

## 🕵️ **5. Exposição de mensagens técnicas no navegador**

```java
System.out.println(foto.filename());
System.out.println(foto.contentType());
```

❌ Problema:

* Log no console em produção pode vazar dados sensíveis.
* Melhor usar logger com níveis adequados (`debug`, `info`).

---

## 📎 **6. Falta de validação em uploads**

👉 Exemplo:

```java
if (ctx.uploadedFile("material").size() != 0) {
    palestra.setMaterial(ctx.uploadedFile("material").content().readAllBytes());
}
```

❌ Problema:

* Nenhuma verificação de extensão real ou tipo confiável.
* Pode permitir upload arbitrário (ex: `.exe`, `.php`...).

✅ Correto:

* Validar MIME e extensão.
* Limitar tamanho (já configurado parcialmente).

---

## 💾 **7. Re-leitura desnecessária de conexões**

👉 Em `/adicionar_palestra`:

```java
Connection conexao = DriverManager.getConnection(...);
palestra.setEvento(new EventoDAO().obter(conexao, ...));
conexao = DriverManager.getConnection(...); // desnecessário
new PalestraDAO().adicionar(conexao, palestra);
```

❌ Problema:

* Abre duas conexões desnecessárias.
  ✅ Use uma única conexão.

---

## 🧭 **8. Hardcode de credenciais e paths**

```java
String username = "postgres";
String password = "postgres";
config.jetty.multipartConfig.cacheDirectory("c:/temp");
```

❌ Problema:

* Senha em código → **vazamento fácil**.
* Diretório fixo → quebra em outros sistemas operacionais.

✅ Correto:

* Usar variáveis de ambiente.
* Diretórios relativos / configuráveis.

---

## 🐢 **9. Sem paginação protegida (offset injection)**

```java
String sql = "SELECT id, nome, cpf FROM participante ORDER BY id desc LIMIT 10 OFFSET " + (pagina * 10);
```

❌ Problema:

* `pagina` vem da URL → pode gerar SQL injection ou crash.
* Nenhum limite máximo.

✅ Correto:

* Usar PreparedStatement ou validar `pagina` rigorosamente.

---

## 🔐 **10. Falta de autenticação/autorização**

👉 Qualquer usuário pode:

* Excluir participantes (`/excluir_participante/{id}`),
* Adicionar/alterar palestras,
* Fazer uploads.

✅ Correto:

* Implementar autenticação mínima (ex: token, sessão).
* Proteger rotas sensíveis.

---

## 🧭 **11. Falta de encoding e Content-Type adequados**

👉 Exemplo:

```java
ctx.html("<embed src=\"data:"+palestra.getMaterialTipo()+";base64,"+encodeImageToBase64(palestra.getMaterial())+"\">");
```

❌ Problema:

* Falta cabeçalho HTTP adequado.
* Pode quebrar em navegadores e gerar XSS.

---

## 🕳 **12. Possíveis NullPointerException**

👉 Exemplo:

```java
if (ctx.uploadedFile("material").size() != 0) { ... }
```

Se `uploadedFile("material")` for `null`, lança NullPointerException.

---

## 🧭 **13. Lógica de paginação frágil**

* Quando `pagina` é inválido, pega 0 — comportamento silencioso.
* Melhor: retornar erro ou corrigir.

---

## ⚠️ **14. Não uso de DAO corretamente em todas as rotas**

👉 Algumas rotas usam DAO (`PalestraDAO`), outras fazem SQL direto.
❌ Inconsistência → manutenção difícil.

✅ Melhor centralizar no DAO.

---

## 🧯 **15. Código não escalável**

* Tudo no `main`.
* Nenhuma separação entre controller / service / dao.
* Dificulta manutenção e testes.

---

## ✅ Sugestões de correção geral:

* [ ] Usar `try-with-resources` em todas as conexões.
* [ ] Substituir concatenação SQL por `PreparedStatement`.
* [ ] Usar **HikariCP** para pooling de conexões.
* [ ] Centralizar operações no DAO.
* [ ] Proteger rotas sensíveis com autenticação.
* [ ] Validar todos os inputs.
* [ ] Remover hardcode de senha e path.
* [ ] Melhorar logging e tratamento de erro.
* [ ] Separar a aplicação em camadas: `controller`, `service`, `dao`.

---


# EventoDAO

---

## 🧨 **1. Fechamento da `Connection` dentro da DAO (erro grave)**

Em vários métodos você faz:

```java
preparedStatement.close();
conexao.close();
```

👉 Isso é **um bug comum**.
A responsabilidade de **fechar a conexão** deve ser de **quem abriu a conexão** (no caso, a camada de controle/serviço), **não da DAO**.

Por quê?

* Se você usar a mesma `Connection` para múltiplas operações (ex: transação), a DAO fecha a conexão no meio da operação e quebra o fluxo.
* Isso impede uso de connection pool corretamente.
* Pode gerar `SQLException: connection is closed`.

✅ Correto:

```java
preparedStatement.close();
rs.close();
```

👉 **Remova todos os `conexao.close()` da DAO.**

---

## 🧯 **2. Falta de fechamento do ResultSet em alguns métodos**

Exemplo em `listar(Connection conexao)`:

```java
ResultSet rs = conexao.prepareStatement(sql).executeQuery();
// ...
conexao.close(); // <-- fecha conexão, mas nunca fecha rs explicitamente
```

👉 Isso pode causar **memory leaks** no driver JDBC.
✅ Correto:

```java
try (PreparedStatement ps = conexao.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        ...
    }
}
```

---

## 🧭 **3. Falta de tratamento de exceções (ou rollback)**

Todos os métodos simplesmente propagam `SQLException`.
👉 Se ocorrer um erro no meio, nada é tratado — o chamador tem que fazer tudo.

✅ Melhor prática:

* Usar `try-with-resources`.
* Lançar exceções personalizadas ou registrar logs.
* (Em transações) aplicar rollback.

---

## 🧠 **4. Lógica duplicada de construção de objeto Evento**

Nos três métodos, você repete:

```java
evento.setId(rs.getInt("id"));
evento.setNome(rs.getString("nome"));
evento.setDataInicio(rs.getDate("data_inicio"));
evento.setDataFim(rs.getDate("data_fim"));
evento.setStatus(rs.getString("status"));
evento.setLocal(rs.getString("local"));
```

👉 Isso gera código repetido e difícil de manter.

✅ Correto:
Crie um método privado de mapeamento:

```java
private Evento mapEvento(ResultSet rs) throws SQLException {
    Evento evento = new Evento();
    evento.setId(rs.getInt("id"));
    evento.setNome(rs.getString("nome"));
    evento.setDataInicio(rs.getDate("data_inicio"));
    evento.setDataFim(rs.getDate("data_fim"));
    evento.setStatus(rs.getString("status"));
    evento.setLocal(rs.getString("local"));
    return evento;
}
```

E então:

```java
while (rs.next()) {
    vetEvento.add(mapEvento(rs));
}
```

---

## 📎 **5. SQL hardcoded sem alias consistente**

Você usa `evento.nome` em alguns lugares, e `nome` diretamente em outros.
Isso **não é bug**, mas reduz a clareza e pode causar conflitos se houver joins com tabelas com colunas de mesmo nome.

✅ Sugestão:

```sql
SELECT e.id, e.nome, e.local, e.data_inicio, e.data_fim, ...
FROM evento e
```

---

## 🧭 **6. Falta de suporte a status nulo em `listar(Connection, int)`**

No método:

```java
evento.setNome(rs.getString("nome"));
evento.setDataInicio(rs.getDate("data_inicio"));
evento.setDataFim(rs.getDate("data_fim"));
evento.setLocal(rs.getString("local"));
```

Você não seta `status` — mas no outro `listar` você seta.
👉 Isso pode gerar `NullPointerException` na camada de apresentação, dependendo do template.

✅ Corrigir para manter consistência:

```java
evento.setStatus(null);
```

Ou melhor: incluir o `CASE` no SQL também nessa query.

---

## 🧰 **7. Uso de tipo cru (`java.sql.Date`)**

Você usa `rs.getDate()` → isso devolve `java.sql.Date` que é legado.
👉 Para aplicações modernas é melhor usar `LocalDate` e `rs.getObject("coluna", LocalDate.class)`.

✅ Exemplo:

```java
evento.setDataInicio(rs.getObject("data_inicio", LocalDate.class));
```

---

## ⚠️ **8. Falta de paginação ou filtros seguros**

As queries retornam **todos os eventos**, o que pode ser ruim se houver milhares de registros.

✅ Sugestão:
Adicionar parâmetros opcionais de paginação (`LIMIT`/`OFFSET`) no `listar`.

---

## ✅ Resumo dos bugs/problemas:

| Problema                          | Impacto                      | Gravidade |
| --------------------------------- | ---------------------------- | --------- |
| Fechar conexão na DAO             | Transações quebradas / leaks | 🚨 Alta   |
| ResultSet não fechado             | Memory leak                  | 🚨 Alta   |
| Código repetido                   | Manutenção difícil           | 🟡 Média  |
| Inconsistência no status          | NPE em templates             | 🟡 Média  |
| Falta de PreparedStatement seguro | (aqui já está ok)            | ✅ OK      |
| SQL não paginado                  | Lentidão em grandes volumes  | 🟡 Média  |

---

## ✨ Exemplo de versão corrigida:

```java
public class EventoDAO {

    public List<Evento> listar(Connection conexao) throws SQLException {
        String sql = """
            SELECT e.id, e.local, e.nome, e.data_inicio, e.data_fim,
                   CASE
                       WHEN e.data_fim < CURRENT_DATE THEN 'Encerrado'
                       WHEN CURRENT_DATE BETWEEN e.data_inicio AND e.data_fim THEN 'Em andamento'
                       ELSE 'Futuro'
                   END AS status
            FROM evento e
        """;

        List<Evento> eventos = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                eventos.add(mapEvento(rs));
            }
        }
        return eventos;
    }

    public List<Evento> listar(Connection conexao, int participanteID) throws SQLException {
        String sql = """
            SELECT e.id, e.nome, e.local, e.data_inicio, e.data_fim,
                   CASE
                       WHEN e.data_fim < CURRENT_DATE THEN 'Encerrado'
                       WHEN CURRENT_DATE BETWEEN e.data_inicio AND e.data_fim THEN 'Em andamento'
                       ELSE 'Futuro'
                   END AS status
            FROM evento e
            JOIN inscricao i ON i.evento_id = e.id
            WHERE i.participante_id = ?
        """;

        List<Evento> eventos = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, participanteID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapEvento(rs));
                }
            }
        }
        return eventos;
    }

    public Evento obter(Connection conexao, int id) throws SQLException {
        String sql = """
            SELECT e.id, e.local, e.nome, e.data_inicio, e.data_fim,
                   CASE
                       WHEN e.data_fim < CURRENT_DATE THEN 'Encerrado'
                       WHEN CURRENT_DATE BETWEEN e.data_inicio AND e.data_fim THEN 'Em andamento'
                       ELSE 'Futuro'
                   END AS status
            FROM evento e
            WHERE e.id = ?
        """;

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvento(rs);
                }
            }
        }
        return null;
    }

    private Evento mapEvento(ResultSet rs) throws SQLException {
        Evento evento = new Evento();
        evento.setId(rs.getInt("id"));
        evento.setNome(rs.getString("nome"));
        evento.setLocal(rs.getString("local"));
        evento.setDataInicio(rs.getDate("data_inicio"));
        evento.setDataFim(rs.getDate("data_fim"));
        evento.setStatus(rs.getString("status"));
        return evento;
    }
}
```

---

# PalestraDAO

## 🧯 **1. Fechamento de conexão dentro dos métodos DAO**

**Trecho:**

```java
preparedStatement.close();
conexao.close();
```

**Problema:**

* O DAO **não deveria fechar a conexão** — quem abriu deve fechar.
* Isso quebra transações compostas (ex: inserir evento + palestras no mesmo commit).
* Também pode causar `SQLException: Connection is closed` se outra operação usar a mesma conexão.

✅ **Correção sugerida:**
Remover todos os `conexao.close()` dos métodos DAO.
A responsabilidade de fechar deve ser da camada de serviço ou controller.

---

## 🧪 **2. Recursos não fechados corretamente**

**Trecho:**

```java
ResultSet rs = preparedStatement.executeQuery();
// ...
preparedStatement.close();
conexao.close();
```

**Problema:**

* O `ResultSet` **não está sendo fechado** explicitamente.
* Se ocorrer exceção antes do `.close()`, ele vaza recursos.

✅ **Correção sugerida:**
Usar **try-with-resources**:

```java
try (PreparedStatement ps = conexao.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    ...
}
```

---

## 🧠 **3. Potencial NPE no insert da palestra**

**Trecho:**

```java
instrucao.setBytes(4, ((palestra.getMaterial().length == 0) ? null : palestra.getMaterial()));
```

**Problema:**

* Se `palestra.getMaterial()` for `null`, gera `NullPointerException`.
* Não há verificação de `null` antes de acessar `.length`.

✅ **Correção sugerida:**

```java
byte[] material = palestra.getMaterial();
instrucao.setBytes(4, (material == null || material.length == 0) ? null : material);
```

---

## 🪝 **4. ResultSet com `while` quando só um resultado é esperado**

**Trecho:**

```java
Palestra palestra = new Palestra();
while (rs.next()) {
    ...
}
```

**Problema:**

* Para métodos como `obterPorId`, você espera **no máximo 1 registro**.
* Usar `while` aqui pode mascarar dados duplicados ou confundir manutenção.

✅ **Correção sugerida:**
Usar `if (rs.next()) { ... }` em vez de `while`.

---

## ⚠️ **5. Falta de tratamento de exceções adequado**

**Trecho:**

```java
throws SQLException
```

**Problema:**

* As exceções SQL sobem sem logging → dificulta depuração.
* Em ambiente web, isso pode estourar até o cliente.

✅ **Correção sugerida:**

* Logar o erro antes de propagar.
* Ou encapsular em exceção de negócio mais amigável.

---

## 🧭 **6. SQL com *SELECT *** (PalestraDAO.listar)**

**Trecho:**

```java
String sql = "SELECT * FROM palestra ORDER BY id DESC;";
```

**Problema:**

* Pode quebrar se alguém adicionar coluna nova.
* Afeta desempenho (lê mais dados que o necessário).
* Dificulta legibilidade.

✅ **Correção sugerida:**
Selecionar apenas colunas necessárias:

```sql
SELECT id, titulo, duracao, material, material_tipo FROM palestra ORDER BY id DESC;
```

---

## 🧩 **7. Falta de transações explícitas**

* Os métodos abrem e fecham conexão sem gerenciar `commit/rollback`.
* Se houver várias operações dependentes, não há atomicidade.

✅ **Correção sugerida:**

* Usar `conexao.setAutoCommit(false)` no nível de serviço.
* Controlar commit/rollback manualmente.

---

## 🧼 **8. Inconsistência de nomenclatura e legibilidade**

* `"SElect"` no primeiro método (`listar` com participanteID) — typo.
* Strings SQL multiline sem formatação clara.
* Código misturando responsabilidades (DAO gerenciando conexão).

✅ **Correção sugerida:**
Padronizar SQL (tudo minúsculo ou maiúsculo coerente) e manter claro:

```java
String sql = """
    SELECT e.id, e.nome, e.local, e.data_inicio, e.data_fim
    FROM evento e
    JOIN inscricao i ON i.evento_id = e.id
    WHERE i.participante_id = ?
""";
```

---

## 📊 **9. Falta de validação de dados retornados**

* Métodos como `obter` ou `obterPorId` retornam objetos vazios se não encontrar nada.
* Isso pode gerar `NullPointerException` depois.

✅ **Correção sugerida:**
Retornar `null` ou `Optional<Evento>` para sinalizar ausência de dados.

---

## 🧨 **10. Possível SQL Injection futura**

Atualmente os parâmetros estão usando `PreparedStatement` (👍),
mas é importante **garantir que nenhum SQL é concatenado dinamicamente** em pontos futuros (por exemplo, filtros dinâmicos).

---

## 📝 **11. Falta de relacionamento no listar palestra**

* `listar()` em `PalestraDAO` retorna palestras **sem evento associado**.
* Pode causar `NullPointerException` ao tentar acessar `palestra.getEvento().getNome()` no front.

✅ **Correção sugerida:**
Fazer JOIN com evento ou preencher `evento` dentro de `listar`.

---

✅ **Resumo dos problemas principais:**

| Categoria                          | Impacto                    | Onde aparece                |
| ---------------------------------- | -------------------------- | --------------------------- |
| Conexão fechada indevidamente      | Quebra transações          | Todos métodos DAO           |
| Recursos não fechados corretamente | Vazamento de recursos      | Todos métodos com ResultSet |
| NullPointerException possível      | Crash da aplicação         | PalestraDAO.adicionar       |
| Estrutura de ResultSet             | Comportamento incorreto    | PalestraDAO.obterPorId      |
| Falta de tratamento de exceções    | Dificuldade de depuração   | Todos                       |
| SQL ruim (* vs colunas)            | Risco de quebra/desempenho | PalestraDAO.listar          |

---


# PalestranteDAO

## 🧯 1. ❌ **Fechamento de conexão no DAO** (de novo)

**Trechos:**

```java
instrucao.close();
conexao.close();
```

em ambos os métodos `listar`.

**Problema:**

* O DAO não deve fechar a conexão.
* Se você chamar `listar` como parte de uma transação maior (ex: listar palestrantes e palestras na mesma requisição), o `conexao.close()` quebra as demais operações.
* Também pode gerar `java.sql.SQLException: Connection is closed`.

✅ **Correção sugerida:**
Remover `conexao.close()` e deixar o fechamento para a camada de serviço/controlador.

---

## 🧪 2. ⚠️ **Recursos não fechados corretamente**

* `ResultSet rs` e `ResultSet rs2` nunca são fechados explicitamente.
* `instrucao2` também não é fechado.

⚡ Isso pode gerar **vazamento de recursos** (leaks), principalmente em aplicações web com muitos acessos simultâneos.

✅ **Correção sugerida:**
Use **try-with-resources**:

```java
try (PreparedStatement ps = conexao.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    ...
}
```

Para o segundo statement:

```java
try (PreparedStatement ps2 = conexao.prepareStatement(sql2)) {
    ps2.setInt(1, palestra_id);
    ps2.setInt(2, palestrante.getId());
    try (ResultSet rs2 = ps2.executeQuery()) {
        if (rs2.next()) { ... }
    }
}
```

---

## 🧠 3. ⚠️ **Consulta SQL da sobrecarga `listar(Connection, int)` não filtra nada**

**Trecho:**

```java
String sql = "SELECT * FROM palestrante ;";
```

Mesmo no método que recebe `palestra_id`, você **não usa** esse parâmetro na primeira query.

✅ **Correção sugerida:**
Ou:

* Filtra palestrantes relacionados à palestra com JOIN:

```sql
SELECT p.* 
FROM palestrante p
JOIN palestra_palestrante pp ON p.id = pp.palestrante_id
WHERE pp.palestra_id = ?
```

Ou:

* Mantenha a query atual e use a consulta extra apenas se realmente for necessário.

💡 *Mas o mais eficiente é fazer tudo em uma única query — a consulta duplicada dentro do loop é um gargalo sério de performance.*

---

## 🐢 4. 🚨 **Consulta dentro do loop** (problema de desempenho N+1)

**Trecho:**

```java
while (rs.next()) {
    ...
    PreparedStatement instrucao2 = conexao.prepareStatement(sql2);
    instrucao2.setInt(1, palestra_id);
    instrucao2.setInt(2, palestrante.getId());
    ResultSet rs2 = instrucao2.executeQuery();
```

**Problema:**

* Para cada palestrante encontrado, você faz uma nova query `SELECT * FROM palestra_palestrante`.
* Se houver 100 palestrantes → serão 101 consultas ao banco (1 + 100)!
* Isso é o clássico problema de **N+1 queries**, que degrada drasticamente a performance.

✅ **Correção sugerida:**
Fazer uma única query com JOIN:

```sql
SELECT p.*, (pp.palestra_id IS NOT NULL) AS vinculado
FROM palestrante p
LEFT JOIN palestra_palestrante pp
  ON p.id = pp.palestrante_id
  AND pp.palestra_id = ?
```

Depois, no Java:

```java
palestrante.ehPalestrante(rs.getBoolean("vinculado"));
```

---

## 🧼 5. ⚠️ `SELECT *` novamente

**Problema:**

* Seleciona todas as colunas, inclusive as que não são usadas.
* Fragiliza manutenção (quebra quando o schema muda).

✅ **Correção sugerida:**
Selecionar apenas `id`, `nome`, `biografia`, `cpf` — as realmente usadas.

---

## 🧭 6. 🚨 Falta de fechamento de `instrucao2` e `rs2`

Mesmo se você não mudar a abordagem, o código atual não fecha `instrucao2` nem `rs2`. Isso é **vazamento de recurso confirmado**.

---

## 🧨 7. 🛑 Lógica incorreta de `ehPalestrante`

Você usa:

```java
palestrante.ehPalestrante(true);
```

Mas:

* Se a consulta não retornar nada, o valor padrão do campo `ehPalestrante` no objeto pode ficar **não inicializado**, dependendo da classe.
* Também não está limpando o valor para o caso de falsos.

✅ Melhor:

```java
palestrante.ehPalestrante(rs2.next());
```

ou, no caso da query otimizada:

```java
palestrante.ehPalestrante(rs.getBoolean("vinculado"));
```

---

## 🪝 8. Sem tratamento de exceções/logging

* O método simplesmente propaga `SQLException` sem logar nada.
* Isso complica muito a depuração em produção.

✅ Sugestão:

* Logar a query e o erro antes de propagar.
* Ou encapsular em uma `DAOException` customizada.

---

## 📝 Resumo dos problemas

| Categoria                    | Impacto principal                 | Onde acontece             |
| ---------------------------- | --------------------------------- | ------------------------- |
| Fechamento de conexão no DAO | Quebra transações / uso incorreto | Ambos métodos             |
| Recursos não fechados        | Vazamento de conexão              | `rs`, `instrucao2`, `rs2` |
| Falta de filtro na query     | Lógica errada                     | `listar(Connection, int)` |
| Consulta dentro do loop      | Gargalo de performance N+1        | `listar(Connection, int)` |
| SELECT *                     | Fragilidade / performance ruim    | Ambas queries             |
| Falta de logging             | Dificulta depuração               | Ambos métodos             |
| Lógica booleana simplista    | Resultado inconsistente           | `ehPalestrante(true)`     |

---

✅ **Sugestão de reescrita simplificada do segundo método:**

```java
public List<Palestrante> listar(Connection conexao, int palestraId) throws SQLException {
    List<Palestrante> lista = new ArrayList<>();
    String sql = """
        SELECT p.id, p.nome, p.biografia, p.cpf,
               (pp.palestra_id IS NOT NULL) AS vinculado
        FROM palestrante p
        LEFT JOIN palestra_palestrante pp
          ON p.id = pp.palestrante_id
          AND pp.palestra_id = ?
        ORDER BY p.nome
    """;

    try (PreparedStatement ps = conexao.prepareStatement(sql)) {
        ps.setInt(1, palestraId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Palestrante palestrante = new Palestrante();
                palestrante.setId(rs.getInt("id"));
                palestrante.setNome(rs.getString("nome"));
                palestrante.setBiografia(rs.getString("biografia"));
                palestrante.setCpf(rs.getString("cpf"));
                palestrante.ehPalestrante(rs.getBoolean("vinculado"));
                lista.add(palestrante);
            }
        }
    }
    return lista;
}
```

---

# ParticipanteDAO

---

## 🧯 1. ❌ Fechamento de conexão dentro do DAO (de novo)

**Trechos:**

```java
conexao.close();
```

em todos os métodos.

👉 Isso é um dos maiores problemas estruturais do projeto.
Quando você fecha a conexão no DAO, **a camada superior perde o controle transacional** e fica impossível fazer várias operações na mesma conexão (por exemplo, cadastrar participante + inscrição na mesma transação).

✅ Correção:

* O DAO **não deve fechar** a conexão.
* Quem abriu a conexão (no `MainWeb`) é quem deve fechá-la no `finally` ou no try-with-resources.

---

## 🧹 2. ⚠️ Recursos não fechados corretamente

* `ResultSet rs` **nunca é fechado**.
* `PreparedStatement preparedStatement` também **poderia** estar num bloco try-with-resources.
* Em `adicionar`, `ResultSet` também fica aberto.

✅ Correção:
Use:

```java
try (PreparedStatement ps = conexao.prepareStatement(sql)) {
    ps.setString(1, cpf);
    try (ResultSet rs = ps.executeQuery()) {
        ...
    }
}
```

---

## 🧠 3. ⚠️ Dependência circular entre DAOs

**Trecho:**

```java
participante.setVetEvento(new EventoDAO().listar(conexao, participante.getId()));
```

👉 Isso significa:

* O `ParticipanteDAO` depende do `EventoDAO`.
* O `EventoDAO` também fecha a conexão.
* Então, se você precisar de um participante **sem precisar dos eventos**, não há opção.
* Também gera **duplo fechamento da conexão**, podendo causar `SQLException: Connection is closed`.

✅ Melhor:

* Separar responsabilidades.
* O `ParticipanteDAO` deveria **só retornar os dados do participante**.
* O carregamento de eventos deveria estar:

  * ou em um **serviço** intermediário (`ParticipanteService`),
  * ou em um método específico tipo `obterComEventos`.

Exemplo:

```java
Participante p = participanteDAO.obterPorId(conn, id);
p.setVetEvento(eventoDAO.listar(conn, id));
```

---

## 🐞 4. 🚨 Lógica perigosa no tratamento de `foto`

**Trecho:**

```java
comando.setBytes(2, ((participante.getFoto().length == 0) ? null :  participante.getFoto()));
```

👉 Se `getFoto()` for `null` (ex: formulário sem upload), isso vai gerar:

```
NullPointerException
```

✅ Melhor:

```java
byte[] foto = participante.getFoto();
comando.setBytes(2, (foto == null || foto.length == 0) ? null : foto);
```

---

## 🧨 5. 🚨 `SELECT *` novamente

**Trechos:**

```java
String sql = "SElect * FROM participante where cpf = ?;";
```

* Fragiliza manutenção: se o schema mudar, o código quebra silenciosamente.
* Carrega dados desnecessários.

✅ Melhor:

```java
String sql = "SELECT id, cpf, nome, email, data_nascimento, foto FROM participante WHERE cpf = ?";
```

---

## ⚡ 6. 🚨 SQL mal formatado (`SElect`)

Você escreveu `"SElect"` com S maiúsculo e E minúsculo.
O PostgreSQL aceita, mas **isso indica descuido de padronização**. Em código real, pode atrapalhar revisões ou linters.

✅ Padronize:

```java
SELECT id, ...
```

---

## 🛑 7. ⚠️ Sem tratamento de exceções / logging

Se algo falhar, a exceção sobe crua. Isso:

* Não gera log útil,
* Não identifica em qual query falhou,
* Dificulta suporte em produção.

✅ Melhor:

* Logar a exceção.
* Opcionalmente encapsular em `DAOException`.

---

## 🧪 8. 🚨 `obterPorCpf` e `obterPorId` duplicam código

* Os dois métodos fazem exatamente a mesma coisa, mudando apenas o parâmetro.
* Isso é duplicação desnecessária.

✅ Melhor:
Criar um método privado comum:

```java
private Participante mapearParticipante(ResultSet rs) throws SQLException {
    Participante p = new Participante();
    p.setId(rs.getInt("id"));
    p.setCpf(rs.getString("cpf"));
    if (rs.getDate("data_nascimento") != null) {
        p.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
    }
    p.setEmail(rs.getString("email"));
    p.setNome(rs.getString("nome"));
    p.setFoto(rs.getBytes("foto"));
    return p;
}
```

E usar esse método nos dois.

---

## 🧼 9. 🚨 A `Connection` está sendo fechada **depois de ser usada por outro DAO**

Na linha:

```java
participante.setVetEvento(new EventoDAO().listar(conexao, participante.getId()));
conexao.close();
```

👉 Se `EventoDAO` também fecha a conexão (e ele fecha!), o `conexao.close()` aqui vai tentar fechar **duas vezes a mesma conexão**, podendo causar:

```
org.postgresql.util.PSQLException: This connection has already been closed.
```

✅ Melhor:
Remover os `close()` dos DAOs e deixar um único ponto de fechamento.

---

## 📝 Resumo dos problemas no `ParticipanteDAO`

| Problema                           | Impacto principal                        | Onde                        |
| ---------------------------------- | ---------------------------------------- | --------------------------- |
| Fechar conexão no DAO              | Quebra transações / dificulta reuso      | Todos os métodos            |
| Recursos não fechados              | Vazamento de recursos                    | Todos                       |
| Dependência circular com EventoDAO | Dificulta manutenção, causa double close | `obterPorCpf`, `obterPorId` |
| `getFoto().length` sem null check  | NullPointerException potencial           | `adicionar`                 |
| SELECT *                           | Fragilidade e overhead                   | Todos                       |
| Falta de logging                   | Debug difícil                            | Todos                       |
| Duplicação de código               | Dificulta manutenção                     | `obterPorCpf`, `obterPorId` |
| Fechar conexão duas vezes          | SQLException: connection closed          | obterPorCpf                 |
| SQL mal formatado                  | Baixa legibilidade                       | obterPorCpf                 |

---

✅ **Sugestão de versão mais profissional:**

```java
public class ParticipanteDAO {

    public Participante obterPorCpf(Connection conexao, String cpf) throws SQLException {
        String sql = """
            SELECT id, cpf, nome, email, data_nascimento, foto
            FROM participante
            WHERE cpf = ?
        """;

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearParticipante(rs);
                }
            }
        }
        return null;
    }

    public Participante obterPorId(Connection conexao, int id) throws SQLException {
        String sql = """
            SELECT id, cpf, nome, email, data_nascimento, foto
            FROM participante
            WHERE id = ?
        """;

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearParticipante(rs);
                }
            }
        }
        return null;
    }

    public boolean adicionar(Connection conexao, Participante participante) throws SQLException {
        String sql = "INSERT INTO participante (nome, foto) VALUES (?, ?) RETURNING id;";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, participante.getNome());
            byte[] foto = participante.getFoto();
            ps.setBytes(2, (foto == null || foto.length == 0) ? null : foto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    participante.setId(rs.getInt("id"));
                }
            }
        }
        return participante.getId() != 0;
    }

    private Participante mapearParticipante(ResultSet rs) throws SQLException {
        Participante p = new Participante();
        p.setId(rs.getInt("id"));
        p.setCpf(rs.getString("cpf"));
        if (rs.getDate("data_nascimento") != null) {
            p.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
        }
        p.setEmail(rs.getString("email"));
        p.setNome(rs.getString("nome"));
        p.setFoto(rs.getBytes("foto"));
        return p;
    }
}
```

---

👉 Repare que:

* Não fecha mais a conexão.
* Fecha recursos corretamente com try-with-resources.
* Evita duplicação de código.
* Elimina SELECT *.
* Elimina double close.
* Evita NPE com `foto`.

---




