DROP DATABASE IF EXISTS sistema_eventos;

CREATE DATABASE sistema_eventos;

\c sistema_eventos;

CREATE TABLE evento (
    id serial primary key,
    nome character varying(200) not null,
    data_inicio date default current_date,
    hora_inicio time default current_time,
    data_fim date,
    hora_fim time,
    local text
);
INSERT INTO evento (nome, data_fim, hora_fim, local) VALUES
('AULA DE IOBD', '2025-08-12', '22:20', 'IFRS');

INSERT INTO evento (nome, local) VALUES
('AULA DE IOBD - na chuva', 'IFRS');

CREATE TABLE participante (
    id serial primary key,
    nome character varying(200) not null,
    data_nascimento date
);
INSERT INTO participante (nome, data_nascimento) VALUES
('JAAZIEL', '2002-04-23'),
('BETITO', '1900-01-01');

CREATE TABLE inscricao (
    id serial primary key,
    evento_id integer references evento (id),
    participante_id integer references participante (id),
    data_hora timestamp default current_timestamp,
    valor money default 0,
    pago boolean default false,    
    check(cast(valor as numeric(8,2)) >= 0),
    unique(evento_id, participante_id) 
);
INSERT INTO inscricao (evento_id, participante_id) VALUES
(1,1),
(1,2);

INSERT INTO inscricao (evento_id, participante_id) VALUES
(2, 1);

CREATE TABLE palestra (
    id serial primary key,
    titulo text not null,
    duracao integer check (duracao > 0),
    data_hora_inicio timestamp default current_timestamp,
    evento_id integer references evento (id)
);
INSERT INTO palestra (titulo, duracao, evento_id) VALUES
('MODELAGEM RELACIONAL', 120, 1);

INSERT INTO palestra (titulo, duracao, evento_id) VALUES
('JAVA', 120, 1);

INSERT INTO palestra (titulo, duracao, evento_id) VALUES
('BD', 120, 2);

INSERT INTO palestra (titulo, duracao, evento_id) VALUES
('OO', 120, 2);

INSERT INTO palestra (titulo, duracao, evento_id) VALUES
('WEB', 120, 2);

CREATE TABLE palestrante (
    id serial primary key,
    nome character varying(200) not null,
    biografia text,
    cpf character(11) not null,
    unique(cpf)
);  
INSERT INTO palestrante (nome, cpf) VALUES 
('IGOR AVILA PEREIRA', '17658586072');


INSERT INTO palestrante (nome, cpf) VALUES 
('MÁRCIO JOSUÉ RAMOS TORRES', '01734555555');



INSERT INTO palestrante (nome, cpf) VALUES 
('RAQUEL BARBOSA', '01734555553');

CREATE TABLE palestra_palestrante (
    palestra_id integer references palestra (id),
    palestrante_id integer references palestrante (id),
    primary key (palestra_id, palestrante_id)
);
INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES
(1,1);

INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES
(1,2);


-- SELECT evento.nome, participante.nome FROM evento INNER JOIN inscricao ON (evento.id = inscricao.evento_id) INNER JOIN participante ON (participante.id = inscricao.participante_id) order by participante.nome;

-- SELECT evento.nome, palestra.titulo FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id);

-- SELECT palestra.titulo, palestrante.nome FROM palestra LEFT JOIN palestra_palestrante ON (palestra.id = palestra_palestrante.palestra_id) left JOIN palestrante ON (palestrante.id = palestra_palestrante.palestrante_id);

--  SELECT palestra.titulo, palestrante.nome FROM palestra LEFT JOIN palestra_palestrante ON (palestra.id = palestra_palestrante.palestra_id) left JOIN palestrante ON (palestrante.id = palestra_palestrante.palestrante_id) where palestra.evento_id = 1;

-- select evento.nome, count(*) as qtde FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id) group by evento.id;

-- off-topic: select evento.nome, count(*) as qtde FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id) group by evento.id having count(*) >= 2;

-- SELECT evento.nome, count(*) from evento INNER JOIN inscricao ON (evento.id = inscricao.evento_id) group by evento.id;


-- select evento.nome FROM evento WHERE data_inicio BETWEEN '2025-01-01' and '2025-12-31';

-- SELECT participante.nome, count(*) FROM participante JOIN inscricao ON (participante.id = inscricao.participante_id) group by participante.id having count(*) >= 2;

-- SELECT participante.nome, count(*) as nro_participacoes FROM participante JOIN inscricao ON (participante.id = inscricao.participante_id) group by participante.id having count(*) >= 2;


--SELECT palestra.titulo, palestrante.nome FROM palestrante INNER JOIN palestra_palestrante on palestrante.id = palestra_palestrante.palestrante_id inner join palestra on palestra.id = palestra_palestrante.palestra_id where palestra.titulo ILIKE '%modelagem%';

--SELECT palestra.titulo, STRING_AGG(palestrante.nome, ',') as palestrantes FROM palestrante INNER JOIN palestra_palestrante on palestrante.id = palestra_palestrante.palestrante_id inner join palestra on palestra.id = palestra_palestrante.palestra_id where palestra.titulo ILIKE '%modelagem%' GROUP BY palestra.titulo;


-- SELECT inscricao.id, participante.nome FROM inscricao JOIN participante ON (inscricao.participante_id = participante.id) where cast(data_hora as date) = CURRENT_DATE;

-- SELECT evento.nome FROM evento where local = 'IFRS';

