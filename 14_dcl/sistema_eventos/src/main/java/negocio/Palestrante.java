package negocio;

import java.util.List;


import apresentacao.MainWeb;

public class Palestrante {
    private int id;
    private String nome;
    private String biografia;
    private String cpf;
    private List<Palestra> vetPalestra;
    private boolean ehPalestrante;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getBiografia() {
        return biografia;
    }
    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }
    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    public void ehPalestrante(boolean b) {
        this.ehPalestrante = b;
    }


    public boolean isEhPalestrante() {
        return ehPalestrante;
    }
  


    

    

    

}
