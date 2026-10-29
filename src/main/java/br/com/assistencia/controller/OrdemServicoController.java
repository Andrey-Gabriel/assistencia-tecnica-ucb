package br.com.assistencia.controller;

import br.com.assistencia.dao.*;
import br.com.assistencia.model.*;
import br.com.assistencia.util.*;
import br.com.assistencia.view.OrdemServicoView;
import java.awt.Dimension;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrdemServicoController {
  private final OrdemServicoView view;
  private final OrdemServicoDAO dao;
  private final EquipamentoDAO equipamentos;
  private final TecnicoDAO tecnicos;
  private final ServicoDAO servicos;
  private OrdemServico selecionada;

  public OrdemServicoController(
      OrdemServicoView v, OrdemServicoDAO d, EquipamentoDAO e, TecnicoDAO t, ServicoDAO s) {
    view = v;
    dao = d;
    equipamentos = e;
    tecnicos = t;
    servicos = s;
    view.novo.addActionListener(ev -> novo());
    view.atualizar.addActionListener(ev -> atualizar());
    view.salvar.addActionListener(ev -> salvar());
    view.excluir.addActionListener(ev -> excluir());
    view.historico.addActionListener(ev -> historico());
    view.aoSelecionar(this::selecionar);
    novo();
  }

  private record Carga(
      List<OrdemServico> ordens,
      List<Equipamento> equipamentos,
      List<Tecnico> tecnicos,
      List<Servico> servicos) {}

  public void atualizar() {
    TarefaUI.executar(
        view,
        () -> new Carga(dao.listar(), equipamentos.listar(), tecnicos.listar(), servicos.listar()),
        c -> {
          view.formulario.opcoes("equipamento", c.equipamentos());
          view.formulario.opcoes("tecnico", c.tecnicos());
          view.formulario.opcoes("servico", c.servicos());
          view.dados(c.ordens());
          novo();
        });
  }

  private void novo() {
    selecionada = null;
    view.limparSelecao();
    view.formulario.limpar();
    view.formulario.opcoes("status", List.of(SituacaoOS.ABERTA));
    view.formulario.selecionar("status", SituacaoOS.ABERTA);
    view.detalhe.setText("Nova ordem de serviço");
    view.salvar.setEnabled(true);
    view.excluir.setEnabled(false);
    view.historico.setEnabled(false);
  }

  private void selecionar(OrdemServico os) {
    selecionada = os;
    var f = view.formulario;
    f.limpar();
    f.selecionar("equipamento", os.getEquipamento());
    f.selecionar("tecnico", os.getTecnico());
    f.selecionar("servico", os.getServico());
    f.texto("problema", os.getProblemaRelatado());
    f.texto("previsao", Formatos.data(os.getDataPrevisao()));
    f.texto("diagnostico", os.getDiagnostico());
    f.texto("orcamento", Formatos.numero(os.getValorOrcado()));
    f.texto("solucao", os.getSolucaoAplicada());
    f.texto("final", Formatos.numero(os.getValorFinal()));
    f.opcoes("status", os.getStatus().destinos());
    f.selecionar("status", os.getStatus());
    f.habilitar("equipamento", false);
    if (os.getDataAprovacao() != null) {
      f.habilitar("servico", false);
      f.habilitar("orcamento", false);
    }
    if (os.getStatus().encerrada()) f.habilitarTodos(false);
    view.detalhe.setText(
        "OS #" + os.getId() + " | Abertura: " + Formatos.dataHora(os.getDataAbertura()));
    view.salvar.setEnabled(!os.getStatus().encerrada());
    view.excluir.setEnabled(
        os.getStatus() == SituacaoOS.ABERTA
            && os.getDiagnostico() == null
            && os.getValorOrcado() == null);
    view.historico.setEnabled(true);
  }

  private void salvar() {
    try {
      var f = view.formulario;
      OrdemServico os = new OrdemServico();
      os.setId(selecionada == null ? null : selecionada.getId());
      os.setStatus(selecionada == null ? SituacaoOS.ABERTA : selecionada.getStatus());
      os.setEquipamento(
          Validacao.escolhido(f.selecionado("equipamento"), Equipamento.class, "o equipamento"));
      os.setTecnico(Validacao.escolhido(f.selecionado("tecnico"), Tecnico.class, "o técnico"));
      os.setServico(Validacao.escolhido(f.selecionado("servico"), Servico.class, "o serviço"));
      if (!os.getTecnico().getAtivo()
          && (selecionada == null || !os.getTecnico().equals(selecionada.getTecnico())))
        throw new IllegalArgumentException("Selecione um técnico ativo.");
      if (!os.getServico().getAtivo()
          && (selecionada == null || !os.getServico().equals(selecionada.getServico())))
        throw new IllegalArgumentException("Selecione um serviço ativo.");
      os.setProblemaRelatado(
          Validacao.obrigatorio(f.texto("problema"), "Problema relatado", 1, 10000));
      os.setDiagnostico(Validacao.opcional(f.texto("diagnostico"), "Diagnóstico", 10000));
      os.setSolucaoAplicada(Validacao.opcional(f.texto("solucao"), "Solução", 10000));
      os.setValorOrcado(Formatos.valor(f.texto("orcamento"), false));
      os.setValorFinal(Formatos.valor(f.texto("final"), false));
      os.setDataPrevisao(Formatos.lerData(f.texto("previsao"), false));
      var abertura =
          selecionada == null
              ? java.time.LocalDate.now()
              : selecionada.getDataAbertura().toLocalDate();
      if (os.getDataPrevisao() != null && os.getDataPrevisao().isBefore(abertura))
        throw new IllegalArgumentException("Previsão não pode ser anterior à abertura.");
      SituacaoOS destino =
          Validacao.escolhido(f.selecionado("status"), SituacaoOS.class, "a situação");
      String obs = Validacao.opcional(f.texto("observacao"), "Observação", 255);
      if (destino == SituacaoOS.AGUARDANDO_APROVACAO
          || destino == SituacaoOS.EM_MANUTENCAO
          || destino.realizada())
        if (os.getDiagnostico() == null || os.getValorOrcado() == null)
          throw new IllegalArgumentException(
              "Informe diagnóstico e valor orçado antes de enviar o orçamento.");
      if (destino.realizada() && (os.getSolucaoAplicada() == null || os.getValorFinal() == null))
        throw new IllegalArgumentException(
            "Para concluir, informe solução aplicada e valor final.");
      if (destino != os.getStatus()
          && (destino == SituacaoOS.CANCELADA || destino == SituacaoOS.EM_MANUTENCAO))
        Validacao.obrigatorio(obs, "Observação da aprovação ou motivo do cancelamento", 5, 255);
      TarefaUI.executar(
          view,
          () -> {
            dao.salvar(os, destino, obs);
            return os;
          },
          salva -> atualizar());
    } catch (Exception e) {
      TarefaUI.erro(view, e);
    }
  }

  private void excluir() {
    if (selecionada == null) return;
    if (JOptionPane.showConfirmDialog(
            view,
            "Excluir a OS #" + selecionada.getId() + "?",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    int id = selecionada.getId();
    TarefaUI.executar(
        view,
        () -> {
          dao.excluir(id);
          return true;
        },
        ok -> atualizar());
  }

  private void historico() {
    if (selecionada == null) return;
    int id = selecionada.getId();
    TarefaUI.executar(
        view,
        () -> dao.historico(id),
        linhas -> {
          DefaultTableModel m =
              new DefaultTableModel(new String[] {"Data", "De", "Para", "Observação"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                  return false;
                }
              };
          for (Object[] l : linhas) {
            l[0] = Formatos.dataHora((java.time.LocalDateTime) l[0]);
            m.addRow(l);
          }
          JTable t = new JTable(m);
          t.setRowHeight(28);
          JScrollPane scroll = new JScrollPane(t);
          scroll.setPreferredSize(new Dimension(950, 280));
          JOptionPane.showMessageDialog(
              view, scroll, "Histórico da OS #" + id, JOptionPane.PLAIN_MESSAGE);
        });
  }
}
