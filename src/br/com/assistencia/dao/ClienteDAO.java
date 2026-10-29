package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO extends DAO implements CrudDAO<Cliente> {
  public ClienteDAO(ConnectionFactory conexao) {
    super(conexao);
  }

  @Override
  public void inserir(Cliente e) throws SQLException {
    String sql = "INSERT INTO cliente (nome,documento,telefone,email,endereco) VALUES (?,?,?,?,?)";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getDocumento());
      pst.setString(3, e.getTelefone());
      pst.setString(4, e.getEmail());
      pst.setString(5, e.getEndereco());
      pst.executeUpdate();
      try (ResultSet rs = pst.getGeneratedKeys()) {
        if (rs.next()) e.setId(rs.getInt(1));
      }
    }
  }

  @Override
  public List<Cliente> listar() throws SQLException {
    String sql = "SELECT * FROM cliente ORDER BY id_cliente DESC";
    List<Cliente> registros = new ArrayList<>();
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
      while (rs.next()) registros.add(mapear(rs));
    }
    return registros;
  }

  @Override
  public void alterar(Cliente e) throws SQLException {
    String sql =
        "UPDATE cliente SET nome=?,documento=?,telefone=?,email=?,endereco=? WHERE id_cliente=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setString(1, e.getNome());
      pst.setString(2, e.getDocumento());
      pst.setString(3, e.getTelefone());
      pst.setString(4, e.getEmail());
      pst.setString(5, e.getEndereco());
      pst.setInt(6, e.getId());
      if (pst.executeUpdate() == 0)
        throw new SQLException("Registro não encontrado. Atualize a listagem.");
    }
  }

  @Override
  public void excluir(int id) throws SQLException {
    String sql = "DELETE FROM cliente WHERE id_cliente=?";
    try (Connection con = abrirBanco();
        PreparedStatement pst = con.prepareStatement(sql)) {
      pst.setInt(1, id);
      if (pst.executeUpdate() == 0) throw new SQLException("Registro não encontrado.");
    }
  }

  private Cliente mapear(ResultSet rs) throws SQLException {
    Cliente e = new Cliente();
    e.setId(rs.getInt("id_cliente"));
    e.setNome(rs.getString("nome"));
    e.setDocumento(rs.getString("documento"));
    e.setTelefone(rs.getString("telefone"));
    e.setEmail(rs.getString("email"));
    e.setEndereco(rs.getString("endereco"));
    return e;
  }
}
