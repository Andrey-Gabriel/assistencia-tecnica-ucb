package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoDAO extends DAO implements CrudDAO<Equipamento> {
  public EquipamentoDAO(ConnectionFactory conexao) {
    super(conexao);
  }

  @Override
  public void inserir(Equipamento e) throws SQLException {
    String sql =
        "INSERT INTO equipamento (id_cliente,tipo,marca,modelo,numero_serie,observacoes) VALUES"
            + " (?,?,?,?,?,?)";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pst.setInt(1, e.getCliente().getId());
      pst.setString(2, e.getTipo());
      pst.setString(3, e.getMarca());
      pst.setString(4, e.getModelo());
      pst.setString(5, e.getNumeroSerie());
      pst.setString(6, e.getObservacoes());
      pst.executeUpdate();
      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) e.setId(rs.getInt(1));
      }
    }
  }

  @Override
  public List<Equipamento> listar() throws SQLException {
    String sql =
        "SELECT e.*,c.nome AS nome_cliente FROM equipamento e JOIN cliente c ON"
            + " c.id_cliente=e.id_cliente ORDER BY e.id_equipamento DESC";
    List<Equipamento> registros = new ArrayList<>();
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) registros.add(mapear(rs));
    }
    return registros;
  }

  @Override
  public void alterar(Equipamento e) throws SQLException {
    String sql =
        "UPDATE equipamento SET id_cliente=?,tipo=?,marca=?,modelo=?,numero_serie=?,observacoes=?"
            + " WHERE id_equipamento=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, e.getCliente().getId());
      pst.setString(2, e.getTipo());
      pst.setString(3, e.getMarca());
      pst.setString(4, e.getModelo());
      pst.setString(5, e.getNumeroSerie());
      pst.setString(6, e.getObservacoes());
      pst.setInt(7, e.getId());
      if (pst.executeUpdate() == 0)
        throw new SQLException("Registro não encontrado. Atualize a listagem.");
    }
  }

  @Override
  public void excluir(int id) throws SQLException {
    String sql = "DELETE FROM equipamento WHERE id_equipamento=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, id);
      if (pst.executeUpdate() == 0) throw new SQLException("Registro não encontrado.");
    }
  }

  private Equipamento mapear(ResultSet rs) throws SQLException {
    Equipamento e = new Equipamento();
    e.setId(rs.getInt("id_equipamento"));
    Cliente cliente = new Cliente();
    cliente.setId(rs.getInt("id_cliente"));
    cliente.setNome(rs.getString("nome_cliente"));
    e.setCliente(cliente);
    e.setTipo(rs.getString("tipo"));
    e.setMarca(rs.getString("marca"));
    e.setModelo(rs.getString("modelo"));
    e.setNumeroSerie(rs.getString("numero_serie"));
    e.setObservacoes(rs.getString("observacoes"));
    return e;
  }
}
