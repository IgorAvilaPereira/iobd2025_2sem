package negocio;

public class Nota {
    private int id;
    private String titulo;
    private String texto;

    

    public Nota(String titulo, String texto) {
        this.titulo = titulo;
        this.texto = texto;
    }
    public Nota() {
        //TODO Auto-generated constructor stub
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getTexto() {
        return texto;
    }
    public void setTexto(String texto) {
        this.texto = texto;
    }

    

}
