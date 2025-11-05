package persistencia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Evento;
import negocio.Inscricao;
import negocio.Participante;

public class ParticipanteDAO {

    public boolean alterar(Participante participante) {
        try (Connection conexao = new ConexaoPostgreSQL().getConnection()) {
            String sql = "UPDATE participante SET cpf = ?, email = ?, nome = ?, data_nascimento = ?, foto = ? where id = ?";
            PreparedStatement preparedStatement = conexao.prepareStatement(sql);
            preparedStatement.setString(1, participante.getCpf());
            preparedStatement.setString(2, participante.getEmail());
            preparedStatement.setString(3, participante.getNome());
            preparedStatement.setDate(4, Date.valueOf(participante.getDataNascimento()));
            preparedStatement.setBytes(5, participante.getFoto());
            preparedStatement.setInt(6, participante.getId());
            int linhas = preparedStatement.executeUpdate();
            return linhas == 1;
        } catch (Exception e) {
            return false;
        }
    }

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
            String sql = "SELECT * FROM participante where id = ?;";
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
            String sql = "INSERT INTO participante (cpf, email, nome, data_nascimento, foto) values (?, ?, ?, ?, ?) RETURNING id;";
            PreparedStatement comando = conexao.prepareStatement(sql);
            comando.setString(1, participante.getCpf());
            comando.setString(2, participante.getEmail());
            comando.setString(3, participante.getNome());
            comando.setDate(4, Date.valueOf(participante.getDataNascimento()));
            comando.setBytes(5, ((participante.getFoto().length == 0) ? null : participante.getFoto()));
            ResultSet rs = comando.executeQuery();
            if (rs.next()) {
                participante.setId(rs.getInt("id"));
            }
        } catch (Exception e) {
            return false;
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

    public void realizarInscricao(int participante_id, int evento_id) throws SQLException {
        Connection conexao = new ConexaoPostgreSQL().getConnection();
        String sql = "INSERT INTO inscricao (participante_id, evento_id) values (?,?)";
        PreparedStatement preparedStatement = conexao.prepareStatement(sql);
        preparedStatement.setInt(1, participante_id);
        preparedStatement.setInt(2, evento_id);
        preparedStatement.executeUpdate();
    }

    public List<Inscricao> minhasInscricoes(int participante_id) throws SQLException {
        Participante participante = obter(participante_id);
        List<Inscricao> vetEvento = new ArrayList<>();
        String sql = "SELECT *, inscricao.id as inscricao_idtop, valor::numeric(10,2) as valor_convertido FROM inscricao INNER JOIN evento on inscricao.evento_id = evento.id where inscricao.participante_id = ?;";
        Connection connection = new ConexaoPostgreSQL().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, participante_id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Inscricao inscricao = new Inscricao();
            inscricao.setId(rs.getInt("inscricao_idtop"));
            inscricao.setDataHora(rs.getObject("data_hora", java.time.LocalDateTime.class));
            inscricao.setPago(rs.getBoolean("pago"));
            inscricao.setValor(rs.getDouble("valor_convertido"));

            Evento evento = new Evento();
            evento.setId(rs.getInt("id"));
            evento.setNome(rs.getString("nome"));
            evento.setDataInicio(rs.getDate("data_inicio"));
            evento.setDataFim(rs.getDate("data_fim"));
            // evento.setStatus(rs.getString("status"));
            evento.setLocal(rs.getString("local"));

            inscricao.setEvento(evento);
            inscricao.setParticipante(participante);

            vetEvento.add(inscricao);
        }
        return vetEvento;
    }
}
