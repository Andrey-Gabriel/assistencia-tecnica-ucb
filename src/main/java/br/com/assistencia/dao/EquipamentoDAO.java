package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;

public class EquipamentoDAO extends JdbcCrudDAO<Equipamento> {
  public EquipamentoDAO(ConnectionFactory c) {
    super(
        c,
        "equipamento",
        "id_equipamento",
        "id_cliente,tipo,marca,modelo,numero_serie,observacoes");
  }

  @Override
  protected String consulta() {
    return "SELECT e.*, c.nome AS nome_cliente FROM equipamento e JOIN cliente c ON"
               + " c.id_cliente=e.id_cliente ORDER BY e.id_equipamento DESC";
  }

  @Override
  protected Equipamento mapear(ResultSet r) throws SQLException {
    Cliente cliente = new Cliente();
    cliente.setId(r.getInt("id_cliente"));
    cliente.setNome(r.getString("nome_cliente"));
    Equipamento e = new Equipamento();
    e.setId(r.getInt("id_equipamento"));
    e.setCliente(cliente);
    e.setTipo(r.getString("tipo"));
    e.setMarca(r.getString("marca"));
    e.setModelo(r.getString("modelo"));
    e.setNumeroSerie(r.getString("numero_serie"));
    e.setObservacoes(r.getString("observacoes"));
    return e;
  }

  @Override
  protected void preencher(PreparedStatement p, Equipamento e) throws SQLException {
    p.setInt(1, e.getCliente().getId());
    p.setString(2, e.getTipo());
    p.setString(3, e.getMarca());
    p.setString(4, e.getModelo());
    p.setString(5, e.getNumeroSerie());
    p.setString(6, e.getObservacoes());
  }
}
