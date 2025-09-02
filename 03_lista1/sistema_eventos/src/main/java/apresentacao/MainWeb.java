package apresentacao;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinMustache;
import negocio.Evento;
import negocio.Participante;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iapereira
 */
public class MainWeb {

    public static void main(String[] args) {
        String host = "localhost";
        String dbname = "sistema_eventos";
        String port = "5432";
        
        // String username = "neondb_owner";
        String username = "postgres";
        // String password = "npg_dBzcbu3Tj8XD";
        String password = "postgres";
        // String url = "jdbc:postgresql://ep-cool-haze-ac62qutg-pooler.sa-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_dBzcbu3Tj8XD&sslmode=require&channelBinding=require";
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

        try {
            var app = Javalin.create(config -> {
                config.fileRenderer(new JavalinMustache());
                config.staticFiles.add("/static", Location.CLASSPATH);
            }).start(7070);

             app.get("/eventos", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                String sql = "select\n" + //
                                        "    evento.nome,\n" + //
                                        "    data_inicio,\n" + //
                                        "    data_fim,\n" + //
                                        "    case \n" + //
                                        "        when data_fim < CURRENT_DATE then 'Encerrado' \n" + //
                                        "        when current_date between data_inicio and data_fim then 'em andamento' \n" + //
                                        "    else 'futuro' \n" + //
                                        "    end as status from evento;";
                ResultSet rs = conexao.prepareStatement(sql).executeQuery();
                Map<String, Object> model = new HashMap<>();
                List<Evento> vetEvento = new ArrayList<>();
                while (rs.next()) {
                    Evento evento = new Evento();
                    evento.setNome(rs.getString("nome"));
                    evento.setDataInicio(rs.getDate("data_inicio"));
                    evento.setDataFim(rs.getDate("data_fim"));
                    evento.setStatus(rs.getString("status"));
                    vetEvento.add(evento);
                }
                model.put("vetEvento", vetEvento);
                rs.close();
                conexao.close();
                ctx.render("/templates/evento.html", model);
            });

            // index - tela inicial: listamos os participantes
            app.get("/", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                String sql = "SELECT id, nome FROM participante ORDER BY id";
                ResultSet rs = conexao.prepareStatement(sql).executeQuery();
                Map<String, Object> model = new HashMap<>();
                List<Participante> vetParticipante = new ArrayList<>();
                while (rs.next()) {
                    vetParticipante.add(new Participante(rs.getInt("id"), rs.getString("nome")));
                }
                model.put("vetParticipante", vetParticipante);
                model.put("mensagem_boas_vindas", "E ai meu!, blzura?");
                rs.close();
                conexao.close();
                ctx.render("/templates/index.html", model);
            });

            // deletar por id vindo por get
            app.get("/excluir_participante/{id}", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                int id = Integer.parseInt(ctx.pathParam("id"));
                String sql = "BEGIN; DELETE FROM inscricao WHERE participante_id = " + id
                        + ";DELETE FROM participante WHERE id = " + id + "; COMMIT;";
                conexao.prepareStatement(sql).execute();
                conexao.close();
                ctx.redirect("/");
            });

            // tela_adicionar: apenas exibimos o html 
            app.get("/tela_adicionar", ctx -> {
                ctx.render("/templates/tela_adicionar.html");
            });

            // adicionar: adicionando um novo participante e redirecionando novamente para o index (listagem)
            app.post("/adicionar", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                String nome = ctx.formParam("nome");
                String sql = "INSERT INTO participante (nome) values ('" + nome + "');";
                conexao.prepareStatement(sql).execute();
                conexao.close();
                ctx.redirect("/");
            });

            // tela_alterar: recebemos o id do participante que desejamos alterar, e encaminhamos para o formulario ja com os dados pre-preenchidos
            app.get("/tela_alterar/{id}", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                Map<String, Object> model = new HashMap<>();
                String sql = "SELECT id, nome FROM participante where id = " + Integer.parseInt(ctx.pathParam("id"));
                ResultSet rs = conexao.prepareStatement(sql).executeQuery();
                Participante participante = null;
                while (rs.next()) {
                    participante = new Participante(rs.getInt("id"), rs.getString("nome"));
                }
                model.put("participante", participante);
                rs.close();
                conexao.close();
                ctx.render("/templates/tela_alterar.html", model);
            });

            // alterar: realiza a alteracao (recebendo id por hidden e um novo valor de nome)
            app.post("/alterar", ctx -> {
                Connection conexao = DriverManager.getConnection(url, username, password);
                String nome = ctx.formParam("nome");
                int id = Integer.parseInt(ctx.formParam("id"));
                String sql = "UPDATE participante SET nome = '" + nome + "' where id =" + id;
                conexao.prepareStatement(sql).execute();
                conexao.close();
                ctx.redirect("/");
            });

        } catch (Exception e) {
            // caso alguma coisa dê xabum! - principalmente no servidor ou no bd
            System.out.println("Deu xabum!");
        }
    }
}
