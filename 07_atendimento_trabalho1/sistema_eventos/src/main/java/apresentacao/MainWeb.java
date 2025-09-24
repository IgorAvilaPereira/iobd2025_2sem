package apresentacao;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinMustache;
import negocio.Evento;
import negocio.Palestra;
import negocio.Participante;
import persistencia.EventoDAO;
import persistencia.PalestraDAO;
import persistencia.PalestranteDAO;
import persistencia.ParticipanteDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        // String url =
        // "jdbc:postgresql://ep-cool-haze-ac62qutg-pooler.sa-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_dBzcbu3Tj8XD&sslmode=require&channelBinding=require";
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

        // try {
        var app = Javalin.create(config -> {
            config.fileRenderer(new JavalinMustache());
            config.staticFiles.add("/static", Location.CLASSPATH);
        }).start(7070);

        // com js
        app.post("/buscar_participante", ctx -> {

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(ctx.body(), Map.class);

            String nome = map.get("nome");
            if (!nome.isEmpty() && !nome.isBlank()) {
                String sql = "SELECT * FROM participante where nome ILIKE '" + nome + "%'";
                Connection conexao = DriverManager.getConnection(url, username, password);
                ResultSet rs = conexao.prepareStatement(sql).executeQuery();
                List<Participante> vetParticipante = new ArrayList<>();
                while (rs.next()) {
                    vetParticipante.add(new Participante(rs.getInt("id"), rs.getString("nome")));
                }
                rs.close();
                conexao.close();
                ctx.json(vetParticipante);
            } else {
                ctx.json(new ArrayList<>());
            }
        });

        app.get("/tela_buscar_participante", ctx -> {
            ctx.render("/templates/tela_buscar_participante.html");
        });

        app.get("/tela_adicionar", ctx -> {
            ctx.render("/templates/tela_adicionar.html");
        });

        app.post("/adicionar_palestra", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            Palestra palestra = new Palestra();
            palestra.setTitulo(ctx.formParam("titulo"));
            palestra.setDuracao(Integer.parseInt(ctx.formParam("duracao")));
            palestra.setEvento(new EventoDAO().obter(conexao, Integer.parseInt(ctx.formParam("evento_id"))));
            conexao = DriverManager.getConnection(url, username, password);
            new PalestraDAO().adicionar(conexao, palestra);
            conexao.close();
            ctx.redirect("/");
        });

        app.get("/palestra/nova", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            List<Evento> vetEvento = new EventoDAO().listar(conexao);
            Map<String, Object> map = new HashMap<>();
            map.put("vetEvento", vetEvento);
            ctx.render("/templates/palestra/tela_adicionar.html", map);
        });

        app.get("/eventos", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            List<Evento> vetEvento = new EventoDAO().listar(conexao);
            Map<String, Object> model = new HashMap<>();
            model.put("vetEvento", vetEvento);
            ctx.render("/templates/evento.html", model);
        });

        app.get("/palestras", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            List<Palestra> vetPalestra = new PalestraDAO().listar(conexao);
            Map<String, Object> model = new HashMap<>();
            model.put("vetPalestra", vetPalestra);
            ctx.render("/templates/palestra/index.html", model);
        });

        app.get("/", ctx -> {
            ctx.redirect("/0");
        });

        app.get("/participante/{cpf}", ctx -> {
            Participante participante = new ParticipanteDAO()
                    .obterPorCpf(DriverManager.getConnection(url, username, password), ctx.pathParam("cpf"));
            Map<String, Object> map = new HashMap<>();
            map.put("participante", participante);
            ctx.render("/templates/participante.html", map);
        });

        app.get("/{pagina}", ctx -> {
            int pagina = 0;
            try {
                pagina = Integer.parseInt(ctx.pathParam("pagina"));
            } catch (Exception e) {
                pagina = 0;
            }
            Connection conexao = DriverManager.getConnection(url, username, password);
            String sqlNro = "SELECT ceil(COUNT(*)::real/10::real)::integer as nro FROM participante;";
            ResultSet rs = conexao.prepareStatement(sqlNro).executeQuery();
            int qtde = 1;
            if (rs.next()) {
                qtde = rs.getInt("nro");
            }

            String sql = "SELECT id, nome FROM participante ORDER BY id LIMIT 10 OFFSET " + (pagina * 10);
            rs = conexao.prepareStatement(sql).executeQuery();
            Map<String, Object> model = new HashMap<>();
            List<Participante> vetParticipante = new ArrayList<>();
            while (rs.next()) {
                vetParticipante.add(new Participante(rs.getInt("id"), rs.getString("nome")));
            }
            model.put("vetParticipante", vetParticipante);
            model.put("mensagem_boas_vindas", "E ai meu!, blzura?");

            if (qtde > 1 && pagina == 0) {
                model.put("proximo", 1);
            } else {
                if (qtde > 1 && pagina >= 1 && pagina + 1 != qtde) {
                    model.put("proximo", pagina + 1);
                    model.put("anterior", pagina - 1);
                } else {
                    if (qtde > 1 && pagina >= 1) {
                        model.put("anterior", pagina - 1);
                    }
                }
            }
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

        // adicionar: adicionando um novo participante e redirecionando novamente para o
        // index (listagem)
        app.post("/adicionar", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            String nome = ctx.formParam("nome");
            String sql = "INSERT INTO participante (nome) values ('" + nome + "');";
            conexao.prepareStatement(sql).execute();
            conexao.close();
            ctx.redirect("/");
        });

        app.post("/alterar_palestra", ctx -> {
            int palestraID = Integer.parseInt(ctx.formParam("palestra_id"));
            List<String> vetPalestrantesSelecionados = ctx.formParams("palestrantes");
            if (vetPalestrantesSelecionados.size() > 0) {
                Connection conexao = DriverManager.getConnection(url, username, password);
                String sql = "BEGIN; DELETE FROM palestra_palestrante WHERE palestra_id = " + palestraID + ";";
                for (int i = 0; i < vetPalestrantesSelecionados.size(); i++) {
                    sql += "INSERT INTO palestra_palestrante (palestra_id, palestrante_id) VALUES (" + palestraID + ","
                            + Integer.parseInt(vetPalestrantesSelecionados.get(i)) + ");";
                }
                sql += "commit;";
                conexao.prepareStatement(sql).execute();
                conexao.close();
                ctx.redirect("/");
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("mensagem", "nenhum palestrante");
                ctx.render("templates/erro.html", map);
            }
        });

        app.get("/tela_alterar_palestra/{id}", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            Map<String, Object> map = new HashMap<>();
            Palestra palestra = new PalestraDAO().obterPorId(conexao, Integer.parseInt(ctx.pathParam("id")));
            map.put("palestra", palestra);
            conexao = DriverManager.getConnection(url, username, password);
            map.put("vetPalestrante", new PalestranteDAO().listar(conexao, Integer.parseInt(ctx.pathParam("id"))));
            ctx.render("/templates/palestra/tela_alterar.html", map);
        });

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

        app.post("/alterar", ctx -> {
            Connection conexao = DriverManager.getConnection(url, username, password);
            String nome = ctx.formParam("nome");
            int id = Integer.parseInt(ctx.formParam("id"));
            String sql = "UPDATE participante SET nome = '" + nome + "' where id =" + id;
            conexao.prepareStatement(sql).execute();
            conexao.close();
            ctx.redirect("/");
        });

    }
}
