package br.com.assistencia.view;

import br.com.assistencia.model.*;
import br.com.assistencia.util.Formatos;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RelatorioView extends JPanel {
  public final JTextField inicio = new JTextField(10), fim = new JTextField(10);
  public final JComboBox<Object> tecnico = new JComboBox<>(), situacao = new JComboBox<>();
  public final JButton consultar = new JButton("Consultar"),
      pdf = new JButton("Exportar PDF"),
      atualizar = new JButton("Atualizar técnicos");
  public final JLabel resumo = new JLabel("Escolha os filtros e clique em Consultar.");
  private final DefaultTableModel modelo =
      new DefaultTableModel(
          new String[] {
            "OS",
            "Abertura",
            "Cliente",
            "Equipamento",
            "Técnico",
            "Serviço",
            "Situação",
            "Valor final"
          },
          0) {
        @Override
        public boolean isCellEditable(int l, int c) {
          return false;
        }
      };

  public RelatorioView() {
    super(new BorderLayout(12, 12));
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    JPanel topo = new JPanel(new BorderLayout(0, 12));
    JLabel titulo = new JLabel("Relatório de ordens de serviço");
    titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 23));
    topo.add(titulo, BorderLayout.NORTH);
    JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 4));
    filtros.add(new JLabel("De:"));
    filtros.add(inicio);
    filtros.add(new JLabel("Até:"));
    filtros.add(fim);
    filtros.add(tecnico);
    filtros.add(situacao);
    filtros.add(consultar);
    filtros.add(pdf);
    filtros.add(atualizar);
    topo.add(filtros);
    add(topo, BorderLayout.NORTH);
    inicio.setText(Formatos.data(LocalDate.now().withDayOfMonth(1)));
    fim.setText(Formatos.data(LocalDate.now()));
    tecnico.addItem("Todos os técnicos");
    situacao.addItem("Todas as situações");
    for (SituacaoOS s : SituacaoOS.values()) situacao.addItem(s);
    pdf.setEnabled(false);
    JTable tabela = new JTable(modelo);
    tabela.setRowHeight(31);
    tabela.setFillsViewportHeight(true);
    tabela.setAutoCreateRowSorter(true);
    add(new JScrollPane(tabela));
    JPanel rodape = new JPanel(new GridLayout(2, 1, 0, 5));
    resumo.setFont(resumo.getFont().deriveFont(Font.BOLD, 14));
    rodape.add(resumo);
    rodape.add(
        new JLabel(
            "Período pela data de abertura. Total considera apenas OS concluídas ou entregues."));
    add(rodape, BorderLayout.SOUTH);
  }

  public void tecnicos(List<Tecnico> lista) {
    Object atual = tecnico.getSelectedItem();
    tecnico.removeAllItems();
    tecnico.addItem("Todos os técnicos");
    for (Tecnico t : lista) tecnico.addItem(t);
    if (atual instanceof Tecnico) tecnico.setSelectedItem(atual);
  }

  public void limpar() {
    modelo.setRowCount(0);
    pdf.setEnabled(false);
    resumo.setText("Filtros alterados. Clique em Consultar.");
  }

  public void mostrar(RelatorioOS rel) {
    modelo.setRowCount(0);
    for (OrdemServico os : rel.ordens())
      modelo.addRow(
          new Object[] {
            os.getId(),
            Formatos.dataHora(os.getDataAbertura()),
            os.getEquipamento().getCliente().getNome(),
            os.getEquipamento().getModelo(),
            os.getTecnico().getNome(),
            os.getServico().getNome(),
            os.getStatus(),
            Formatos.moeda(os.getValorFinal())
          });
    resumo.setText(
        rel.ordens().size()
            + " OS | "
            + rel.concluidas()
            + " concluídas/entregues | "
            + rel.canceladas()
            + " canceladas | Total: "
            + Formatos.moeda(rel.total()));
    pdf.setEnabled(true);
  }
}