-- agora considerando empate
-- select inscricao.evento_id, inscricao.id, participante.id, participante.nome from participante inner join inscricao on (participante.id = inscricao.participante_id) where inscricao.evento_id IN (SELECT evento.id FROM evento JOIN palestra on (evento.id = palestra.evento_id) group by evento.id having count(*) = (SELECT count(*) FROM evento JOIN palestra on (evento.id = palestra.evento_id) group by evento.id order by count(*) DESC LIMIT 1));

-- SELECT palestra.titulo, count(*) FROM palestra JOIN palestra_palestrante ON palestra.id = palestra_palestrante.palestra_id group by palestra.id having count(*) >= 2;


-- subselect sucessivos
-- SELECT evento.id, evento.nome, count(*) FROM evento JOIN inscricao on evento.id = inscricao.evento_id group by evento.id  having count(*) > (SELECT AVG(cnt) FROM (SELECT count(*) as cnt FROM inscricao group by evento_id) sub);
  
-- usando with
/*
WITH tabela_media AS (
    SELECT AVG(cnt) as media FROM (SELECT count(*) as cnt FROM inscricao group by evento_id) sub
), tabela_eventos AS (
    SELECT evento.id, evento.nome, count(*) as qtde FROM evento         
    JOIN inscricao on evento.id = inscricao.evento_id group by evento.id
) 
SELECT * FROM tabela_eventos WHERE qtde > (SELECT media FROM tabela_media);
*/

-- SELECT * FROM participante WHERE id NOT IN (SELECT participante_id from inscricao);


/*
CREATE VIEW eventos_com_inscricoes_maior_que_a_media AS (WITH tabela_media AS (                                                                                       SELECT AVG(cnt) as media FROM (SELECT count(*) as cnt FROM inscricao group by evento_id) sub                
), tabela_eventos AS (                                                  
    SELECT evento.id, evento.nome, count(*) as qtde FROM evento         
    JOIN inscricao on evento.id = inscricao.evento_id group by evento.id
)                                                                          
SELECT * FROM tabela_eventos WHERE qtde > (SELECT media FROM tabela_media));
*/

-- SELECT evento_id, evento.nome, count(inscricao.id) as qtde_inscricoes FROM inscricao INNER JOIN evento ON (evento.id = inscricao.evento_id) group by inscricao.evento_id, evento.nome order by inscricao.evento_id;

/*
SELECT palestrante.id, palestrante.nome, count(*) FROM
palestrante INNER JOIN palestra_palestrante ON (palestrante.id = palestra_palestrante.palestrante_id) group by palestrante.id, palestrante.nome having count(*) >= 3;
*/

-- SELECT evento.id, evento.nome, count(inscricao.id) FROM evento INNER JOIN inscricao ON (evento.id = inscricao.evento_id) GROUP BY evento.id, evento.nome HAVING count(inscricao.id) >= 2;

-- SELECT evento.id, avg(palestra.id)::numeric(10,1) FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id) GROUP BY evento.id;

-- select id, nome, data_fim, data_inicio from evento where data_fim - data_inicio >= 3

-- 26
/*
SELECT
    evento.nome,
    data_inicio,
    data_fim,
    CASE 
        WHEN data_fim < CURRENT_DATE THEN 'Encerrado' 
        WHEN current_date BETWEEN data_inicio AND data_fim THEN 'em andamento' 
    ELSE 'futuro' 
    END AS status 
FROM evento;
*/

/*
SELECT 
    palestra.id, palestra.titulo, 
    STRING_AGG(COALESCE(palestrante.nome, 'Sem palestrante'),',') as palestrantes 
FROM palestra LEFT JOIN palestra_palestrante ON
(palestra.id = palestra_palestrante.palestra_id) 
LEFT JOIN palestrante ON (palestrante.id = palestra_palestrante.palestrante_id) 
GROUP BY 
        palestra.id, 
        palestra.titulo order by palestra.id;


*/

/*
SELECT palestra.id, palestra.titulo, case when count(palestra_palestrante.palestrante_id) > 0 then 'tem' else 'n tem' end FROM palestra LEFT JOIN palestra_palestrante ON (palestra.id = palestra_palestrante.palestra_id) group by palestra.id, palestra.titulo;
 id 
*/

-- 31
SELECT id, data_inicio FROM evento WHERE data_inicio <= CURRENT_DATE ORDER BY data_inicio DESC LIMIT 5;

-- 31
SELECT id, data_inicio FROM evento WHERE data_inicio <= CURRENT_DATE UNION SELECT id, data_inicio FROM evento WHERE data_inicio <= CURRENT_DATE ORDER BY data_inicio ASC LIMIT 5;

-- 32
 SELECT participante.nome, cast(data_hora as date) FROM inscricao join participante on inscricao.participante_id = participante.id ORDER BY data_hora ASC LIMIT 10;
 
  INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES (3, 1);

INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES (3, 2);

-- 33
SELECT evento.id, evento.nome FROM evento JOIN palestra ON (evento.id = palestra.evento_id) join palestra_palestrante on (palestra.id = palestra_palestrante.palestra_id) group by evento.id, evento.nome having count(*) = (select count(*) FROM evento JOIN palestra on evento.id = palestra.evento_id join palestra_palestrante on (palestra.id = palestra_palestrante.palestra_id) group by evento.id ORDER BY count(*) DESC LIMIT 1) ORDER BY evento.id;

-- 34 mesma ideia do 33
SELECT * FROM participante LIMIT 10;
SELECT * FROM participante LIMIT 10 OFFSET 10;


