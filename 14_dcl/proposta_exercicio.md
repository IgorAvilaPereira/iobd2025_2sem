# 🧾 Exercícios – DCL e Normalização

---

## **Aplicação de DCL**

Criação do usuário da aplicação de eventos:

```sql
CREATE USER usuario_app WITH PASSWORD 'senha123';

GRANT CONNECT ON DATABASE eventos TO usuario_app;
GRANT USAGE ON SCHEMA public TO usuario_app;

GRANT SELECT, INSERT, UPDATE 
    ON participante, evento, atividade, inscricao 
    TO usuario_app;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO usuario_app;
```

---

## ✅ **Lista – DCL (contextualizado para sistema de eventos)**

### **Questão 1**

**Pergunta:** Crie um usuário chamado `usuario_teste` com a senha `senha123`.
<!--
```sql
CREATE USER usuario_teste WITH PASSWORD 'senha123';
```
-->

---

### **Questão 2**

**Pergunta:** Conceda permissão de SELECT na tabela `participante` para o usuário `usuario_teste`.
<!--
```sql
GRANT SELECT ON TABLE participante TO usuario_teste;
```
-->
---

### **Questão 3**

**Pergunta:** Revogue a permissão de SELECT na tabela `participante` do usuário `usuario_teste`.
<!--
```sql
REVOKE SELECT ON TABLE participante FROM usuario_teste;
```
-->
---

### **Questão 4**

**Pergunta:** Conceda permissão de INSERT e UPDATE na tabela `evento` para o usuário `usuario_teste`.
<!--
```sql
GRANT INSERT, UPDATE ON TABLE evento TO usuario_teste;
```
-->
---

### **Questão 5**

**Pergunta:** Crie um papel (role) chamado `coordenador_evento` e conceda permissão de DELETE na tabela `atividade` para esse papel.
<!--
```sql
CREATE ROLE coordenador_evento;
GRANT DELETE ON TABLE atividade TO coordenador_evento;
```
-->
---

### **Questão 6**

**Pergunta:** Atribua o papel `coordenador_evento` ao usuário `usuario_teste`.
<!--
```sql
GRANT coordenador_evento TO usuario_teste;
```
-->
---

### **Questão 7**

**Pergunta:** Revogue o papel `coordenador_evento` do usuário `usuario_teste`.
<!--
```sql
REVOKE coordenador_evento FROM usuario_teste;
```
-->
---

### **Questão 8**

**Pergunta:** Conceda permissão de EXECUTE em uma função chamada `calcular_lotacao_evento` para o usuário `usuario_teste`.
<!--
```sql
GRANT EXECUTE ON FUNCTION calcular_lotacao_evento() TO usuario_teste;
```
-->
---

### **Questão 9**

**Pergunta:** Crie um papel chamado `leitor` com permissão de SELECT em todas as tabelas do esquema `public`.
<!--
```sql
CREATE ROLE leitor;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO leitor;
```
-->
---

### **Questão 10**

**Pergunta:** Conceda permissão de USAGE no esquema `organizacao` para o usuário `usuario_teste`.
<!--
```sql
GRANT USAGE ON SCHEMA organizacao TO usuario_teste;
```
-->
---

### **Questão 11**

**Pergunta:** Conceda permissão de USAGE e SELECT na sequence `participante_id_seq` para o usuário `usuario_teste`.
<!--
```sql
GRANT USAGE, SELECT ON SEQUENCE participante_id_seq TO usuario_teste;
```
-->
---

### **Questão 12**

**Pergunta:** Conceda permissão de UPDATE na sequence `evento_id_seq` para o usuário `usuario_teste`.
<!--
```sql
GRANT UPDATE ON SEQUENCE evento_id_seq TO usuario_teste;
```
-->
---

### **Questão 13**

**Pergunta:** Revogue a permissão de USAGE na sequence `participante_id_seq` do usuário `usuario_teste`.
<!--
```sql
REVOKE USAGE ON SEQUENCE participante_id_seq FROM usuario_teste;
```
-->
---

### **Questão 14**

**Pergunta:** Crie um papel chamado `admin_eventos` com permissão de USAGE e UPDATE em todas as sequences do esquema `public`.
<!--
```sql
CREATE ROLE admin_eventos;
GRANT USAGE, UPDATE ON ALL SEQUENCES IN SCHEMA public TO admin_eventos;
```
-->
---

### **Questão 15**

**Pergunta:** Atribua o papel `admin_eventos` ao usuário `usuario_teste`.

<!--
```sql
GRANT admin_eventos TO usuario_teste;
```
-->
