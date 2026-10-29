package br.com.assistencia.view;

import br.com.assistencia.model.Entidade;
import java.awt.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CadastroView<T extends Entidade> extends JPanel {
  public final Formulario formulario = new Formulario();
  public final JButton novo = new JButton("Novo"),
      salvar = new JButton("Salvar"),
      alterar = new JButton("Alterar"),
      excluir = new JButton("Excluir"),
      atualizar = new JButton("Atualizar listagem");
  public final JPanel acoes = new JPanel(new GridLayout(0, 2, 6, 6));
  public final JLabel detalhe = new JLabel("Novo registro"), resumo = new JLabel(" ");
  private final JLabel quantidade = new JLabel("0 registros");
  private final JTable tabela;
  private final DefaultTableModel modelo;
  private final Function<T, Object[]> linha;
  private List<T> dados = List.of();

  public CadastroView(
      String titulo, String orientacao, String[] colunas, Function<T, Object[]> linha) {
    super(new BorderLayout(10, 10));
    this.linha = linha;
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    JPanel cabecalho = new JPanel(new BorderLayout(0, 5));
    JLabel nome = new JLabel(titulo);
    nome.setFont(nome.getFont().deriveFont(Font.BOLD, 20));
    cabecalho.add(nome, BorderLayout.NORTH);
    cabecalho.add(new JLabel(orientacao));
    add(cabecalho, BorderLayout.NORTH);
    modelo =
        new DefaultTableModel(colunas, 0) {
          @Override
          public boolean isCellEditable(int l, int c) {
            return false;
          }

          @Override
          public Class<?> getColumnClass(int c) {
            return c == 0 ? Integer.class : Object.class;
          }
        };
    tabela = new JTable(modelo);
    tabela.setRowHeight(28);
    tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabela.setAutoCreateRowSorter(true);
    tabela.setFillsViewportHeight(true);
    tabela.getTableHeader().setReorderingAllowed(false);
    tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
    for (int i = 1; i < colunas.length; i++)
      tabela
          .getColumnModel()
          .getColumn(i)
          .setPreferredWidth(
              colunas[i].equals("Situação") ? 170 : colunas[i].equals("E-mail") ? 210 : 140);
    JPanel listagem = new JPanel(new BorderLayout(8, 8));
    listagem.setBorder(BorderFactory.createTitledBorder("Registros cadastrados"));
    JTextField busca = new JTextField();
    JLabel buscarLabel = new JLabel("Buscar:");
    buscarLabel.setLabelFor(busca);
    busca.setToolTipText("Digite parte de um dado da tabela para filtrar a listagem");
    JPanel buscaLinha = new JPanel(new BorderLayout(8, 0));
    buscaLinha.add(buscarLabel, BorderLayout.WEST);
    buscaLinha.add(busca);
    buscaLinha.add(quantidade, BorderLayout.EAST);
    listagem.add(buscaLinha, BorderLayout.NORTH);
    listagem.add(new JScrollPane(tabela));
    busca
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              void filtrar() {
                ((TableRowSorter<?>) tabela.getRowSorter())
                    .setRowFilter(
                        RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(busca.getText())));
                contar();
              }

              public void insertUpdate(DocumentEvent e) {
                filtrar();
              }

              public void removeUpdate(DocumentEvent e) {
                filtrar();
              }

              public void changedUpdate(DocumentEvent e) {
                filtrar();
              }
            });
    JPanel edicao = new JPanel(new BorderLayout(8, 8));
    edicao.setBorder(BorderFactory.createTitledBorder("Dados do registro"));
    detalhe.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
    detalhe.setFont(detalhe.getFont().deriveFont(Font.BOLD));
    edicao.add(detalhe, BorderLayout.NORTH);
    JScrollPane campos = new JScrollPane(formulario);
    campos.setBorder(BorderFactory.createEmptyBorder());
    campos.getVerticalScrollBar().setUnitIncrement(18);
    edicao.add(campos);
    acoes.add(novo);
    acoes.add(salvar);
    acoes.add(alterar);
    acoes.add(excluir);
    JPanel botoes = new JPanel(new BorderLayout(0, 6));
    botoes.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    botoes.add(acoes);
    botoes.add(atualizar, BorderLayout.SOUTH);
    atualizar.setToolTipText("Recarregar os dados do banco e limpar o formulário");
    edicao.add(botoes, BorderLayout.SOUTH);
    alterar.setEnabled(false);
    excluir.setEnabled(false);
    JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listagem, edicao);
    divisao.setResizeWeight(0.65);
    divisao.setDividerSize(8);
    listagem.setMinimumSize(new Dimension(400, 250));
    edicao.setMinimumSize(new Dimension(410, 250));
    add(divisao);
    add(resumo, BorderLayout.SOUTH);
    addComponentListener(
        new java.awt.event.ComponentAdapter() {
          private boolean iniciado;

          @Override
          public void componentResized(java.awt.event.ComponentEvent e) {
            if (!iniciado && divisao.getWidth() > 0) {
              divisao.setDividerLocation(Math.max(400, divisao.getWidth() - 440));
              iniciado = true;
            }
          }
        });
  }

  public void dados(List<T> lista) {
    dados = List.copyOf(lista);
    modelo.setRowCount(0);
    for (T e : dados) modelo.addRow(linha.apply(e));
    contar();
  }

  private void contar() {
    int total = modelo.getRowCount(), visiveis = tabela.getRowCount();
    quantidade.setText(visiveis == total ? total + " registros" : visiveis + " de " + total);
    resumo.setText(
        total == 0
            ? "Nenhum registro cadastrado. Clique em Novo para preencher os dados."
            : visiveis == 0
                ? "Nenhum resultado para a busca."
                : "Exibindo "
                    + visiveis
                    + " de "
                    + total
                    + " registros. Selecione uma linha para alterar ou excluir.");
  }

  public void aoSelecionar(Consumer<T> acao) {
    tabela
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0)
                acao.accept(dados.get(tabela.convertRowIndexToModel(tabela.getSelectedRow())));
            });
  }

  public void limparSelecao() {
    tabela.clearSelection();
  }
}
