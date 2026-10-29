package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.Cliente;
import java.sql.*;

public class ClienteDAO extends JdbcCrudDAO<Cliente> {
  public ClienteDAO(ConnectionFactory c) {
    super(c, "cliente", "id_cliente", "nome,documento,telefone,email,endereco");
  }

  @Override
  protected Cliente mapear(ResultSet r) throws SQLException {
    Cliente e = new Cliente();
    e.setId(r.getInt("id_cliente"));
    e.setNome(r.getString("nome"));
    e.setDocumento(r.getString("documento"));
    e.setTelefone(r.getString("telefone"));
    e.setEmail(r.getString("email"));
    e.setEndereco(r.getString("endereco"));
    return e;
  }

  @Override
  protected void preencher(PreparedStatement p, Cliente e) throws SQLException {
    p.setString(1, e.getNome());
    p.setString(2, e.getDocumento());
    p.setString(3, e.getTelefone());
    p.setString(4, e.getEmail());
    p.setString(5, e.getEndereco());
  }
}
