package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import util.MinhasPropriedades;

public class ConexaoPostgreSQL {
    private String host;
    private String port;
    private String dbname;
    private String username;
    private String password;

    public ConexaoPostgreSQL() {
        Properties prop = new MinhasPropriedades().getPropertyObject();
        this.dbname = prop.getProperty("dbname");
        this.username = prop.getProperty("username");
        this.password = prop.getProperty("password");
        this.port = prop.getProperty("port");
        this.host = prop.getProperty("host");
    }

    public Connection getConnection() {
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new IllegalArgumentException("Deu xabum na conexão");
        }
    }
}
