package negocio;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class Evento {
    private int id;
    private String nome;
    private Date dataInicio;
    private Date dataFim;
    private String status;
    private String local;

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

    private String formatarData(Date data) {
        if (data != null) {
            // Define a date format pattern
            String pattern = "dd/MM/yyyy";

            // Create a SimpleDateFormat object with the specified pattern
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);

            // Format the date into a string
            return formatter.format(data);
        } else {
            return "";
        }
    }

    public String dataInicioFormatada() {
        return this.formatarData(this.dataInicio);

    }

    public String dataFimFormatada() {

        return this.formatarData(this.dataFim);
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getLocal() {
        return local;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    

}
