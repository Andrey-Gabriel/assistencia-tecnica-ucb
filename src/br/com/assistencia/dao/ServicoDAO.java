package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO extends DAO implements CrudDAO<Servico> {
  public ServicoDAO(ConnectionFactory conexao) {
    super(conexao);
  }

  @Override
  public void inserir(Servico e) throws SQLException {
    String sql =
        "INSERT INTO servico (nome,descricao,valor_referencia,prazo_estimado_dias,ativo) VALUES"
            + " (?,?,?,?,?)";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getDescricao());
      pst.setBigDecimal(3, e.getValorReferencia());
      pst.setInt(4, e.getPrazoEstimadoDias());
      pst.setBoolean(5, e.getAtivo());
      pst.executeUpdate();
      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) e.setId(rs.getInt(1));
      }
    }
  }

  @Override
  public List<Servico> listar() throws SQLException {
    String sql = "SELECT * FROM servico ORDER BY id_servico DESC";
    List<Servico> registros = new ArrayList<>();
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) registros.add(mapear(rs));
    }
    return registros;
  }

  @Override
  public void alterar(Servico e) throws SQLException {
    String sql =
        "UPDATE servico SET nome=?,descricao=?,valor_referencia=?,prazo_estimado_dias=?,ativo=?"
            + " WHERE id_servico=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getDescricao());
      pst.setBigDecimal(3, e.getValorReferencia());
      pst.setInt(4, e.getPrazoEstimadoDias());
      pst.setBoolean(5, e.getAtivo());
      pst.setInt(6, e.getId());
      if (pst.executeUpdate() == 0)
        throw new SQLException("Registro não encontrado. Atualize a listagem.");
    }
  }

  @Override
  public void excluir(int id) throws SQLException {
    String sql = "DELETE FROM servico WHERE id_servico=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, id);
      if (pst.executeUpdate() == 0) throw new SQLException("Registro não encontrado.");
    }
  }

  private Servico mapear(ResultSet rs) throws SQLException {
    Servico e = new Servico();
    e.setId(rs.getInt("id_servico"));
    e.setNome(rs.getString("nome"));
    e.setDescricao(rs.getString("descricao"));
    e.setValorReferencia(rs.getBigDecimal("valor_referencia"));
    e.setPrazoEstimadoDias(rs.getInt("prazo_estimado_dias"));
    e.setAtivo(rs.getBoolean("ativo"));
    return e;
  }
}
