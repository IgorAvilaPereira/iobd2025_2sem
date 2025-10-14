package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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
            participante.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
            participante.setEmail(rs.getString("email"));
            participante.setNome(rs.getString("nome"));            
        }
        preparedStatement.close();
        participante.setVetEvento(new EventoDAO().listar(conexao, participante.getId()));
        conexao.close();
        return participante;

    }

}
