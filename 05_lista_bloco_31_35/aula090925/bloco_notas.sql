DROP DATABASE IF EXISTS bloco_notas;

CREATE DATABASE bloco_notas;

\c bloco_notas;

CREATE SCHEMA administrativo;

SET search_path TO public, administrativo;

CREATE TABLE administrativo.usuario (
    id uuid DEFAULT gen_random_uuid() primary key,
    email text,
    nome text not null,
    unique (email)
);

CREATE VIEW administrativo.view_usuario AS SELECT * FROM administrativo.usuario;

CREATE TABLE nota (
    id serial primary key,
    titulo text,
    texto text,
    usuario_id uuid references administrativo.usuario (id)
);

INSERT INTO nota (titulo, texto) values ('titulo1', 'texto1');
INSERT INTO nota (titulo, texto) values ('titulo2', 'texto2');;
