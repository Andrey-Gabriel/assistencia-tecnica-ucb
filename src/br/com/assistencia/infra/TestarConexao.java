package br.com.assistencia.infra;

import java.sql.*;

/** Execute esta classe na IDE para conferir a conexão sem abrir as telas. */
public class TestarConexao {
  public static void main(String[] args) {
    ConnectionFactory conexao = new ConnectionFactory();
    System.out.println("Arquivo em uso: " + conexao.getArquivoConfiguracao());
    try (Connection c = conexao.abrir()) {
      System.out.println("Conectado ao MySQL " + c.getMetaData().getDatabaseProductVersion());
      System.out.println("Banco: " + c.getCatalog());
      try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM ordem_servico");
          ResultSet r = p.executeQuery()) {
        r.next();
        System.out.println("Ordens de serviço: " + r.getInt(1));
      }
    } catch (SQLException e) {
      System.err.println("Falha: " + e.getMessage());
      System.err.println("Código MySQL: " + e.getErrorCode() + "; SQLState: " + e.getSQLState());
      System.exit(1);
    }
  }
}
