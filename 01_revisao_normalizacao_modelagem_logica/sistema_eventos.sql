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

CREATE TABLE palestrante (
    id serial primary key,
    nome character varying(200) not null,
    biografia text,
    cpf character(11) not null,
    unique(cpf)
);  
INSERT INTO palestrante (nome, cpf) VALUES 
('IGOR AVILA PEREIRA', '17658586072');

CREATE TABLE palestra_palestrante (
    palestra_id integer references palestra (id),
    palestrante_id integer references palestrante (id),
    primary key (palestra_id, palestrante_id)
);
INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES
(1,1);

-- SELECT evento.nome, participante.nome FROM evento INNER JOIN inscricao ON (evento.id = inscricao.evento_id) INNER JOIN participante ON (participante.id = inscricao.participante_id) order by participante.nome;

-- SELECT evento.nome, palestra.titulo FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id);

-- SELECT palestra.titulo, palestrante.nome FROM palestra LEFT JOIN palestra_palestrante ON (palestra.id = palestra_palestrante.palestra_id) left JOIN palestrante ON (palestrante.id = palestra_palestrante.palestrante_id);

--  SELECT palestra.titulo, palestrante.nome FROM palestra LEFT JOIN palestra_palestrante ON (palestra.id = palestra_palestrante.palestra_id) left JOIN palestrante ON (palestrante.id = palestra_palestrante.palestrante_id) where palestra.evento_id = 1;

-- select evento.nome, count(*) as qtde FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id) group by evento.id;

-- off-topic: select evento.nome, count(*) as qtde FROM evento INNER JOIN palestra ON (evento.id = palestra.evento_id) group by evento.id having count(*) >= 2;

-- SELECT evento.nome, count(*) from evento INNER JOIN inscricao ON (evento.id = inscricao.evento_id) group by evento.id;











