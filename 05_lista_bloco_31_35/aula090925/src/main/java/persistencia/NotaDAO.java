package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negocio.Nota;

public class NotaDAO {

    public List<Nota> listar() throws SQLException {
        List<Nota> vetNota = new ArrayList<>();
        String query = "SELECT * FROM nota ORDER BY id";
        Connection conexao = new ConexaoPostgreSQL().getConexao();
        PreparedStatement instrucaoSQL = conexao.prepareStatement(query);
        ResultSet resultado = instrucaoSQL.executeQuery();
        while (resultado.next()) {
            Nota nota = new Nota();   
            nota.setId(resultado.getInt("id"));
            nota.setTitulo(resultado.getString("titulo"));
            nota.setTexto(resultado.getString("texto"));
            vetNota.add(nota);
        }
        conexao.close();
        return vetNota;

    }

}
