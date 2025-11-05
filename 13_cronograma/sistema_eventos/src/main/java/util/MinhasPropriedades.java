package util;

import java.io.IOException;
import java.util.Properties;

import persistencia.ConexaoPostgreSQL;

public class MinhasPropriedades {
    private Properties prop;

    public MinhasPropriedades(){
        prop = new Properties();
        try {
            prop.load(ConexaoPostgreSQL.class.getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    public Properties getPropertyObject() {
        return prop;
    }

}
