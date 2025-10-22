package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Evento;
import negocio.Palestra;

public class PalestraDAO {

    public boolean adicionar(Palestra palestra) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "INSERT INTO palestra (titulo, duracao, evento_id, material, material_tipo) values (?, ?, ?, ?, ?) RETURNING id;";
        PreparedStatement instrucao = conexao.prepareStatement(sql);
        instrucao.setString(1, palestra.getTitulo());
        instrucao.setInt(2, palestra.getDuracao());
        instrucao.setInt(3, palestra.getEvento().getId());
        instrucao.setBytes(4, ((palestra.getMaterial().length == 0) ? null : palestra.getMaterial()));
        instrucao.setString(5, palestra.getMaterialTipo());
        ResultSet rs = instrucao.executeQuery();
        if (rs.next()) {
            palestra.setId(rs.getInt("id"));
        }
        instrucao.close();
        conexao.close();
        return palestra.getId() != 0;
    }

    public List<Palestra> listar() throws SQLException {
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
            vetPalestra.add(palestra);
        }
        instrucao.close();
        conexao.close();
        return vetPalestra;
    }

    public Palestra obter(int id) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "SELECT palestra.material_tipo as palestra_material_tipo, palestra.material as palestra_material, palestra.id as id, titulo, duracao, nome, local, data_inicio, data_fim, evento.id as evento_id FROM palestra inner join evento on (evento.id = palestra.evento_id) where palestra.id = ?;";
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
            palestra.setEvento(evento);
        }
        instrucao.close();
        conexao.close();
        return palestra;
    }

    // TODO: colocar demais atributos de Palestra
    public void alterarPalestrantes(int palestraID, List<String> vetPalestrantesSelecionados) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "BEGIN; DELETE FROM palestra_palestrante WHERE palestra_id = " + palestraID + ";";
        for (int i = 0; i < vetPalestrantesSelecionados.size(); i++) {
            sql += "INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES (" + palestraID + ","
                    + Integer.parseInt(vetPalestrantesSelecionados.get(i)) + ");";
        }
        sql += "commit;";
        conexao.prepareStatement(sql).execute();
        conexao.close();
    }
}
