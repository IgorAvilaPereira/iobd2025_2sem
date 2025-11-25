-- 1
CREATE SCHEMA esquema_cursos;
-- adicionando o esquema no conjunto de esquemas
SET search_path TO public, esquema_cursos;

-- 2
-- linha importante para habilitar uuid
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE esquema_cursos.curso (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome text not null,
    data_hora_inicio timestamp default current_timestamp,
    data_hora_fim timestamp
);
-- 3
ALTER TABLE esquema_cursos.curso ADD COLUMN detalhes JSONB;
INSERT INTO esquema_cursos.curso (nome, detalhes) VALUES
('curso sql', '{"carga_horaria": 10, "professor": "igor", "lista_topicos": ["sql", "bd"]}');
SELECT detalhes->>'professor' as professor from esquema_cursos.curso;

-- 4
CREATE TABLE esquema_cursos.arquivo (
    id serial primary key,
    nome text not null,
    arquivo bytea,
    curso_id UUID references esquema_cursos.curso (id)
);
-- 5
INSERT INTO esquema_cursos.arquivo (nome, arquivo, curso_id) VALUES
('ARQUIVO X', pg_read_binary_file('/tmp/igor.jpg'), '004d4fef-2d21-426a-b82c-d3dc5e979716');

-- 6
DROP TABLE esquema_cursos.curso_privado;

CREATE TABLE esquema_cursos.curso_privado (
    senha character varying(250) not null,
    -- herda a coluna id - mas n preserva a CONSTRAINT PRIMARY KEY referente
    PRIMARY KEY (id)
) INHERITS (esquema_cursos.curso); 

-- 7
INSERT INTO esquema_cursos.curso_privado (nome, senha) VALUES
('CURSO POO', md5('123'));

-- 8
select * from curso_privado; -- somente as tuplas da tabela curso_privado
SELECT * FROM ONLY curso; -- somente as tuplas da tabela pai (curso)
SELECT * FROM curso; -- tanto as tuplas da tabela pai (curso) como as tuplas das tabelas filhas (curso_privado)

-- 9
UPDATE curso 
    SET detalhes = detalhes || jsonb_build_object('nivel', 'supremo') WHERE id = '004d4fef-2d21-426a-b82c-d3dc5e979716';

-- 10
-- x-salada da elvira

-- 11
-- nome_curso + nome professor
SELECT id, nome||','||coalesce(detalhes->>'professor', 'SEM PROFESSOR') as resposta FROM curso;

-- 12
SELECT * FROM curso WHERE detalhes ? 'nivel';

-- OFF-TOPIC:
CREATE VIEW qtos_arquivos_cada_curso_com_nivel_tem AS SELECT curso_id, curso.nome, COALESCE(count(*), 0) FROM arquivo JOIN curso ON curso.id = arquivo.curso_id WHERE curso_id IN (SELECT id FROM curso where detalhes ? 'nivel') group by curso_id, curso.nome ORDER BY curso.nome;




