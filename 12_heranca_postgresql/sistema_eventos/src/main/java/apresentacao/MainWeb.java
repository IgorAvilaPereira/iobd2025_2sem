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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Gatherer.Integrator;

import javax.print.DocFlavor.STRING;

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
            config.jetty.multipartConfig.maxInMemoryFileSize(10, SizeUnit.MB); // memory
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
            ctx.render("/templates/participante/tela_buscar_participante.html");
        });

        app.get("/tela_adicionar", ctx -> {
            ctx.render("/templates/participante/tela_adicionar.html");
        });

        app.post("/adicionar_palestra", ctx -> {
            Palestra palestra = new Palestra();
            palestra.setTitulo(ctx.formParam("titulo"));
            palestra.setDuracao(Integer.parseInt(ctx.formParam("duracao")));
            String palavras_chave = ctx.formParam("palavras_chave");
            palestra.setVetPalavraChave(Arrays.asList(palavras_chave.split(";")));

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
            ctx.render("/templates/participante/participante.html", map);
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

        app.get("/excluir_palestra/{id}", ctx -> {

            Map<String, Object> map = new HashMap<>();
            map.put("mensagem", "Problema na Exclusão do Palestra");
            try {
                boolean resultado = new PalestraDAO().excluir(Integer.parseInt(ctx.pathParam("id")));
                if (resultado) {
                    ctx.redirect("/palestras");
                } else {
                    ctx.render("templates/erro.html", map);
                }
            } catch (Exception e) {
                ctx.render("templates/erro.html", map);
            }

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
            map.put("vetInscricao", new ParticipanteDAO().minhasInscricoes(Integer.parseInt(ctx.pathParam("id"))));
            ctx.render("templates/participante/visualizar.html", map);
        });

        app.post("/adicionar", ctx -> {
            Map<String, Object> map = new HashMap<>();
            String nome = ctx.formParam("nome");
            String cpf = ctx.formParam("cpf");
            String email = ctx.formParam("email");
            LocalDate dataNascimento = LocalDate.parse(ctx.formParam("data_nascimento"));
            Participante participante = new Participante();
            participante.setNome(nome);
            participante.setCpf(cpf);
            participante.setEmail(email);
            participante.setDataNascimento(dataNascimento);
            var foto = ctx.uploadedFile("foto");
            // System.out.println(foto.filename());
            // System.out.println(foto.contentType());
            // System.out.println(foto.size());
            // System.out.println(foto.content().toString());
            participante.setFoto(foto.content().readAllBytes());
            // TODO: tamanho maximo
            if (foto.size() == 0 || foto.contentType().equals("image/jpeg")) {
                boolean resultado = new ParticipanteDAO().adicionar(participante);
                if (resultado) {
                    ctx.redirect("/");
                } else {
                    map.put("mensagem", "Email ou Cpf já existente!");
                    ctx.render("templates/erro.html", map);
                }
            } else {
                map.put("mensagem", "Imagem não é JPEG");
                ctx.render("templates/erro.html", map);
            }
        });

        app.post("/alterar_palestra", ctx -> {
            // TODO: estamos mexendo soh nos palestrantes...
            int id = Integer.parseInt(ctx.formParam("palestra_id"));
            String titulo = ctx.formParam("titulo");
            int duracao = Integer.parseInt(ctx.formParam("duracao"));
            String palavras_chave = ctx.formParam("palavras_chave");
            List<String> vetPalavraChave = Arrays.asList(palavras_chave.split(";"));

            Palestra palestra = new Palestra();
            palestra.setId(id);
            palestra.setTitulo(titulo);
            palestra.setDuracao(duracao);
            palestra.setVetPalavraChave(vetPalavraChave);

            List<String> vetPalestrantesSelecionados = ctx.formParams("palestrantes");
            if (vetPalestrantesSelecionados.size() > 0) {
                new PalestraDAO().alterar(palestra, vetPalestrantesSelecionados);
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
            ctx.render("/templates/participante/tela_alterar.html", map);
        });

           app.get("/inscricao/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            map.put("participante", new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id"))));
            map.put("vetEvento", new EventoDAO().listar());
            ctx.render("/templates/participante/inscricao.html", map);
        });

        app.post("/realizar_inscricao", ctx -> {
            int participante_id = Integer.parseInt(ctx.formParam("id"));
            int evento_id = Integer.parseInt(ctx.formParam("evento_id"));
            new ParticipanteDAO().realizarInscricao(participante_id, evento_id);
            ctx.redirect("/");
        });

        app.post("/alterar", ctx -> {
            int id = Integer.parseInt(ctx.formParam("id"));
            Participante participante = new ParticipanteDAO().obter(id);
            String nome = ctx.formParam("nome");
            String cpf = ctx.formParam("cpf");
            String email = ctx.formParam("email");
            LocalDate dataNascimento = LocalDate.parse(ctx.formParam("data_nascimento"));
            participante.setNome(nome);
            participante.setCpf(cpf);
            participante.setEmail(email);
            participante.setDataNascimento(dataNascimento);
            if (ctx.formParam("remover") != null) {
                int remover = Integer.parseInt(ctx.formParam("remover"));
                if (remover == 1) {
                    participante.setFoto(null);
                } else {
                    var foto = ctx.uploadedFile("foto");
                    if (foto != null) {
                        if (foto.contentType().equals("image/jpeg")) {
                            System.out.println("veio foto!");
                            participante.setFoto(foto.content().readAllBytes());
                        }
                    }
                }
            } else {
                var foto = ctx.uploadedFile("foto");
                if (foto != null) {
                    if (foto.contentType().equals("image/jpeg")) {
                        System.out.println("veio foto!");
                        participante.setFoto(foto.content().readAllBytes());
                    }
                }
            }
            boolean resultado = new ParticipanteDAO().alterar(participante);
            // System.out.println(resultado);
            ctx.redirect("/");
        });

    }
}
