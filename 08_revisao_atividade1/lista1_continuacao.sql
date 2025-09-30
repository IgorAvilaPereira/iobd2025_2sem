-- 51
CREATE SCHEMA relatorios;

SET search_path TO public, relatorios;

CREATE TABLE relatorios.eventos_passados ( 
    id serial primary key,
    evento_id integer references evento (id)
);

INSERT INTO relatorios.eventos_passados (evento_id) select evento.id from public.evento where data_fim < CURRENT_DATE;

-- 52
DROP VIEW eventos_futuros;

CREATE VIEW eventos_futuros AS SELECT evento.id, evento.nome, STRING_AGG(palestra.titulo, ',') as nm_palestras  FROM evento LEFT JOIN palestra ON evento.id = palestra.evento_id where data_inicio > CURRENT_DATE 
GROUP BY evento.id, evento.nome;

select * from eventos_futuros;

-- 53
CREATE VIEW qtde_eventos_por_participante AS select participante.id, participante.nome, count(inscricao.evento_id) from participante LEFT JOIN inscricao on participante.id = inscricao.participante_id group by participante.id, participante.nome ORDER BY count(inscricao.evento_id) DESC;


-- 54
CREATE VIEW relatorios.qtde_palestras_por_palestrante AS SELECT palestrante.id, palestrante.nome, count(palestra_id) as qte FROM palestrante LEFT JOIN palestra_palestrante ON palestrante.id = palestra_palestrante.palestrante_id group by palestrante.id, palestrante.nome ORDER BY palestrante.id;

select * from relatorios.qtde_palestras_por_palestrante ;

-- 55
CREATE VIEW qtde_inscritos_por_evento AS Select evento.id, evento.nome, count(inscricao.participante_id) FROM evento LEFT JOIN inscricao ON evento.id = inscricao.evento_id group by evento.id, evento.nome;

SELECT * FROM qtde_inscritos_por_evento where count >= 4;

-- off-topic: left join view + tabela
SELECT * FROM qtde_inscritos_por_evento LEFT JOIN palestra ON qtde_inscritos_por_evento.id = palestra.evento_id where count >=4;

-- 56
SELECT participante.id, participante.nome FROM participante
UNION
SELECT palestrante.id, palestrante.nome FROM palestrante;

-- 57
SELECT participante.nome FROM participante INTERSECT ALL SELECT nome from palestrante;

-- 58 jump

-- 59 jump

-- 60 
SELECT participante.id, participante.nome, 'Participante' as tipo FROM participante UNION ALL SELECT palestrante.id, palestrante.nome, 'Palestrante' as tipo FROM palestrante;

-- 61 jump + eh interessante fazer

-- 62
SELECT DISTINCT palestrante.id, palestrante.nome FROM palestrante JOIN palestra_palestrante on palestrante.id = palestra_palestrante.palestrante_id join palestra on palestra.id = palestra_palestrante.palestra_id where palestra.evento_id in (select evento.id from evento where extract(year from data_inicio) = EXTRACT(year from CURRENT_DATE));

-- 63
SELECT * FROM participante join inscricao on participante.id = inscricao.participante_id where inscricao.evento_id in(SELECT evento.id FROM evento JOIN palestra on evento.id = palestra.evento_id GROUP BY evento.id having count(palestra.evento_id) >= 5);



