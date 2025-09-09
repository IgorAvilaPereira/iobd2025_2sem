package apresentacao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinMustache;
import negocio.Nota;
import persistencia.NotaDAO;

public class Main {
    public static void main(String[] args) {

          var app = Javalin.create(config -> {
                config.fileRenderer(new JavalinMustache());
                config.staticFiles.add("/static", Location.CLASSPATH);
            }).start(7070);

            app.get("/", ctx -> ctx.result("Hello World"));

            app.get("/teste", ctx -> {
                Map<String, Object> model = new HashMap<>();
                model.put("x", "oi");               
                model.put("vetNota", new NotaDAO().listar());
                ctx.render("/templates/index.html", model);
            });

    }
}