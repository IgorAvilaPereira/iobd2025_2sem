package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Palestrante;

public class PalestranteDAO {

    public List<Palestrante> listar() throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            List<Palestrante> vetPalestrante = new ArrayList<>();
            String sql = "SELECT * FROM palestrante ;";
            PreparedStatement instrucao = conexao.prepareStatement(sql);
            ResultSet rs = instrucao.executeQuery();
            while (rs.next()) {
                Palestrante palestrante = new Palestrante();
                palestrante.setId(rs.getInt("id"));
                palestrante.setNome(rs.getString("nome"));
                palestrante.setBiografia(rs.getString("biografia"));
                palestrante.setCpf(rs.getString("cpf"));
                vetPalestrante.add(palestrante);
            }
            instrucao.close();
            conexao.close();
            return vetPalestrante;
        }

        
    }

     public List<Palestrante> listar(int palestra_id) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            List<Palestrante> vetPalestrante = new ArrayList<>();
            String sql = "SELECT * FROM palestrante ;";
            PreparedStatement instrucao = conexao.prepareStatement(sql);
            ResultSet rs = instrucao.executeQuery();
            while (rs.next()) {
                Palestrante palestrante = new Palestrante();
                palestrante.setId(rs.getInt("id"));
                palestrante.setNome(rs.getString("nome"));
                palestrante.setBiografia(rs.getString("biografia"));
                palestrante.setCpf(rs.getString("cpf"));
                String sql2 = "SELECT * FROM palestra_palestrante where palestra_id = ? and palestrante_id = ? ;";
                PreparedStatement instrucao2 = conexao.prepareStatement(sql2);
                instrucao2.setInt(1, palestra_id);
                instrucao2.setInt(2, palestrante.getId());
                ResultSet rs2 = instrucao2.executeQuery();
                if (rs2.next()){
                    palestrante.ehPalestrante(true);
                }            
                vetPalestrante.add(palestrante);
            }
            instrucao.close();
            conexao.close();
            return vetPalestrante;
        }

        
    }

}
