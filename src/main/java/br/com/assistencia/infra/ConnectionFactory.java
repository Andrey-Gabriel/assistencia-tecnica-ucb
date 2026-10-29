package br.com.assistencia.infra;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public class ConnectionFactory {
  private final Properties props = new Properties();
  private final Path arquivo;

  public ConnectionFactory() {
    try {
      Path origem =
          Path.of(
              ConnectionFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      Path pasta =
          Files.isRegularFile(origem)
              ? origem.getParent()
              : Path.of(System.getProperty("user.dir"));
      arquivo =
          Path.of(
              System.getProperty(
                  "assistencia.config", pasta.resolve("banco.properties").toString()));
      props.setProperty(
          "db.url",
          "jdbc:mysql://localhost:3306/assistencia_tecnica_completa?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000");
      props.setProperty("db.user", "root");
      props.setProperty("db.password", "");
      if (Files.exists(arquivo))
        try (Reader r = Files.newBufferedReader(arquivo)) {
          props.load(r);
        }
    } catch (Exception e) {
      throw new IllegalStateException("Não foi possível ler banco.properties.", e);
    }
  }

  public synchronized String get(String chave) {
    return props.getProperty(chave, "");
  }

  public Connection abrir() throws SQLException {
    String url, user, pass;
    synchronized (this) {
      url = get("db.url");
      user = get("db.user");
      pass = get("db.password");
    }
    return DriverManager.getConnection(url, user, pass);
  }

  public synchronized void configurar(String url, String usuario, String senha) throws IOException {
    if (url == null || !url.startsWith("jdbc:mysql://") || usuario == null || usuario.isBlank())
      throw new IllegalArgumentException("Informe uma URL JDBC MySQL e o usuário.");
    Properties nova = new Properties();
    nova.setProperty("db.url", url.trim());
    nova.setProperty("db.user", usuario.trim());
    nova.setProperty("db.password", senha);
    try (Writer w = Files.newBufferedWriter(arquivo)) {
      nova.store(w, "Conexao MySQL da Assistencia Tecnica");
    }
    props.clear();
    props.putAll(nova);
  }
}
