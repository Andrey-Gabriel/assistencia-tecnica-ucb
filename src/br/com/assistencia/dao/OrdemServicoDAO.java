package br.com.assistencia.dao;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.model.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class OrdemServicoDAO {
  private final ConnectionFactory conexoes;

  public OrdemServicoDAO(ConnectionFactory c) {
    conexoes = c;
  }

  private static final String SELECT = "SELECT * FROM vw_ordens_servico";

  public List<OrdemServico> listar() throws SQLException {
    return consultar(null);
  }

  public List<OrdemServico> consultar(FiltroRelatorio filtro) throws SQLException {
    String sql = SELECT;
    if (filtro != null)
      sql +=
          " WHERE data_abertura >= ? AND data_abertura < ?"
              + (filtro.tecnico() != null ? " AND id_tecnico=?" : "")
              + (filtro.situacao() != null ? " AND status=?" : "");
    sql += " ORDER BY data_abertura DESC, numero_os DESC";
    try (Connection c = conexoes.abrir();
        PreparedStatement p = c.prepareStatement(sql)) {
      if (filtro != null) {
        p.setTimestamp(1, Timestamp.valueOf(filtro.inicio().atStartOfDay()));
        p.setTimestamp(2, Timestamp.valueOf(filtro.fim().plusDays(1).atStartOfDay()));
        int i = 3;
        if (filtro.tecnico() != null) p.setInt(i++, filtro.tecnico());
        if (filtro.situacao() != null) p.setString(i, filtro.situacao().name());
      }
      try (ResultSet r = p.executeQuery()) {
        List<OrdemServico> lista = new ArrayList<>();
        while (r.next()) lista.add(mapear(r));
        return lista;
      }
    }
  }

  private LocalDateTime data(ResultSet r, String campo) throws SQLException {
    Timestamp t = r.getTimestamp(campo);
    return t == null ? null : t.toLocalDateTime();
  }

  private OrdemServico mapear(ResultSet r) throws SQLException {
    Cliente cliente = new Cliente();
    cliente.setId(r.getInt("id_cliente"));
    cliente.setNome(r.getString("cliente"));
    cliente.setTelefone(r.getString("telefone_cliente"));
    Equipamento e = new Equipamento();
    e.setId(r.getInt("id_equipamento"));
    e.setCliente(cliente);
    e.setMarca("");
    e.setModelo(r.getString("equipamento"));
    e.setNumeroSerie(r.getString("numero_serie"));
    Tecnico t = new Tecnico();
    t.setId(r.getInt("id_tecnico"));
    t.setNome(r.getString("tecnico"));
    Servico s = new Servico();
    s.setId(r.getInt("id_servico"));
    s.setNome(r.getString("servico"));
    OrdemServico os = new OrdemServico();
    os.setId(r.getInt("numero_os"));
    os.setEquipamento(e);
    os.setTecnico(t);
    os.setServico(s);
    os.setProblemaRelatado(r.getString("problema_relatado"));
    os.setDiagnostico(r.getString("diagnostico"));
    os.setSolucaoAplicada(r.getString("solucao_aplicada"));
    os.setValorOrcado(r.getBigDecimal("valor_orcado"));
    os.setValorFinal(r.getBigDecimal("valor_final"));
    os.setStatus(SituacaoOS.valueOf(r.getString("status")));
    os.setDataAbertura(data(r, "data_abertura"));
    java.sql.Date d = r.getDate("data_previsao");
    os.setDataPrevisao(d == null ? null : d.toLocalDate());
    os.setDataAprovacao(data(r, "data_aprovacao"));
    os.setDataConclusao(data(r, "data_conclusao"));
    os.setDataEntrega(data(r, "data_entrega"));
    os.setDataCancelamento(data(r, "data_cancelamento"));
    os.setMotivoCancelamento(r.getString("motivo_cancelamento"));
    os.setDataStatus(data(r, "data_status"));
    return os;
  }

  // Os dados editados e a mudança de situação são atômicos: falha em qualquer etapa desfaz tudo.
  public void salvar(OrdemServico os, SituacaoOS destino, String observacao) throws SQLException {
    try (Connection c = conexoes.abrir()) {
      c.setAutoCommit(false);
      try {
        if (os.getId() == null) {
          if (destino != SituacaoOS.ABERTA)
            throw new SQLException("Uma nova OS deve iniciar como Aberta.");
          try (PreparedStatement p =
              c.prepareStatement(
                  "INSERT INTO"
                      + " ordem_servico(id_equipamento,id_tecnico,id_servico,problema_relatado,diagnostico,solucao_aplicada,valor_orcado,valor_final,data_previsao)"
                      + " VALUES (?,?,?,?,?,?,?,?,?)",
                  Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, os.getEquipamento().getId());
            preencher(p, os, 2);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
              if (r.next()) os.setId(r.getInt(1));
            }
          }
        } else {
          try (PreparedStatement p =
              c.prepareStatement(
                  "SELECT status FROM ordem_servico WHERE id_ordem_servico=? FOR UPDATE")) {
            p.setInt(1, os.getId());
            try (ResultSet r = p.executeQuery()) {
              if (!r.next() || !r.getString(1).equals(os.getStatus().name()))
                throw new SQLException(
                    "A OS foi alterada por outra operação. Atualize antes de salvar.");
            }
          }
          try (PreparedStatement p =
              c.prepareStatement(
                  "UPDATE ordem_servico SET"
                      + " id_tecnico=?,id_servico=?,problema_relatado=?,diagnostico=?,solucao_aplicada=?,valor_orcado=?,valor_final=?,data_previsao=?"
                      + " WHERE id_ordem_servico=?")) {
            preencher(p, os, 1);
            p.setInt(9, os.getId());
            p.executeUpdate();
          }
          if (destino != os.getStatus())
            try (CallableStatement p = c.prepareCall("{call sp_alterar_status_os(?,?,?,?)}")) {
              p.setInt(1, os.getId());
              p.setString(2, destino.name());
              p.setNull(3, Types.TIMESTAMP);
              p.setString(4, observacao);
              p.execute();
            }
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  private void preencher(PreparedStatement p, OrdemServico os, int i) throws SQLException {
    p.setInt(i++, os.getTecnico().getId());
    p.setInt(i++, os.getServico().getId());
    p.setString(i++, os.getProblemaRelatado());
    p.setString(i++, os.getDiagnostico());
    p.setString(i++, os.getSolucaoAplicada());
    p.setBigDecimal(i++, os.getValorOrcado());
    p.setBigDecimal(i++, os.getValorFinal());
    p.setObject(i, os.getDataPrevisao());
  }

  public void excluir(int id) throws SQLException {
    try (Connection c = conexoes.abrir();
        PreparedStatement p =
            c.prepareStatement("DELETE FROM ordem_servico WHERE id_ordem_servico=?")) {
      p.setInt(1, id);
      if (p.executeUpdate() == 0) throw new SQLException("OS não encontrada.");
    }
  }

  public List<Object[]> historico(int id) throws SQLException {
    try (Connection c = conexoes.abrir();
        PreparedStatement p =
            c.prepareStatement(
                "SELECT data_evento,status_anterior,status_novo,observacao FROM"
                    + " ordem_servico_historico WHERE id_ordem_servico=? ORDER BY"
                    + " data_evento,id_historico")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        List<Object[]> lista = new ArrayList<>();
        while (r.next())
          lista.add(
              new Object[] {
                r.getTimestamp(1).toLocalDateTime(), r.getString(2), r.getString(3), r.getString(4)
              });
        return lista;
      }
    }
  }
}
