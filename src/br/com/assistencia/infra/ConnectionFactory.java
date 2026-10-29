package br.com.assistencia.infra;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Conexão JDBC, sem framework. As credenciais ficam em banco.local.properties, fora do Git. */
public class ConnectionFactory {
  private final Properties props = new Properties();
  private final Path arquivo;

  public ConnectionFactory() {
    arquivo = localizarArquivo();
    recarregar();
  }

  private Path localizarArquivo() {
    String explicito = System.getProperty("assistencia.config");
    if (explicito != null && !explicito.isBlank())
      return Path.of(explicito).toAbsolutePath().normalize();
    try {
      Path origem =
          Path.of(
              ConnectionFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      if (Files.isRegularFile(origem)) return origem.getParent().resolve("banco.local.properties");
      // Na IDE, procurar a configuração junto ao projeto, e não em outra pasta de execução.
      Path pasta = origem;
      for (int i = 0; pasta != null && i < 7; i++, pasta = pasta.getParent()) {
        Path candidato = pasta.resolve("banco.local.properties");
        if (Files.isRegularFile(candidato)
            || Files.isRegularFile(pasta.resolve("banco.exemplo.properties"))
            || Files.isRegularFile(pasta.resolve("assistencia-tecnica.iml"))) {
          return candidato.toAbsolutePath().normalize();
        }
      }
    } catch (URISyntaxException | IllegalArgumentException e) {
      throw new IllegalStateException("Não foi possível localizar o projeto.", e);
    }
    return Path.of(System.getProperty("user.dir"), "banco.local.properties")
        .toAbsolutePath()
        .normalize();
  }

  public synchronized void recarregar() {
    Properties dados = new Properties();
    dados.setProperty(
        "db.url",
        "jdbc:mysql://localhost:3306/assistencia_tecnica_completa?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000");
    dados.setProperty("db.user", "root");
    dados.setProperty("db.password", "");
    if (Files.isRegularFile(arquivo)) {
      try (Reader r = Files.newBufferedReader(arquivo)) {
        dados.load(r);
      } catch (IOException e) {
        throw new IllegalStateException("Não foi possível ler " + arquivo, e);
      }
    }
    props.clear();
    props.putAll(dados);
  }

  public Path getArquivoConfiguracao() {
    return arquivo;
  }

  public synchronized String get(String chave) {
    return props.getProperty(chave, "");
  }

  /** Método equivalente a abrirBanco do exemplo dado em sala. */
  public Connection abrir() throws SQLException {
    String url, usuario, senha;
    synchronized (this) {
      recarregar();
      url = get("db.url");
      usuario = get("db.user");
      senha = get("db.password");
    }
    try {
      return conectar(url, usuario, senha);
    } catch (SQLException e) {
      if (e.getErrorCode() == 1045) {
        throw new SQLException(
            "O MySQL recusou as credenciais do arquivo:\n"
                + arquivo
                + "\nPreencha a senha em Conexão com o banco e clique em Testar e salvar."
                + "\nAlterar uma senha no código não modifica esse arquivo.",
            e.getSQLState(),
            e.getErrorCode(),
            e);
      }
      throw e;
    }
  }

  public Connection conectar(String url, String usuario, String senha) throws SQLException {
    if (url == null || !url.startsWith("jdbc:mysql://") || usuario == null || usuario.isBlank())
      throw new IllegalArgumentException("Informe uma URL JDBC MySQL e o usuário.");
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new SQLException(
          "Driver MySQL ausente. Adicione lib/mysql-connector-j-8.4.0.jar às bibliotecas do"
              + " projeto.",
          "IM003",
          e);
    }
    return DriverManager.getConnection(url.trim(), usuario.trim(), senha == null ? "" : senha);
  }

  /** Testa os valores digitados antes de gravar. Não há uma segunda senha no código. */
  public void configurar(String url, String usuario, String senha)
      throws IOException, SQLException {
    try (Connection c = conectar(url, usuario, senha)) {
      try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM ordem_servico");
          ResultSet r = p.executeQuery()) {
        r.next();
      }
    }
    Properties nova = new Properties();
    nova.setProperty("db.url", url.trim());
    nova.setProperty("db.user", usuario.trim());
    nova.setProperty("db.password", senha == null ? "" : senha);
    synchronized (this) {
      try (Writer w = Files.newBufferedWriter(arquivo)) {
        nova.store(w, "Credenciais locais: editar somente aqui ou na tela Conexao");
      }
      props.clear();
      props.putAll(nova);
    }
  }
}
