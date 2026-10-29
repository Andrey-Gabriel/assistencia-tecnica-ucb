package br.com.assistencia.view;

import br.com.assistencia.model.Entidade;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CadastroView<T extends Entidade> extends JPanel {
  public final Formulario formulario = new Formulario();
  public final JButton novo = new JButton("Novo"),
      salvar = new JButton("Salvar"),
      excluir = new JButton("Excluir"),
      atualizar = new JButton("Atualizar");
  public final JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
  public final JLabel detalhe = new JLabel("Novo registro"), resumo = new JLabel(" ");
  private final JTable tabela;
  private final DefaultTableModel modelo;
  private final Function<T, Object[]> linha;
  private List<T> dados = List.of();

  public CadastroView(
      String titulo, String orientacao, String[] colunas, Function<T, Object[]> linha) {
    super(new BorderLayout(12, 12));
    this.linha = linha;
    setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
    JPanel cabecalho = new JPanel(new GridLayout(2, 1, 0, 4));
    JLabel nome = new JLabel(titulo);
    nome.setFont(nome.getFont().deriveFont(Font.BOLD, 23));
    cabecalho.add(nome);
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
    tabela.setRowHeight(31);
    tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabela.setAutoCreateRowSorter(true);
    tabela.setFillsViewportHeight(true);
    JPanel listagem = new JPanel(new BorderLayout(0, 8));
    JTextField busca = new JTextField();
    busca.putClientProperty("JTextField.placeholderText", "Filtrar a listagem");
    JPanel buscaLinha = new JPanel(new BorderLayout(8, 0));
    buscaLinha.add(new JLabel("Buscar:"), BorderLayout.WEST);
    buscaLinha.add(busca);
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
    JPanel edicao = new JPanel(new BorderLayout(0, 5));
    detalhe.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 0));
    edicao.add(detalhe, BorderLayout.NORTH);
    edicao.add(new JScrollPane(formulario));
    acoes.add(novo);
    acoes.add(salvar);
    acoes.add(excluir);
    acoes.add(atualizar);
    edicao.add(acoes, BorderLayout.SOUTH);
    JSplitPane divisao = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listagem, edicao);
    divisao.setResizeWeight(0.60);
    divisao.setDividerLocation(690);
    edicao.setMinimumSize(new Dimension(380, 250));
    add(divisao);
    add(resumo, BorderLayout.SOUTH);
    excluir.setEnabled(false);
  }

  public void dados(List<T> lista) {
    dados = List.copyOf(lista);
    modelo.setRowCount(0);
    for (T e : dados) modelo.addRow(linha.apply(e));
    resumo.setText(lista.size() + " registro(s). Selecione uma linha para consultar ou alterar.");
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
