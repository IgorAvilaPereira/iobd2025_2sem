DROP DATABASE IF EXISTS agenda;

CREATE DATABASE agenda;

\c agenda;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE pessoa (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    caracteristicas jsonb, 
    CONSTRAINT email_unico UNIQUE (email),
    CHECK (char_length(nome) > 2) -- esta foi tb pra filha
);
INSERT INTO pessoa (nome, email, caracteristicas) VALUES
('ET BILU', 'bilu@marte.com_busqueconhecimento', 
'{"aparencia": "et", "foco": "buscar conhecimento"}'
);

INSERT INTO pessoa (nome, email, caracteristicas) VALUES
('ET de varginha', 'et_vargina@marte.com', 
'{"objetivo": "invadir a terra", "hobbie": "palavras cruzadas"}'
);

-- pega a primeira posicao -> lembrando que comeca/default eh zero
SELECT caracteristicas->'objetivo'->>1 from only pessoa where caracteristicas->>'objetivo' IS NOT NULL;


INSERT INTO pessoa (nome, email, caracteristicas) VALUES
('ET de RG', 'et_RG@rg.com', 
'{"objetivo": ["invadir a terra", "volta galvao bueno e polo naval"], "hobbie": "palavras cruzadas"}');

CREATE TABLE cliente (
    data_cadastro DATE NOT NULL DEFAULT CURRENT_DATE
) INHERITS (pessoa); -- definindo a heranca (tabela pai)
ALTER TABLE cliente ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);
ALTER TABLE cliente ADD CONSTRAINT cliente_email_unico UNIQUE (email);

INSERT INTO cliente (nome, email) values 
('igor', 'igor@gmail.com');

CREATE TABLE empresa (
    cnpj character(14) unique,
    data_cadastro DATE NOT NULL DEFAULT CURRENT_DATE
) INHERITS (pessoa); -- definindo a heranca (tabela pai)
ALTER TABLE empresa ADD CONSTRAINT empresa_pkey PRIMARY KEY (id);
ALTER TABLE empresa ADD CONSTRAINT empresa_cnpj_unico UNIQUE (cnpj);

INSERT INTO empresa (nome, email, cnpj) values 
('IFRS', 'ifrs@gmail.com', '12312312312312');


--PGPASSWORD=postgres pg_dump --host localhost --port 5432 --username postgres --format plain --create --clean --inserts --verbose --file /home/iapereira/git/iobd2025_2sem/12_heranca_postgresql/sistema_eventos.sql sistema_eventos


