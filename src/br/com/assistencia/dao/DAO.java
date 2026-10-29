package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

/** Base de conexão JDBC, como a classe DAO do exemplo do professor. */
public abstract class DAO {
  private final ConnectionFactory conexoes;

  protected DAO(ConnectionFactory conexoes) {
    this.conexoes = conexoes;
  }

  protected Connection abrirBanco() throws SQLException {
    return conexoes.abrir();
  }
}
