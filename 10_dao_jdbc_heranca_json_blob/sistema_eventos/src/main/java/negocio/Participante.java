package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Participante {
    private int id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;
    private List<Evento> vetEvento;

    public Participante(){
        this.vetEvento = new ArrayList<Evento>();

    }
        
    public Participante(int id, String nome) {
        this();
        this.id = id;
        this.nome = nome;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public List<Evento> getVetEvento() {
        return vetEvento;
    }

    public void setVetEvento(List<Evento> vetEvento) {
        this.vetEvento = vetEvento;
    }

    
    


}
