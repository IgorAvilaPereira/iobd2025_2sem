-- 1 Pergunta: Crie um usuário chamado usuario_teste com a senha senha123.
CREATE ROLE usuario_eventos LOGIN PASSWORD '111';
GRANT CONNECT ON DATABASE sistema_eventos TO usuario_eventos;
GRANT USAGE ON SCHEMA public TO usuario_eventos;

-- 2 Pergunta: Crie um usuário chamado usuario_teste com a senha senha123.
GRANT SELECT ON TABLE participante TO usuario_eventos;

-- 3 Pergunta: Revogue a permissão de SELECT na tabela participante do usuário usuario_teste.
REVOKE SELECT ON TABLE participante FROM usuario_eventos;

-- 4 Pergunta: Revogue a permissão de SELECT na tabela participante do usuário usuario_teste.
GRANT INSERT, UPDATE ON TABLE participante TO usuario_eventos;
-- motivo: a pk/coluna eh serial >> sequencia
GRANT USAGE, SELECT ON SEQUENCE participante_id_seq TO usuario_eventos;

-- 5 Pergunta: Crie um papel (role) chamado coordenador_evento e conceda permissão de DELETE na tabela palestra para esse papel.
CREATE ROLE coordenador_evento LOGIN PASSWORD '111';
GRANT CONNECT ON DATABASE sistema_eventos TO coordenador_evento;
GRANT USAGE ON SCHEMA public TO coordenador_evento;
-- deletar e visualizar a propria tabela palestra
GRANT SELECT, DELETE ON TABLE palestra TO coordenador_evento;
-- deletar e visualizar a tabela intermediaria que liga palestrantes as palestras - se isso n fosse autorizado, o coordenador_evento n conseguiria deletar nenhuma palestra com palestrante
GRANT SELECT, DELETE ON TABLE palestra_palestrante TO coordenador_evento;

-- ex:
--BEGIN; DELETE FROM palestra_palestrante where palestra_id = 1; DELETE FROM palestra where id = 1; commit;

-- 6 Pergunta: Atribua o papel coordenador_evento ao usuário usuario_teste.
-- usuario_eventos ganha os privilegios/poderes de coordenador_evento (acumulando os que ja tem)
GRANT coordenador_evento TO usuario_eventos;
-- ex:
--BEGIN; DELETE FROM palestra_palestrante where palestra_id = 3; DELETE FROM palestra where id = 3; commit; 

-- 7 Pergunta: Revogue o papel coordenador_evento do usuário usuario_teste..
REVOKE coordenador_evento FROM usuario_eventos;

-- 8 Pergunta: Conceda permissão de EXECUTE em uma função chamada calcular_lotacao_evento para o usuário usuario_eventos.
CREATE FUNCTION total_inscritos(integer) RETURNS integer AS
$$
DECLARE
    qtde integer := 0;
BEGIN
    SELECT coalesce(count(*), 0) from inscricao where evento_id = $1 INTO qtde;
    RETURN qtde;
END;
$$ LANGUAGE 'plpgsql';

GRANT SELECT ON TABLE inscricao TO usuario_eventos;
GRANT EXECUTE ON FUNCTION total_inscritos(integer) TO usuario_eventos;

-- 9 Pergunta: Crie um papel chamado leitor com permissão de SELECT em todas as tabelas do esquema public.
CREATE ROLE leitor LOGIN PASSWORD '111';
GRANT CONNECT ON DATABASE sistema_eventos TO leitor;
GRANT USAGE ON SCHEMA public TO leitor;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO leitor;

-- 10 Pergunta: Conceda permissão de USAGE no esquema organizacao para o usuário usuario_eventos.
CREATE SCHEMA organizacao;
GRANT USAGE ON SCHEMA organizacao TO usuario_eventos;
-- extra
GRANT SELECT ON ALL TABLES IN SCHEMA organizacao TO usuario_eventos;
CREATE TABLE organizacao.equipe (id serial primary key, nome text);





