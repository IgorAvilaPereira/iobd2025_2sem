package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import apresentacao.MainWeb;
import negocio.Participante;

public class ParticipanteDAO {



    public Participante obterPorCpf(Connection conexao, String cpf) throws SQLException {
        String sql = "SElect * FROM participante where cpf = ?;";
        PreparedStatement preparedStatement = conexao.prepareStatement(sql);
        preparedStatement.setString(1, cpf);
        ResultSet rs = preparedStatement.executeQuery();
        Participante participante = new Participante();
        if (rs.next()) {
            participante.setId(rs.getInt("id"));
            participante.setCpf(rs.getString("cpf"));
            participante.setDataNascimento(((rs.getDate("data_nascimento") != null) ? rs.getDate("data_nascimento").toLocalDate(): null));
            participante.setEmail(rs.getString("email"));
            participante.setNome(rs.getString("nome"));    
            participante.setFoto(rs.getBytes("foto"));        
        }
        preparedStatement.close();
        participante.setVetEvento(new EventoDAO().listar(conexao, participante.getId()));
        conexao.close();
        return participante;

    }

     public Participante obterPorId(Connection conexao, int id) throws SQLException {
        String sql = "SElect * FROM participante where id = ?;";
        PreparedStatement preparedStatement = conexao.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        Participante participante = new Participante();
        if (rs.next()) {
            participante.setId(rs.getInt("id"));
            participante.setCpf(rs.getString("cpf"));
            participante.setDataNascimento(((rs.getDate("data_nascimento") != null) ? rs.getDate("data_nascimento").toLocalDate(): null));
            participante.setEmail(rs.getString("email"));
            participante.setNome(rs.getString("nome"));    
            participante.setFoto(rs.getBytes("foto"));        
        }
        preparedStatement.close();
        participante.setVetEvento(new EventoDAO().listar(conexao, participante.getId()));
        conexao.close();
        return participante;

    }

    public boolean adicionar(Connection conexao, Participante participante) throws SQLException {
        String sql = "INSERT INTO participante (nome, foto) values (?, ?) RETURNING id;";
        PreparedStatement comando = conexao.prepareStatement(sql);
        comando.setString(1, participante.getNome());
        comando.setBytes(2, ((participante.getFoto().length == 0) ? null :  participante.getFoto()));
        ResultSet rs = comando.executeQuery();
        if (rs.next()) {
            participante.setId(rs.getInt("id"));
        }
        // conexao.prepareStatement(sql).execute();
        conexao.close();
        return participante.getId() != 0;
    }

}
