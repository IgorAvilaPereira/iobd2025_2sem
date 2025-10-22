package persistencia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import negocio.Participante;

public class ParticipanteDAO {

    public Participante obterPorCpf(String cpf) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "SELECT * FROM participante where cpf = ?;";
            PreparedStatement preparedStatement = conexao.prepareStatement(sql);
            preparedStatement.setString(1, cpf);
            ResultSet rs = preparedStatement.executeQuery();
            Participante participante = new Participante();
            if (rs.next()) {
                participante.setId(rs.getInt("id"));
                participante.setCpf(rs.getString("cpf"));
                participante.setDataNascimento(
                        ((rs.getDate("data_nascimento") != null) ? rs.getDate("data_nascimento").toLocalDate() : null));
                participante.setEmail(rs.getString("email"));
                participante.setNome(rs.getString("nome"));
                participante.setFoto(rs.getBytes("foto"));
            }
            preparedStatement.close();
            participante.setVetEvento(new EventoDAO().listar(participante.getId()));
            conexao.close();
            return participante;
        }

    }

    public Participante obter(int id) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "SElect * FROM participante where id = ?;";
            PreparedStatement preparedStatement = conexao.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            Participante participante = new Participante();
            if (rs.next()) {
                participante.setId(rs.getInt("id"));
                participante.setCpf(rs.getString("cpf"));
                participante.setDataNascimento(
                        ((rs.getDate("data_nascimento") != null) ? rs.getDate("data_nascimento").toLocalDate() : null));
                participante.setEmail(rs.getString("email"));
                participante.setNome(rs.getString("nome"));
                participante.setFoto(rs.getBytes("foto"));
            }
            preparedStatement.close();
            participante.setVetEvento(new EventoDAO().listar(participante.getId()));
            conexao.close();
            return participante;
        }

    }

    public boolean adicionar(Participante participante) throws SQLException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "INSERT INTO participante (nome, foto) values (?, ?) RETURNING id;";
            PreparedStatement comando = conexao.prepareStatement(sql);
            comando.setString(1, participante.getNome());
            comando.setBytes(2, ((participante.getFoto().length == 0) ? null : participante.getFoto()));
            ResultSet rs = comando.executeQuery();
            if (rs.next()) {
                participante.setId(rs.getInt("id"));
            }
        }
        return participante.getId() != 0;
    }

    public List<Participante> listar(String nome) throws FileNotFoundException, IOException {
        List<Participante> vetParticipante = new ArrayList<>();
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            PreparedStatement ps = conexao.prepareStatement("SELECT * FROM participante WHERE nome ILIKE ?");
            ps.setString(1, nome + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                vetParticipante.add(new Participante(rs.getInt("id"), rs.getString("nome")));
            }
            rs.close();
            conexao.close();
            return vetParticipante;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean excluir(int id) throws FileNotFoundException, IOException {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement ps1 = conexao.prepareStatement("DELETE FROM inscricao WHERE participante_id = ?");
                    PreparedStatement ps2 = conexao.prepareStatement("DELETE FROM participante WHERE id = ?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
                ps2.setInt(1, id);
                ps2.executeUpdate();
                conexao.commit();
                return true;
            } catch (Exception e) {
                conexao.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int quantidadePaginas() {
        int qtde = 1;
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sqlNro = "SELECT ceil(COUNT(*)::real/10::real)::integer as nro FROM participante;";
            ResultSet rs = conexao.prepareStatement(sqlNro).executeQuery();
            if (rs.next()) {
                qtde = rs.getInt("nro");
                return qtde;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return qtde;
    }

    public List<Participante> obterPorPagina(int pagina) throws SQLException {
        int offset = pagina * 10;
        List<Participante> vetParticipante = new ArrayList<>();
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "SELECT id, nome, cpf FROM participante ORDER BY id desc LIMIT 10 OFFSET ?;";
        PreparedStatement preparedStatement = conexao.prepareStatement(sql);
        preparedStatement.setInt(1, offset);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            vetParticipante.add(new Participante(rs.getInt("id"), rs.getString("nome"), rs.getString("cpf")));
        }
        return vetParticipante;
    }

    public void alterar(int id, String nome) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "UPDATE participante SET nome = ? where id = ?";
        PreparedStatement preparedStatement = conexao.prepareStatement(sql);
        preparedStatement.setString(1, nome);
        preparedStatement.setInt(2, id);
        preparedStatement.executeUpdate();
    }
}
