# 📚 Aula – Armazenamento de Arquivos no Banco de Dados com PostgreSQL + JDBC (Java)

## 1. Conceitos Fundamentais

O PostgreSQL permite armazenar arquivos binários (imagens, PDFs, etc.) diretamente no banco de dados.  
Existem duas principais abordagens:

1. **BYTEA** → para arquivos pequenos/médios.  
2. **OID (Large Objects)** → para arquivos grandes.

---

## 2. BYTEA – Arquivos em formato binário

Exemplo de tabela para armazenamento de banners com arquivo binário:

```sql
CREATE TABLE publicidade.banner (
    id SERIAL PRIMARY KEY,
    arquivo bytea,
    legenda text,
    altura integer,
    largura integer,
    link text,
    tipo text CHECK (tipo = 'SUPERIOR' OR tipo = 'INFERIOR') DEFAULT 'SUPERIOR',
    qtde_cliques INTEGER DEFAULT 0
);
```

Inserção de arquivo:

```sql
INSERT INTO publicidade.banner (arquivo, legenda, link, tipo)
VALUES (pg_read_binary_file('/tmp/globo.png'), 'Clique Aqui', 'http://www.g1.com','SUPERIOR');
```

---

## 3. Integração com Java (JDBC)

Utiliza classes modelo (**Banner**) e **DAO** para persistir e recuperar arquivos binários.  
O arquivo é convertido em `byte[]` e enviado ao banco via `PreparedStatement`.

1. `Banner`: armazena dados do banner e do arquivo (`byte[]`).  
2. `BannerDAO`: métodos para inserir, listar e obter banners.  
3. `Main`: insere banner com imagem e exibe imagem recuperada do banco.

### 🧾 Exemplo de código Java

#### Classe `Banner`:
```java
public class Banner {
    private int id;  
    private String legenda;
    private int largura;
    private int altura;
    private String link;
    private String tipo;
    private int qtdeCliques;
    private byte[] arquivo;

    // getters e setters...

    // obs: o javalin tem sua classe file (upload)
    public void setArquivo(String diretorio) throws FileNotFoundException, IOException {        
        File f = new File(diretorio);
        FileInputStream fileInputStream = new FileInputStream(f);
        this.arquivo = fileInputStream.readAllBytes();
    }
}
```

#### Classe `BannerDAO`:
```java
public class BannerDAO {
    private ConexaoPostgreSQL conexaoPostgreSQL;

    public void adicionar(Banner banner, String dir) throws SQLException, FileNotFoundException {
        this.conexaoPostgreSQL = new ConexaoPostgreSQL();
        Connection conn = this.conexaoPostgreSQL.getConexao();
        String sql = "INSERT INTO publicidade.banner (arquivo, legenda, link, tipo) VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        banner.setArquivo(dir);
        preparedStatement.setBytes(1, banner.getArquivo());
        preparedStatement.setString(2, banner.getLegenda());
        preparedStatement.setString(3, banner.getLink());
        preparedStatement.setString(4, banner.getTipo());
        preparedStatement.executeUpdate();
        conn.close();
    }

    public Banner obter(int id) throws SQLException {
        Banner b = new Banner();
        this.conexaoPostgreSQL = new ConexaoPostgreSQL();
        Connection conn = this.conexaoPostgreSQL.getConexao();
        String sql = "SELECT * FROM publicidade.banner WHERE id = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            b.setId(rs.getInt("id"));
            b.setAltura(rs.getInt("altura"));
            b.setLargura(rs.getInt("largura"));
            b.setLegenda(rs.getString("legenda"));
            b.setLink(rs.getString("link"));
            b.setQtdeCliques(rs.getInt("qtde_cliques"));
            b.setTipo(rs.getString("tipo"));
            b.setArquivo(rs.getBytes("arquivo"));
        }
        conn.close();
        return b;
    }
}
```

#### Classe `Main`:
```java
public class Main {
    public static void main(String[] args) throws SQLException, FileNotFoundException {
        // escrita
        Banner bannerVetorial = new Banner();
        bannerVetorial.setLink("http://vetorial.net");
        bannerVetorial.setLegenda("clique aqui e contrate sua banda larga");
        bannerVetorial.setTipo("SUPERIOR");
        new BannerDAO().adicionar(bannerVetorial, "/home/iapereira/vetorial.png");

        // leitura para aplicacao desktop
        BannerDAO bannerDAO = new BannerDAO();
        Banner bannerGlobo = bannerDAO.obter(1); // exemplo com id=1
        ImageIcon imageIcon = new ImageIcon(bannerGlobo.getArquivo());
        JFrame jFrame = new JFrame();
        jFrame.setLayout(new FlowLayout());
        jFrame.setSize(500, 500);
        JLabel jLabel = new JLabel();
        jLabel.setIcon(imageIcon);
        jFrame.add(jLabel);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
```

---

## 4. OID (Large Objects)

Para arquivos grandes, utiliza-se OID e funções nativas do PostgreSQL:

```sql
CREATE TABLE largeObjects_Devmedia (
    cod_imagem INTEGER PRIMARY KEY,
    nome_imagem VARCHAR(30),
    local_imagem oid
);
```

### Inserção
```sql
INSERT INTO largeObjects_Devmedia (cod_imagem, nome_imagem, local_imagem)
VALUES (1, 'naruto_shippuden', lo_import('D:/imagens/naruto_shippuden.jpg'));
```

### Exportação
```sql
SELECT lo_export(32784, 'D:/imagens/naruto_shippuden.jpg');
```

### Remoção
```sql
SELECT lo_unlink(32784);
```

---

## 5. Links Complementares

1. [PostgreSQL Binary Data](https://www.postgresql.org/docs/7.4/jdbc-binary-data.html)  
2. [Binary Data Performance](https://www.cybertec-postgresql.com/en/binary-data-performance-in-postgresql/)  
3. [PostgreSQL BR Blog](http://postgresqlbr.blogspot.com/2013/04/trate-com-blobs-e-clobs-diretamente-no.html)  
4. [DevMedia Large Objects](https://www.devmedia.com.br/trabalhando-com-large-objects-no-postgresql/34167)
