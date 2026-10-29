package br.com.assistencia.controller;

import br.com.assistencia.dao.*;
import br.com.assistencia.model.*;
import br.com.assistencia.service.RelatorioPdfService;
import br.com.assistencia.util.*;
import br.com.assistencia.view.RelatorioView;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RelatorioController {
  private final RelatorioView view;
  private final OrdemServicoDAO ordens;
  private final TecnicoDAO tecnicos;
  private final RelatorioPdfService pdf;
  private RelatorioOS atual;

  public RelatorioController(
      RelatorioView v, OrdemServicoDAO o, TecnicoDAO t, RelatorioPdfService pdf) {
    view = v;
    ordens = o;
    tecnicos = t;
    this.pdf = pdf;
    view.consultar.addActionListener(e -> consultar());
    view.atualizar.addActionListener(e -> atualizar());
    view.pdf.addActionListener(e -> exportar());
    DocumentListener listener =
        new DocumentListener() {
          public void insertUpdate(DocumentEvent e) {
            invalidar();
          }

          public void removeUpdate(DocumentEvent e) {
            invalidar();
          }

          public void changedUpdate(DocumentEvent e) {
            invalidar();
          }
        };
    view.inicio.getDocument().addDocumentListener(listener);
    view.fim.getDocument().addDocumentListener(listener);
    view.tecnico.addActionListener(e -> invalidar());
    view.situacao.addActionListener(e -> invalidar());
  }

  private void invalidar() {
    atual = null;
    view.limpar();
  }

  public void atualizar() {
    TarefaUI.executar(view, tecnicos::listar, view::tecnicos);
  }

  public void consultar() {
    try {
      Tecnico tecnico = view.tecnico.getSelectedItem() instanceof Tecnico t ? t : null;
      SituacaoOS status = view.situacao.getSelectedItem() instanceof SituacaoOS s ? s : null;
      FiltroRelatorio filtro =
          new FiltroRelatorio(
              Formatos.lerData(view.inicio.getText(), true),
              Formatos.lerData(view.fim.getText(), true),
              tecnico == null ? null : tecnico.getId(),
              tecnico == null ? null : tecnico.getNome(),
              status);
      TarefaUI.executar(
          view,
          () -> new RelatorioOS(filtro, ordens.consultar(filtro)),
          rel -> {
            atual = rel;
            view.mostrar(rel);
          });
    } catch (Exception e) {
      TarefaUI.erro(view, e);
    }
  }

  private void exportar() {
    if (atual == null) return;
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Salvar relatório de ordens de serviço");
    chooser.setSelectedFile(new java.io.File("relatorio-ordens-servico.pdf"));
    chooser.setFileFilter(new FileNameExtensionFilter("Documento PDF", "pdf"));
    if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;
    Path path = chooser.getSelectedFile().toPath();
    if (!path.toString().toLowerCase().endsWith(".pdf")) path = Path.of(path + ".pdf");
    if (Files.exists(path)
        && JOptionPane.showConfirmDialog(
                view,
                "Substituir o arquivo existente?",
                "Confirmar substituição",
                JOptionPane.YES_NO_OPTION)
            != JOptionPane.YES_OPTION) return;
    Path destino = path;
    RelatorioOS rel = atual;
    TarefaUI.executar(
        view,
        () -> {
          pdf.exportar(rel, destino);
          return destino;
        },
        arquivo ->
            JOptionPane.showMessageDialog(
                view,
                "Relatório salvo em:\n" + arquivo.toAbsolutePath(),
                "PDF gerado",
                JOptionPane.INFORMATION_MESSAGE));
  }
}
