package negocio;

import java.util.ArrayList;
import java.util.List;

import apresentacao.MainWeb;

public class Palestra {
    private int id;
    private String titulo;
    private int duracao;
    private Evento evento;
    private byte[] material;
    private String materialTipo;
    private List<String> vetPalavraChave;

    public Palestra () {
        this.vetPalavraChave = new ArrayList<>();
    }

    public List<String> getVetPalavraChave() {
        return vetPalavraChave;
    }

    public void setVetPalavraChave(List<String> vetPalavraChave) {
        this.vetPalavraChave = vetPalavraChave;
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

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public byte[] getMaterial() {
        return material;
    }

    public void setMaterial(byte[] material) {
        this.material = material;
    }

    public void setMaterialTipo(String materialTipo) {
        this.materialTipo = materialTipo;
    }

    public String getMaterialTipo() {
        return materialTipo;
    }

    public String materialEncode() {
        if (material != null)
            return MainWeb.encodeImageToBase64(material);
        return null;
    }

    public String vetPalavraChaveFormatada(){
        return String.join(";",this.vetPalavraChave);
    }
}
