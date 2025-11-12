package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import negocio.Evento;
import negocio.Palestra;

public class PalestraDAO {

    public boolean adicionar(Palestra palestra) throws SQLException, JsonProcessingException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "INSERT INTO palestra (titulo, duracao, evento_id, material, material_tipo, palavras_chave) values (?, ?, ?, ?, ?, ?::jsonb) RETURNING id;";
        PreparedStatement instrucao = conexao.prepareStatement(sql);
        instrucao.setString(1, palestra.getTitulo());
        instrucao.setInt(2, palestra.getDuracao());
        instrucao.setInt(3, palestra.getEvento().getId());
        instrucao.setBytes(4, ((palestra.getMaterial().length == 0) ? null : palestra.getMaterial()));
        instrucao.setString(5, palestra.getMaterialTipo());
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(palestra.getVetPalavraChave());
        instrucao.setString(6, jsonString);
        ResultSet rs = instrucao.executeQuery();
        if (rs.next()) {
            palestra.setId(rs.getInt("id"));
        }
        instrucao.close();
        conexao.close();
        return palestra.getId() != 0;
    }

    public List<Palestra> listar() throws SQLException, JsonMappingException, JsonProcessingException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        List<Palestra> vetPalestra = new ArrayList<>();
        String sql = "SELECT * FROM palestra ORDER BY id DESC;";
        PreparedStatement instrucao = conexao.prepareStatement(sql);
        ResultSet rs = instrucao.executeQuery();
        while (rs.next()) {
            Palestra palestra = new Palestra();
            palestra.setId(rs.getInt("id"));
            palestra.setTitulo(rs.getString("titulo"));
            palestra.setDuracao(rs.getInt("duracao"));
            palestra.setMaterial(rs.getBytes("material"));
            palestra.setMaterialTipo(rs.getString("material_tipo"));
            if (rs.getString("palavras_chave") != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(rs.getString("palavras_chave"));
                Iterator<JsonNode> iterator = jsonNode.iterator();
                while (iterator.hasNext()) {
                    palestra.getVetPalavraChave().add(iterator.next().asText());
                }
            }
            vetPalestra.add(palestra);
        }
        instrucao.close();
        conexao.close();
        return vetPalestra;
    }

    public Palestra obter(int id) throws SQLException, JsonMappingException, JsonProcessingException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "SELECT palestra.palavras_chave, palestra.material_tipo as palestra_material_tipo, palestra.material as palestra_material, palestra.id as id, titulo, duracao, nome, local, data_inicio, data_fim, evento.id as evento_id FROM palestra inner join evento on (evento.id = palestra.evento_id) where palestra.id = ?;";
        PreparedStatement instrucao = conexao.prepareStatement(sql);
        instrucao.setInt(1, id);
        ResultSet rs = instrucao.executeQuery();
        Palestra palestra = new Palestra();
        while (rs.next()) {
            palestra.setId(rs.getInt("id"));
            palestra.setTitulo(rs.getString("titulo"));
            palestra.setDuracao(rs.getInt("duracao"));
            palestra.setMaterial(
                    ((rs.getBytes("palestra_material") != null) ? rs.getBytes("palestra_material") : null));
            palestra.setMaterialTipo(rs.getString("palestra_material_tipo"));
            Evento evento = new Evento();
            evento.setNome(rs.getString("nome"));
            evento.setId(rs.getInt("evento_id"));
            evento.setDataFim(rs.getDate("data_fim"));
            evento.setDataInicio(rs.getDate("data_inicio"));
            evento.setLocal(rs.getString("local"));
            if (rs.getString("palavras_chave") != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(rs.getString("palavras_chave"));
                Iterator<JsonNode> iterator = jsonNode.iterator();
                while (iterator.hasNext()) {
                    palestra.getVetPalavraChave().add(iterator.next().asText());
                }
            }
            palestra.setEvento(evento);
        }
        instrucao.close();
        conexao.close();
        return palestra;
    }

    // TODO: colocar evento na jogada!
    public void alterar(Palestra palestra, List<String> vetPalestrantesSelecionados)
            throws SQLException, JsonProcessingException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "BEGIN; DELETE FROM palestra_palestrante WHERE palestra_id = " + palestra.getId() + ";";
        for (int i = 0; i < vetPalestrantesSelecionados.size(); i++) {
            sql += "INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES (" + palestra.getId() + ","
                    + Integer.parseInt(vetPalestrantesSelecionados.get(i)) + ");";
        }
        if (palestra.getVetPalavraChave().size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(palestra.getVetPalavraChave());
            sql += "UPDATE palestra SET titulo = '" + palestra.getTitulo() + "', duracao = " + palestra.getDuracao()
                    + ", palavras_chave = '" + jsonString + "'::jsonb where id = " + palestra.getId() + ";";
        } else {
            sql += "UPDATE palestra SET titulo = '" + palestra.getTitulo() + "', duracao = " + palestra.getDuracao()
                    + " where id = " + palestra.getId() + ";";
        }
        sql += "commit;";
        System.out.println(sql);
        conexao.prepareStatement(sql).execute();
        conexao.close();
    }

    public boolean excluir(int id) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        conexao.setAutoCommit(false);
        try (PreparedStatement ps1 = conexao.prepareStatement("DELETE FROM palestra_palestrante where palestra_id = ?");
                PreparedStatement ps2 = conexao.prepareStatement("DELETE FROM palestra WHERE id = ?")) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
            ps2.setInt(1, id);
            ps2.executeUpdate();
            conexao.commit();
            return true;
        } catch (Exception e) {
            conexao.rollback();
        }
        return false;
    }
}
