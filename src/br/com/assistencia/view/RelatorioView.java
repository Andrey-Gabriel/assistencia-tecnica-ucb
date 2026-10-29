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

        @Override
        public Class<?> getColumnClass(int c) {
          return c == 0 ? Integer.class : Object.class;
        }
      };

  public RelatorioView() {
    super(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    JPanel topo = new JPanel(new BorderLayout(0, 10));
    JLabel titulo = new JLabel("Relatório de ordens de serviço");
    titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20));
    topo.add(titulo, BorderLayout.NORTH);
    JPanel filtros = new JPanel(new BorderLayout(0, 8));
    filtros.setBorder(BorderFactory.createTitledBorder("Filtros da consulta"));
    JPanel campos = new JPanel(new GridLayout(1, 4, 12, 0));
    campos.add(filtro("Data inicial (dd/mm/aaaa)", inicio));
    campos.add(filtro("Data final (dd/mm/aaaa)", fim));
    campos.add(filtro("Técnico", tecnico));
    campos.add(filtro("Situação", situacao));
    filtros.add(campos);
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    botoes.add(consultar);
    botoes.add(pdf);
    botoes.add(atualizar);
    filtros.add(botoes, BorderLayout.SOUTH);
    topo.add(filtros);
    add(topo, BorderLayout.NORTH);
    inicio.setText(Formatos.data(LocalDate.now().withDayOfMonth(1)));
    fim.setText(Formatos.data(LocalDate.now()));
    tecnico.addItem("Todos os técnicos");
    situacao.addItem("Todas as situações");
    for (SituacaoOS s : SituacaoOS.values()) situacao.addItem(s);
    pdf.setEnabled(false);

    JTable tabela = new JTable(modelo);
    tabela.setRowHeight(28);
    tabela.setFillsViewportHeight(true);
    tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabela.setAutoCreateRowSorter(true);
    tabela.getTableHeader().setReorderingAllowed(false);
    tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    int[] larguras = {50, 145, 150, 150, 150, 185, 170, 110};
    for (int i = 0; i < larguras.length; i++)
      tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
    JPanel resultados = new JPanel(new BorderLayout());
    resultados.setBorder(BorderFactory.createTitledBorder("Ordens de serviço encontradas"));
    resultados.add(new JScrollPane(tabela));
    add(resultados);
    JPanel rodape = new JPanel(new GridLayout(2, 1, 0, 6));
    resumo.setFont(resumo.getFont().deriveFont(Font.BOLD));
    rodape.add(resumo);
    rodape.add(
        new JLabel(
            "Período pela abertura da OS. O total considera somente serviços concluídos ou"
                + " entregues."));
    add(rodape, BorderLayout.SOUTH);
  }

  private JPanel filtro(String titulo, JComponent campo) {
    JPanel p = new JPanel(new BorderLayout(0, 5));
    JLabel label = new JLabel(titulo);
    label.setLabelFor(campo);
    p.add(label, BorderLayout.NORTH);
    campo.setPreferredSize(new Dimension(150, 28));
    p.add(campo);
    return p;
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
            os.getId(), Formatos.dataHora(os.getDataAbertura()),
                os.getEquipamento().getCliente().getNome(),
            os.getEquipamento().getModelo(), os.getTecnico().getNome(), os.getServico().getNome(),
            os.getStatus(), Formatos.moeda(os.getValorFinal())
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
