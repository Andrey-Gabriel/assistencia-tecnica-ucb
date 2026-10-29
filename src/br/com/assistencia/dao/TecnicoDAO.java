package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TecnicoDAO extends DAO implements CrudDAO<Tecnico> {
  public TecnicoDAO(ConnectionFactory conexao) {
    super(conexao);
  }

  @Override
  public void inserir(Tecnico e) throws SQLException {
    String sql = "INSERT INTO tecnico (nome,telefone,email,especialidade,ativo) VALUES (?,?,?,?,?)";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getTelefone());
      pst.setString(3, e.getEmail());
      pst.setString(4, e.getEspecialidade());
      pst.setBoolean(5, e.getAtivo());
      pst.executeUpdate();
      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) e.setId(rs.getInt(1));
      }
    }
  }

  @Override
  public List<Tecnico> listar() throws SQLException {
    String sql = "SELECT * FROM tecnico ORDER BY id_tecnico DESC";
    List<Tecnico> registros = new ArrayList<>();
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) registros.add(mapear(rs));
    }
    return registros;
  }

  @Override
  public void alterar(Tecnico e) throws SQLException {
    String sql =
        "UPDATE tecnico SET nome=?,telefone=?,email=?,especialidade=?,ativo=? WHERE id_tecnico=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getTelefone());
      pst.setString(3, e.getEmail());
      pst.setString(4, e.getEspecialidade());
      pst.setBoolean(5, e.getAtivo());
      pst.setInt(6, e.getId());
      if (pst.executeUpdate() == 0)
        throw new SQLException("Registro não encontrado. Atualize a listagem.");
    }
  }

  @Override
  public void excluir(int id) throws SQLException {
    String sql = "DELETE FROM tecnico WHERE id_tecnico=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, id);
      if (pst.executeUpdate() == 0) throw new SQLException("Registro não encontrado.");
    }
  }

  private Tecnico mapear(ResultSet rs) throws SQLException {
    Tecnico e = new Tecnico();
    e.setId(rs.getInt("id_tecnico"));
    e.setNome(rs.getString("nome"));
    e.setTelefone(rs.getString("telefone"));
    e.setEmail(rs.getString("email"));
    e.setEspecialidade(rs.getString("especialidade"));
    e.setAtivo(rs.getBoolean("ativo"));
    return e;
  }
}
