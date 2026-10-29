package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.Servico;
import java.sql.*;

public class ServicoDAO extends JdbcCrudDAO<Servico> {
  public ServicoDAO(ConnectionFactory c) {
    super(c, "servico", "id_servico", "nome,descricao,valor_referencia,prazo_estimado_dias,ativo");
  }

  @Override
  protected Servico mapear(ResultSet r) throws SQLException {
    Servico e = new Servico();
    e.setId(r.getInt("id_servico"));
    e.setNome(r.getString("nome"));
    e.setDescricao(r.getString("descricao"));
    e.setValorReferencia(r.getBigDecimal("valor_referencia"));
    e.setPrazoEstimadoDias(r.getInt("prazo_estimado_dias"));
    e.setAtivo(r.getBoolean("ativo"));
    return e;
  }

  @Override
  protected void preencher(PreparedStatement p, Servico e) throws SQLException {
    p.setString(1, e.getNome());
    p.setString(2, e.getDescricao());
    p.setBigDecimal(3, e.getValorReferencia());
    p.setInt(4, e.getPrazoEstimadoDias());
    p.setBoolean(5, e.getAtivo());
  }
}
