package negocio;

import java.sql.Date;

public class Evento {

    private String nome;
    private Date dataInicio;
    private Date dataFim;
    private String status;

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDataInicio(Date date) {
        this.dataInicio = date;
    }

    public void setDataFim(Date date) {
        this.dataFim = date;
    }

    public void setStatus(String string) {
        this.status = string;
    }

    public String getNome() {
        return nome;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public String getStatus() {
        return status;
    }
    

    
}
