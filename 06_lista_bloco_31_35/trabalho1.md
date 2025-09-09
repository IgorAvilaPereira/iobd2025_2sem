# Trabalho 1

Baseado no nosso sistema de eventos, implemente usando SQL, Java, JDBC, Mustache, Javalin e etc.:

## ✅ Página de dashboard com status dos eventos

### **Objetivo**

Criar uma página administrativa que exiba uma tabela com todos os eventos, mostrando:

* Nome do evento
* Data de início e fim
* Local
* Status do evento, que pode ser:

  * "Encerrado" (caso o evento já tenha terminado)
  * "Em andamento" (caso esteja acontecendo hoje)
  * "Futuro" (caso ainda não tenha começado)

### **Desafio**

* Criar a rota `/admin/eventos`
* Implementar a lógica do status no backend ou via SQL
* Renderizar a lista de eventos com seus status usando Mustache

---

## ✅ Página de perfil do participante com eventos inscritos

### **Objetivo**

Criar uma página de visualização dos dados de um participante, exibindo:

* Informações pessoais (nome, CPF, e-mail, etc.)
* Lista de eventos nos quais está inscrito, com nome, local e data

### **Desafio**

* Criar rota dinâmica `/participante/:cpf`
* Buscar os dados do participante e seus eventos via JDBC
* Exibir os dados em um template Mustache

---

## ✅ Cadastro de nova palestra com múltiplos palestrantes

### **Objetivo**

Criar um formulário para cadastro de uma nova palestra, permitindo selecionar múltiplos palestrantes para associar a ela.

### **Desafio**

* Criar rota GET `/palestra/nova` para exibir o formulário
* Criar rota POST `/palestra` para processar o cadastro
* Realizar as inserções nas tabelas `palestra` e `palestra_palestrante` usando transações JDBC
* Garantir que pelo menos um palestrante foi selecionado

---

## ✅ Relatório gerencial de palestrantes com total de palestras

### **Objetivo**

Criar uma tela de relatório que liste todos os palestrantes com a quantidade de palestras que ministram.

### **Desafio**

* Criar rota `/admin/palestrantes`
* Consultar a base de dados para obter a contagem de palestras por palestrante
* Exibir a lista ordenada por total de palestras, de forma decrescente, com Mustache

---

## ✅ Página de eventos com botão “Inscrever-se” (com verificação)

### **Objetivo**

Exibir a lista de eventos disponíveis para inscrição de um participante, com a opção de se inscrever diretamente pela interface.

### **Desafio**

* Criar rota `/eventos/disponiveis/:cpf`
* Exibir somente eventos nos quais o participante ainda não está inscrito e que ainda não encerraram
* Implementar rota POST `/inscricao` para registrar a inscrição com verificação de regras
* Mostrar mensagem de sucesso ou erro com base na lógica



