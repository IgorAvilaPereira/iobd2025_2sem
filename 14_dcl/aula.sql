-- criando papeis com direito a login e com senha
CREATE ROLE vergara LOGIN PASSWORD '111';
CREATE ROLE doris LOGIN PASSWORD '222';
CREATE ROLE maxsuel login PASSWORD '333';
-- jaaziel super usuario - soh perde do postgres
CREATE ROLE jaaziel LOGIN PASSWORD '111' SUPERUSER;
-- jorge herda/compartilha as mesmas permissoes da doris
CREATE ROLE jorge LOGIN PASSWORD '222' IN ROLE doris;

CREATE ROLE francine WITH PASSWORD '111' CREATEDB CREATEROLE;
-- SE ESQUECER ALGUMA CONSTANTE NA CRIACAO DO USUARIO
-- Ex: permitir login a um ROLE que nao tinha recebido a possibilidade de realizar login - mesmo com uma senha "setada"
ALTER ROLE francine WITH LOGIN;
-- trocar senha
ALTER ROLE francine WITH PASSWORD 'oi';


-- todos podem conectar em sistemas de eventos
GRANT CONNECT ON DATABASE sistema_eventos TO vergara, doris;
GRANT CONNECT ON DATABASE sistema_eventos TO maxsuel;
GRANT CONNECT ON DATABASE sistema_eventos TO francine;

-- todos podem "usar" o schema public
GRANT USAGE ON SCHEMA public TO vergara, doris;
GRANT USAGE ON SCHEMA public TO maxsuel;
GRANT USAGE ON SCHEMA public TO francine;

REVOKE ALL PRIVILEGES on SCHEMA public FROM vergara;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vergara;
GRANT SELECT, DELETE, UPDATE, INSERT ON ALL TABLES IN SCHEMA public TO vergara;
REVOKE CREATE ON SCHEMA public FROM vergara;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO vergara;

-- soh pode select em participante
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO doris;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO doris;
REVOKE UPDATE ON evento FROM doris;
REVOKE SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public FROM doris;
GRANT SELECT ON TABLE participante TO doris;

-- mostra as permissoes
-- \z

-- mostra as permissoes de um determinada tabela
-- \z inscricao

-- mostra usuarios/roles
-- \du ou \du+
