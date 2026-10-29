package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.Tecnico;
import java.sql.*;

public class TecnicoDAO extends JdbcCrudDAO<Tecnico> {
  public TecnicoDAO(ConnectionFactory c) {
    super(c, "tecnico", "id_tecnico", "nome,telefone,email,especialidade,ativo");
  }

  @Override
  protected Tecnico mapear(ResultSet r) throws SQLException {
    Tecnico e = new Tecnico();
    e.setId(r.getInt("id_tecnico"));
    e.setNome(r.getString("nome"));
    e.setTelefone(r.getString("telefone"));
    e.setEmail(r.getString("email"));
    e.setEspecialidade(r.getString("especialidade"));
    e.setAtivo(r.getBoolean("ativo"));
    return e;
  }

  @Override
  protected void preencher(PreparedStatement p, Tecnico e) throws SQLException {
    p.setString(1, e.getNome());
    p.setString(2, e.getTelefone());
    p.setString(3, e.getEmail());
    p.setString(4, e.getEspecialidade());
    p.setBoolean(5, e.getAtivo());
  }
}
