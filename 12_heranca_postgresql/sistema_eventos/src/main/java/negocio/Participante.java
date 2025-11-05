package negocio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apresentacao.MainWeb;
import persistencia.ConexaoPostgreSQL;

public class Participante {
    private int id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;
    private List<Evento> vetEvento;
    private byte[] foto;

    public Participante() {
        this.vetEvento = new ArrayList<Evento>();

    }

    public Participante(int id, String nome) {
        this();
        this.id = id;
        this.nome = nome;
    }

    public Participante(int id, String nome, String cpf) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
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

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String dataNascimentoFormatada() {
        if (this.dataNascimento != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dataNascimento.format(formatter);
        }
        return null;
    }

    //   public String dataNascimentoFormatadaInput() {
    //     if (this.dataNascimento != null) {
    //         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyydd/MM/");
    //         return dataNascimento.format(formatter);
    //     }
    //     return null;
    // }



    public String fotoEncode() {
        if (foto != null)
            return MainWeb.encodeImageToBase64(foto);
        return null;
    }

    @Override
    public String toString() {
        return "Participante [id=" + id + ", nome=" + nome + ", email=" + email + ", cpf=" + cpf + ", dataNascimento="
                + dataNascimento + "]";
    }

    
}
