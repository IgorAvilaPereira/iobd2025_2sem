# 📝 **Lista Revisão: JSONB, Bytea, Herança de Tabelas, UUID e etc.**

### **1. Crie um schema chamado `schema_cursos` e explique por que schemas ajudam na organização do banco.**

* Pode ser necessário criar a tabela _inscricao_curso_ e _curso_ (exercício 2)

---

### **2. Crie uma tabela `curso` com chave primária UUID e um campo `informacoes` JSONB.**

Coloque a tabela no schema proposto do exercício 1

---

### **3. Insira um curso com um JSON contendo carga horária, professor e uma lista de tópicos.**

---

### **4. Crie a tabela `arquivo_curso` contendo um campo BYTEA para armazenar materiais.**

Neste caso um curso pode ter vários arquivos

---

### **5. Insira um arquivo PDF na tabela criada no exercício anterior.**

Use comandos de insert para manipulação de bytea

---

### **6. Crie uma tabela `curso_privado` que herda de `curso` e adiciona um campo senha.**

---

### **7. Insira um curso na tabela filha e explique por que ele aparece na tabela pai.**

---

### **8. Liste apenas os registros da tabela `curso_privado` usando `ONLY`.**

---

### **9. Atualize o campo JSONB de um curso para incluir um novo item "nivel" e "professor".**

---

### **11. Passe uma consulta que retorne: nome do curso + nome do inscrito.**

Se possível o nome do professor (propriedade da coluna jsonb)

---

### **12. Escreva uma query que busque cursos onde o JSONB contenha a chave `"topicos"`.**
