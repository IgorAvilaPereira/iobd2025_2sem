package apresentacao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.swing.JOptionPane;

public class MainDesktop {
    public static void main(String[] args) {
        String host = "localhost";
        String username = "postgres";
        String port = "5432";
        String password = "postgres";
        String dbname = "sistema_eventos";
        String url = "jdbc:postgresql://"+host+":"+port+"/"+dbname;
          
        try {
            Connection conexao = DriverManager.getConnection(url, username, password);

            // inserindo um novo
            // String nome = JOptionPane.showInputDialog("Digite o nome do novo participante:");
            // String sqlInsert = "INSERT INTO participante (nome) VALUES ('"+nome+"');";
            // conexao.prepareStatement(sqlInsert).execute();

            // listando todos os participantes
            String sql = "SELECT participante.nome FROM participante ORDER BY id";
            ResultSet rs = conexao.prepareStatement(sql).executeQuery();
            String retorno = "";
            while(rs.next()){
                retorno += rs.getString("nome")+"\n";
            }
            JOptionPane.showMessageDialog(null, retorno);
            rs.close();


            String sqlView = "select * from eventos_com_inscricoes_maior_que_a_media;";
            ResultSet rsView = conexao.prepareStatement(sqlView).executeQuery();
            retorno = "";
            while(rsView.next()){
                retorno += "------------------\n";
                retorno += String.valueOf(rsView.getInt("id"))+"\n";
                retorno += rsView.getString("nome")+"\n";
                retorno += String.valueOf(rsView.getInt("qtde"))+"\n";
                retorno += "------------------\n";

            }
            JOptionPane.showMessageDialog(null, retorno);
            rsView.close();
            conexao.close();
        } catch (Exception e) {
            System.out.println("Deu xabum!");
        }
       
    }
}