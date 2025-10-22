package apresentacao;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinMustache;
import negocio.Evento;
import negocio.Palestra;
import negocio.Participante;
import persistencia.EventoDAO;
import persistencia.PalestraDAO;
import persistencia.PalestranteDAO;
import persistencia.ParticipanteDAO;
import util.MinhasPropriedades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author iapereira
 */
public class MainWeb {

    public static String encodeImageToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static void main(String[] args) {
        Properties prop = new MinhasPropriedades().getPropertyObject();
        var app = Javalin.create(config -> {
            config.fileRenderer(new JavalinMustache());
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.jetty.multipartConfig.cacheDirectory("c:/temp");
            config.jetty.multipartConfig.maxFileSize(Integer.parseInt(prop.getProperty("MAX_SIZE")), SizeUnit.MB); 
            config.jetty.multipartConfig.maxInMemoryFileSize(10, SizeUnit.MB);                                                                                // memory
            config.jetty.multipartConfig.maxTotalRequestSize(1, SizeUnit.GB); 
        }).start(Integer.parseInt(prop.getProperty("javalin_port")));

        // com js
        app.post("/buscar_participante", ctx -> {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(ctx.body(), Map.class);
            String nome = map.get("nome");
            if (!nome.isEmpty() && !nome.isBlank()) {
                List<Participante> vetParticipante = new ParticipanteDAO().listar(nome);
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
            Palestra palestra = new Palestra();
            palestra.setTitulo(ctx.formParam("titulo"));
            palestra.setDuracao(Integer.parseInt(ctx.formParam("duracao")));

            if (ctx.uploadedFile("material") != null) {
                // TODO: não testamos ainda o limite maximo
                if (ctx.uploadedFile("material").size() > 0) {
                    palestra.setMaterial(ctx.uploadedFile("material").content().readAllBytes());
                    palestra.setMaterialTipo(ctx.uploadedFile("material").contentType());
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("mensagem", "O tamanho do arquivo deve estar entre maior que zero e menor que  "
                            + prop.getProperty("MAX_SIZE") + " mb");
                    ctx.render("templates/erro.html", map);
                }
            }
            palestra.setEvento(new EventoDAO().obter(Integer.parseInt(ctx.formParam("evento_id"))));
            new PalestraDAO().adicionar(palestra);
            ctx.redirect("/");
        });

        app.get("/palestra/nova", ctx -> {
            List<Evento> vetEvento = new EventoDAO().listar();
            Map<String, Object> map = new HashMap<>();
            map.put("vetEvento", vetEvento);
            ctx.render("/templates/palestra/tela_adicionar.html", map);
        });

        app.get("/eventos", ctx -> {
            List<Evento> vetEvento = new EventoDAO().listar();
            Map<String, Object> model = new HashMap<>();
            model.put("vetEvento", vetEvento);
            ctx.render("/templates/evento.html", model);
        });

        app.get("/baixar_material/{id}", ctx -> {
            Palestra palestra = new PalestraDAO().obter(Integer.parseInt(ctx.pathParam("id")));
            if (!palestra.getMaterialTipo().contains("zip")) {
                ctx.html("<embed src=\"data:" + palestra.getMaterialTipo() + ";base64,"
                        + encodeImageToBase64(palestra.getMaterial()) + "\">");
            } else {
                Map<String, Object> model = new HashMap<>();
                model.put("palestra", palestra);
                ctx.render("/templates/palestra/baixar.html", model);
            }
        });

        app.get("/palestras", ctx -> {
            List<Palestra> vetPalestra = new PalestraDAO().listar();
            Map<String, Object> model = new HashMap<>();
            model.put("vetPalestra", vetPalestra);
            ctx.render("/templates/palestra/index.html", model);
        });

        app.get("/", ctx -> {
            ctx.redirect("/0");
        });

        app.get("/participante/{cpf}", ctx -> {
            Participante participante = new ParticipanteDAO().obterPorCpf(ctx.pathParam("cpf"));
            Map<String, Object> map = new HashMap<>();
            map.put("participante", participante);
            ctx.render("/templates/participante.html", map);
        });

        app.get("/{pagina}", ctx -> {
            Map<String, Object> model = new HashMap<>();
            int pagina = 0;
            try {
                pagina = Integer.parseInt(ctx.pathParam("pagina"));
            } catch (Exception e) {
                pagina = 0;
            }
            int qtde = new ParticipanteDAO().quantidadePaginas();
            model.put("vetParticipante", new ParticipanteDAO().obterPorPagina(pagina));
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
            ctx.render("/templates/index.html", model);
        });

        app.get("/excluir_participante/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            map.put("mensagem", "Problema na Exclusão do Participante");
            try {
                boolean resultado = new ParticipanteDAO().excluir(Integer.parseInt(ctx.pathParam("id")));
                if (resultado) {
                    ctx.redirect("/");
                } else {
                    ctx.render("templates/erro.html", map);
                }
            } catch (Exception e) {
                ctx.render("templates/erro.html", map);
            }
        });

        app.get("/visualizar/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            Participante participante = new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id")));
            map.put("participante", participante);
            ctx.render("templates/visualizar.html", map);
        });

        app.post("/adicionar", ctx -> {
            String nome = ctx.formParam("nome");
            Participante participante = new Participante();
            participante.setNome(nome);
            var foto = ctx.uploadedFile("foto");
            // System.out.println(foto.filename());
            // System.out.println(foto.contentType());
            // System.out.println(foto.size());
            // System.out.println(foto.content().toString());
            participante.setFoto(foto.content().readAllBytes());
            // TODO: tamanho maximo
            if (foto.size() == 0 || foto.contentType().equals("image/jpeg")) {
                new ParticipanteDAO().adicionar(participante);
                ctx.redirect("/");
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("mensagem", "Imagem não é JPEG");
                ctx.render("templates/erro.html", map);
            }
        });

        app.post("/alterar_palestra", ctx -> {
            // TODO: estamos mexendo soh nos palestrantes...
            int palestraID = Integer.parseInt(ctx.formParam("palestra_id"));
            List<String> vetPalestrantesSelecionados = ctx.formParams("palestrantes");
            if (vetPalestrantesSelecionados.size() > 0) {
                new PalestraDAO().alterarPalestrantes(palestraID, vetPalestrantesSelecionados);
                ctx.redirect("/");
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("mensagem", "nenhum palestrante");
                ctx.render("templates/erro.html", map);
            }
        });

        app.get("/tela_alterar_palestra/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            Palestra palestra = new PalestraDAO().obter(Integer.parseInt(ctx.pathParam("id")));
            map.put("palestra", palestra);
            map.put("vetPalestrante", new PalestranteDAO().listar(Integer.parseInt(ctx.pathParam("id"))));
            ctx.render("/templates/palestra/tela_alterar.html", map);
        });

        app.get("/tela_alterar/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            map.put("participante", new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id"))));
            ctx.render("/templates/tela_alterar.html", map);
        });

        app.post("/alterar", ctx -> {
            String nome = ctx.formParam("nome");
            int id = Integer.parseInt(ctx.formParam("id"));
            new ParticipanteDAO().alterar(id, nome);           
            ctx.redirect("/");
        });

    }
}
