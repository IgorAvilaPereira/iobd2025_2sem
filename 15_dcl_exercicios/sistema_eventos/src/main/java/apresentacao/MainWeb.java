package apresentacao;

import io.javalin.Javalin;
import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinMustache;
import jakarta.servlet.http.HttpSession;
import negocio.Evento;
import negocio.Palestra;
import negocio.Participante;
import persistencia.EventoDAO;
import persistencia.PalestraDAO;
import persistencia.PalestranteDAO;
import persistencia.ParticipanteDAO;
import util.MinhasPropriedades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

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
            config.jetty.multipartConfig.maxInMemoryFileSize(10, SizeUnit.MB);
            config.jetty.multipartConfig.maxTotalRequestSize(1, SizeUnit.GB);
        }).start(Integer.parseInt(prop.getProperty("javalin_port")));

        // app.before("/*",ctx -> {
        // System.out.println("Received request for path: " + ctx.path());
        // if (urlPrivadas(ctx.path())) {
        // System.out.println("privada");
        // ctx.html("oi").status(400);
        // // Map<String, String> map = new HashMap<>();
        // // map.put("mensagem", "Url privada");
        // // ctx.render("templates/erro.html", map);
        // // ctx.html("erro");

        // } else {
        // System.out.println("publica");
        // }
        // });

        // com js
        app.post("/buscar_participante", ctx -> {
            if (isLogado(ctx)) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> map = objectMapper.readValue(ctx.body(), Map.class);
                String nome = map.get("nome");
                if (!nome.isEmpty() && !nome.isBlank()) {
                    List<Participante> vetParticipante = new ParticipanteDAO().listar(nome);
                    ctx.json(vetParticipante);
                } else {
                    ctx.json(new ArrayList<>());
                }
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/tela_buscar_participante", ctx -> {
            if (isLogado(ctx)) {

                ctx.render("/templates/participante/tela_buscar_participante.html");
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/tela_adicionar", ctx -> {
            if (isLogado(ctx)) {

                ctx.render("/templates/participante/tela_adicionar.html");
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.post("/adicionar_palestra", ctx -> {
            if (isLogado(ctx)) {

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
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/palestra/nova", ctx -> {
            if (isLogado(ctx)) {
                List<Evento> vetEvento = new EventoDAO().listar();
                Map<String, Object> map = new HashMap<>();
                map.put("vetEvento", vetEvento);
                ctx.render("/templates/palestra/tela_adicionar.html", map);
            } else {
                ctx.render("templates/tela_login.html");
            }

        });

        app.get("/eventos", ctx -> {
            if (isLogado(ctx)) {

                List<Evento> vetEvento = new EventoDAO().listar();
                Map<String, Object> model = new HashMap<>();
                model.put("vetEvento", vetEvento);
                ctx.render("/templates/evento.html", model);
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/baixar_material/{id}", ctx -> {
            if (isLogado(ctx)) {
                Palestra palestra = new PalestraDAO().obter(Integer.parseInt(ctx.pathParam("id")));
                if (palestra.getMaterialTipo() != null) {
                    if (!palestra.getMaterialTipo().contains("zip")) {
                        ctx.html("<embed src=\"data:" + palestra.getMaterialTipo() + ";base64,"
                                + encodeImageToBase64(palestra.getMaterial()) + "\">");
                    } else {
                        Map<String, Object> model = new HashMap<>();
                        model.put("palestra", palestra);
                        ctx.render("/templates/palestra/baixar.html", model);
                    }
                } else {
                    ctx.html("Sem material");
                }
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/palestras", ctx -> {
            if (isLogado(ctx)) {
                List<Palestra> vetPalestra = new PalestraDAO().listar();
                Map<String, Object> model = new HashMap<>();
                model.put("vetPalestra", vetPalestra);
                ctx.render("/templates/palestra/index.html", model);
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/", ctx -> {
            if (isLogado(ctx)) {

                ctx.redirect("/0");
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/participante/{cpf}", ctx -> {
            if (isLogado(ctx)) {

                Participante participante = new ParticipanteDAO().obterPorCpf(ctx.pathParam("cpf"));
                Map<String, Object> map = new HashMap<>();
                map.put("participante", participante);
                ctx.render("/templates/participante/participante.html", map);
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/{pagina}", ctx -> {
            if (isLogado(ctx)) {
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
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/excluir_palestra/{id}", ctx -> {
            if (isLogado(ctx)) {

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

            } else {
                ctx.render("templates/tela_login.html");
            }

        });

        app.get("/excluir_participante/{id}", ctx -> {
            if (isLogado(ctx)) {

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
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.get("/visualizar/{id}", ctx -> {
            if (isLogado(ctx)) {

                Map<String, Object> map = new HashMap<>();
                Participante participante = new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id")));
                map.put("participante", participante);
                map.put("vetInscricao", new ParticipanteDAO().minhasInscricoes(Integer.parseInt(ctx.pathParam("id"))));
                ctx.render("templates/participante/visualizar.html", map);
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.post("/adicionar", ctx -> {
            if (isLogado(ctx)) {

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
            } else {
                ctx.render("templates/tela_login.html");
            }
        });

        app.post("/login", ctx -> {
            Map<String, Object> map = new HashMap<>();
            if (!ctx.formParam("email").isBlank() && !ctx.formParam("senha").isBlank()) {
                String email = ctx.formParam("email").trim();
                String senha = ctx.formParam("senha").trim();
                boolean resultado = false;
                String tipo = ctx.formParam("tipo");
                if (tipo.equals("participante")) {
                    resultado = new ParticipanteDAO().realizarLogin(email, senha);
                } else {
                    resultado = new PalestranteDAO().realizarLogin(email, senha);
                }
                if (resultado == true) {
                    ctx.sessionAttribute("email", email);
                    ctx.sessionAttribute("tipo", tipo);
                    ctx.redirect("/0");
                } else {
                    map.put("mensagem", "Deu Xabum no teu login! Verifica teu email e tua senha, bro!");
                    ctx.render("templates/tela_login.html", map);
                }
            } else {
                map.put("mensagem", "senha e email são obrigatórios!");
                ctx.render("templates/tela_login.html", map);
            }
        });

        app.post("/alterar_palestra", ctx -> {
            if (isLogado(ctx)) {
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
            } else {
                ctx.render("/templates/tela_login.html");
            }
        });

        app.get("/tela_alterar_palestra/{id}", ctx -> {
            if (isLogado(ctx)) {
                Map<String, Object> map = new HashMap<>();
                Palestra palestra = new PalestraDAO().obter(Integer.parseInt(ctx.pathParam("id")));
                map.put("palestra", palestra);
                map.put("vetPalestrante", new PalestranteDAO().listar(Integer.parseInt(ctx.pathParam("id"))));
                ctx.render("/templates/palestra/tela_alterar.html", map);
            } else {
                ctx.render("/templates/tela_login.html");
            }
        });

        app.get("/tela_alterar/{id}", ctx -> {
            if (isLogado(ctx)) {
                Map<String, Object> map = new HashMap<>();
                map.put("participante", new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id"))));
                ctx.render("/templates/participante/tela_alterar.html", map);
            } else {
                ctx.render("/templates/tela_login.html");
            }
        });

        app.get("/inscricao/{id}", ctx -> {
            Map<String, Object> map = new HashMap<>();
            if (isLogado(ctx)) {
                List<Evento> vetEvento = new EventoDAO()
                        .listarEventosDisponiveis(Integer.parseInt(ctx.pathParam("id")));
                map.put("participante", new ParticipanteDAO().obter(Integer.parseInt(ctx.pathParam("id"))));
                map.put("vetEvento", vetEvento);
                if (vetEvento.size() > 0) {
                    map.put("tem_vet_evento", "true");
                }
                ctx.render("/templates/participante/inscricao.html", map);
            } else {
                ctx.render("/templates/tela_login.html");
            }
        });

        app.post("/realizar_inscricao", ctx -> {
            if (isLogado(ctx)) {
                int participante_id = Integer.parseInt(ctx.formParam("id"));
                int evento_id = Integer.parseInt(ctx.formParam("evento_id"));
                new ParticipanteDAO().realizarInscricao(participante_id, evento_id);
                ctx.redirect("/");
            } else {
                ctx.render("/templates/tela_login.html");
            }
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
            new ParticipanteDAO().alterar(participante);
            ctx.redirect("/");
        });

    }

    private static boolean isLogado(Context ctx) {
        HttpSession session = ctx.req().getSession();
        // System.out.println(ctx.path()+"oi"+new Random().nextDouble()+":"+session.getAttribute("email"));
        if (session.getAttribute("email") != null && !ctx.path().equals("/logout"))
            return true;
        // logout
        session.invalidate();
        return false;
    }
}
