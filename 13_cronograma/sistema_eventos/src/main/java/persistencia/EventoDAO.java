package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import negocio.Evento;

public class EventoDAO {

    public List<Evento> listar(int participanteID) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "SElect evento.id, evento.nome, evento.local, evento.data_inicio, evento.data_fim FROM evento JOIN inscricao on (inscricao.evento_id = evento.id) where inscricao.participante_id = ?;";
            PreparedStatement preparedStatement = conexao.prepareStatement(sql);
            preparedStatement.setInt(1, participanteID);
            ResultSet rs = preparedStatement.executeQuery();
            List<Evento> vetEvento = new ArrayList<>();
            while (rs.next()) {
                Evento evento = new Evento();
                evento.setNome(rs.getString("nome"));
                evento.setDataInicio(rs.getDate("data_inicio"));
                evento.setDataFim(rs.getDate("data_fim"));
                // evento.setStatus(rs.getString("status"));
                evento.setLocal(rs.getString("local"));
                vetEvento.add(evento);
            }
            preparedStatement.close();
            conexao.close();
            return vetEvento;
        }
    }

    public List<Evento> listar() throws SQLException {
        String sql = "select evento.id, evento.local,\n" + //
                "    evento.nome,\n" + //
                "    data_inicio,\n" + //
                "    data_fim,\n" + //
                "    case \n" + //
                "        when data_fim < CURRENT_DATE then 'Encerrado' \n" + //
                "        when current_date between data_inicio and data_fim then 'em andamento' \n" + //
                "    else 'futuro' \n" + //
                "    end as status from evento;";
        try (ResultSet rs = new ConexaoPostgreSQL().getConnection().prepareStatement(sql).executeQuery()) {
            List<Evento> vetEvento = new ArrayList<>();
            while (rs.next()) {
                Evento evento = new Evento();
                evento.setId(rs.getInt("id"));
                evento.setNome(rs.getString("nome"));
                evento.setDataInicio(rs.getDate("data_inicio"));
                evento.setDataFim(rs.getDate("data_fim"));
                evento.setStatus(rs.getString("status"));
                evento.setLocal(rs.getString("local"));
                vetEvento.add(evento);
            }
            return vetEvento;
        }
    }

    public Evento obter(int id) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "select evento.id, evento.local,\n" + //
                    "    evento.nome,\n" + //
                    "    data_inicio,\n" + //
                    "    data_fim,\n" + //
                    "    case \n" + //
                    "        when data_fim < CURRENT_DATE then 'Encerrado' \n" + //
                    "        when current_date between data_inicio and data_fim then 'em andamento' \n" + //
                    "    else 'futuro' \n" + //
                    "    end as status from evento where evento.id = ?;";
            PreparedStatement instrucao = conexao.prepareStatement(sql);
            instrucao.setInt(1, id);
            ResultSet rs = instrucao.executeQuery();
            Evento evento = new Evento();

            if (rs.next()) {
                evento.setId(rs.getInt("id"));
                evento.setNome(rs.getString("nome"));
                evento.setDataInicio(rs.getDate("data_inicio"));
                evento.setDataFim(rs.getDate("data_fim"));
                evento.setStatus(rs.getString("status"));
                evento.setLocal(rs.getString("local"));
            }
            instrucao.close();
            return evento;
        }
    }
}
