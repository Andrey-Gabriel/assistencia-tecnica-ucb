package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.Entidade;
import java.sql.*;
import java.util.*;

public abstract class JdbcCrudDAO<T extends Entidade> implements CrudDAO<T> {
  protected final ConnectionFactory conexoes;
  private final String tabela, chave, campos;

  protected JdbcCrudDAO(ConnectionFactory c, String tabela, String chave, String campos) {
    this.conexoes = c;
    this.tabela = tabela;
    this.chave = chave;
    this.campos = campos;
  }

  protected String consulta() {
    return "SELECT * FROM " + tabela + " ORDER BY " + chave + " DESC";
  }

  protected abstract T mapear(ResultSet r) throws SQLException;

  protected abstract void preencher(PreparedStatement p, T e) throws SQLException;

  @Override
  public List<T> listar() throws SQLException {
    try (Connection c = conexoes.abrir();
        PreparedStatement p = c.prepareStatement(consulta());
        ResultSet r = p.executeQuery()) {
      List<T> lista = new ArrayList<>();
      while (r.next()) lista.add(mapear(r));
      return lista;
    }
  }

  @Override
  public void inserir(T e) throws SQLException {
    String marcadores = String.join(",", Collections.nCopies(campos.split(",").length, "?"));
    try (Connection c = conexoes.abrir();
        PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO " + tabela + " (" + campos + ") VALUES (" + marcadores + ")",
                Statement.RETURN_GENERATED_KEYS)) {
      preencher(p, e);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) e.setId(r.getInt(1));
      }
    }
  }

  @Override
  public void alterar(T e) throws SQLException {
    String[] colunas = campos.split(",");
    StringJoiner atribuicoes = new StringJoiner(",");
    for (String campo : colunas) atribuicoes.add(campo + "=?");
    try (Connection c = conexoes.abrir();
        PreparedStatement p =
            c.prepareStatement(
                "UPDATE " + tabela + " SET " + atribuicoes + " WHERE " + chave + "=?")) {
      preencher(p, e);
      p.setInt(colunas.length + 1, e.getId());
      if (p.executeUpdate() == 0)
        throw new SQLException("Registro não encontrado. Atualize a listagem.");
    }
  }

  @Override
  public void excluir(int id) throws SQLException {
    try (Connection c = conexoes.abrir();
        PreparedStatement p =
            c.prepareStatement("DELETE FROM " + tabela + " WHERE " + chave + "=?")) {
      p.setInt(1, id);
      if (p.executeUpdate() == 0)
        throw new SQLException("Registro já excluído. Atualize a listagem.");
    }
  }
}
