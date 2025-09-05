# 🎤 **Sistema de Gestão de Eventos**

### 🎯 **Objetivo do Projeto**

Desenvolver um sistema simples de banco de dados relacional para gerenciar eventos, participantes, inscrições, palestrantes e suas apresentações.

---

## 📌 **Descrição das Relações**

### 1. **Evento**

* Cada evento possui nome, data de início, data de fim e local.
* Um evento pode ter várias **palestras**.
* Um evento pode ter vários **participantes** (via inscrições).

### 2. **Participante**

* Participante é qualquer pessoa que se inscreve em um evento.
* Pode estar inscrito em vários eventos (relação N\:N com Evento via **Inscrição**).

### 3. **Inscrição**

* Tabela associativa entre **Participante** e **Evento**.
* Relação **N\:N**, com informação adicional da data de inscrição.

### 4. **Palestrante**

* Representa o responsável por ministrar uma ou mais palestras.
* Pode participar de várias palestras (relação N\:N com Palestra via tabela **Palestra\_Palestrante**).

### 5. **Palestra**

* Cada palestra está vinculada a **um único evento**.
* Pode ter **um ou mais palestrantes** (relação N\:N).

### 6. **Palestra\_Palestrante**

* Tabela associativa entre **Palestra** e **Palestrante**.
* Permite que uma palestra tenha múltiplos palestrantes e que um palestrante participe de várias palestras.

---


## 📝 Lista de Exercícios SQL – Sistema de Gestão de Eventos

### 🔹 1–5. JOINs Simples e Compostos

1. Liste o nome dos participantes e os nomes dos eventos em que estão inscritos.
2. Liste o nome dos eventos e os nomes das palestras associadas.
3. Liste os nomes dos palestrantes e as palestras que eles ministram.
4. Liste os eventos e a quantidade de palestras que cada um possui.
5. Liste os eventos e o número total de participantes inscritos.

---

### 🔹 6–10. WHERE, IN, BETWEEN

6. Liste os eventos que ocorrem entre duas datas específicas (ex: entre '2025-01-01' e '2025-12-31').
7. Liste os participantes que estão inscritos em mais de um evento.
8. Liste os palestrantes que participaram de uma palestra com o nome contendo a palavra “tecnologia”.
9. Liste as inscrições feitas em uma data específica.
10. Liste os eventos que ocorreram em um local específico (ex: 'São Paulo').

---

### 🔹 11–15. Subselects

11. Liste os participantes que estão inscritos no evento com o maior número de palestras.
12. Liste as palestras com mais de um palestrante.
13. Liste os eventos que têm mais participantes do que a média de participantes por evento.
14. Liste os nomes dos palestrantes que não estão vinculados a nenhuma palestra.
15. Liste os participantes que não se inscreveram em nenhum evento.

[19/07](https://github.com/IgorAvilaPereira/iobd2025_2sem/blob/main/02_lista1/sistema_eventos.sql)

---

### 🔹 16–20. GROUP BY e HAVING

16. Liste a quantidade de inscrições por evento.
17. Liste os palestrantes que participam de mais de 3 palestras.
18. Liste os eventos com mais de 100 participantes.
19. Mostre a média de palestras por evento.
20. Liste os eventos cuja duração (data fim - data início) seja maior que 3 dias.

---

### 🔹 21–25. Agregações: COUNT, AVG, MAX, MIN, SUM

21. Calcule a média de participantes por evento.
22. Calcule a média de palestras por palestrante.
23. Mostre o total de eventos que já ocorreram (data fim < data atual).
24. Mostre a quantidade de palestras realizadas em cada mês.
25. Calcule a idade média (em dias) das inscrições feitas até hoje.

---

### 🔹 26–30. CASE WHEN, COALESCE, UNION, INTERSECT

26. Liste todos os eventos e, ao lado, mostre "Encerrado" se a data fim for menor que hoje, "Em andamento" se estiver entre início e fim, e "Futuro" se ainda não começou.
27. Liste os nomes das palestras e, ao lado, mostre “Sem palestrante” caso nenhuma pessoa esteja associada a ela.
28. Liste os participantes e mostre “Inscrito” se estiverem inscritos em algum evento e “Não inscrito” caso contrário.
29. Liste os nomes das pessoas que são **tanto palestrantes quanto participantes**.
30. Liste todos os nomes de pessoas envolvidas no sistema, sejam palestrantes ou participantes (sem duplicar nomes).


[02/09](https://github.com/IgorAvilaPereira/iobd2025_2sem/blob/main/03_lista1/sistema_eventos.sql)

---

### 🔹 31–35. ORDER BY, LIMIT, OFFSET

31. Liste os 5 eventos mais recentes (ordenados pela data de início decrescente).
32. Liste os 10 participantes mais antigos no sistema (ordenado pela data de inscrição mais antiga).
33. Liste os 3 palestras com o maior número de palestrantes.
34. Mostre os 5 eventos com maior número de participantes, ordenando do maior para o menor.
35. Mostre a 2ª página de participantes com 10 resultados por página (use `LIMIT` e `OFFSET`).

---

### 🔹 36–40. JOINs Variados, LIKE, Filtros

36. Liste todos os eventos e, se houver, o número de palestras associadas (use `LEFT JOIN`).
37. Liste todas as palestras, mesmo que não tenham palestrantes ainda associados.
38. Liste todos os palestrantes e, se houver, as palestras que ministram (use `RIGHT JOIN`).
39. Encontre os participantes cujos nomes começam com a letra 'A'.
40. Liste as palestras cujo nome contém a palavra "dados" (insensível a maiúsculas).

---

### 🔹 41–45. Funções de Agregação Avançadas

41. Liste o total de palestras ministradas por cada palestrante, ordenando da maior para a menor.
42. Mostre o evento com a maior quantidade de palestras.
43. Liste o número total de inscrições feitas no mês atual.
44. Calcule a soma total de palestras realizadas em todos os eventos.
45. Liste o evento com a menor duração (data fim - data início).

---

### 🔹 46–50. EXCEPT, IS NULL, NOT IN, Subselects

46. Liste os participantes que **não** estão inscritos em nenhum evento.
47. Liste as palestras **sem palestrante** associado.
48. Liste os eventos que **não têm** nenhuma palestra associada.
49. Liste os nomes dos palestrantes que **não** participam de nenhuma palestra cujo nome contenha "inovação".
50. Liste os participantes que se inscreveram em **todos os eventos que ocorreram em 2025**.
