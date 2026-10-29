package br.com.assistencia.service;

import br.com.assistencia.model.*;
import br.com.assistencia.util.Formatos;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

public class RelatorioPdfService {
  private static final Color TEAL = new Color(23, 91, 98), CLARO = new Color(238, 245, 246);

  private Font fonte(float tamanho, int estilo, Color cor) {
    return FontFactory.getFont(
        FontFactory.HELVETICA, BaseFont.WINANSI, false, tamanho, estilo, cor);
  }

  public void exportar(RelatorioOS rel, Path destino) throws IOException, DocumentException {
    Document doc = new Document(PageSize.A4.rotate(), 30, 30, 34, 38);
    try (OutputStream out = Files.newOutputStream(destino)) {
      PdfWriter writer = PdfWriter.getInstance(doc, out);
      writer.setPageEvent(
          new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
              Font f = fonte(8, Font.NORMAL, Color.GRAY);
              ColumnText.showTextAligned(
                  w.getDirectContent(),
                  Element.ALIGN_LEFT,
                  new Phrase("Assistência Técnica | Ordens de serviço", f),
                  30,
                  20,
                  0);
              ColumnText.showTextAligned(
                  w.getDirectContent(),
                  Element.ALIGN_RIGHT,
                  new Phrase("Página " + w.getPageNumber(), f),
                  d.getPageSize().getWidth() - 30,
                  20,
                  0);
            }
          });
      doc.addTitle("Relatório de ordens de serviço");
      doc.addAuthor("Sistema de Assistência Técnica");
      doc.open();
      doc.add(new Paragraph("ASSISTÊNCIA TÉCNICA", fonte(10, Font.BOLD, TEAL)));
      Paragraph titulo =
          new Paragraph("Relatório de ordens de serviço", fonte(23, Font.BOLD, TEAL));
      titulo.setSpacingAfter(8);
      doc.add(titulo);
      FiltroRelatorio f = rel.filtro();
      String filtros =
          "Abertura: "
              + Formatos.data(f.inicio())
              + " a "
              + Formatos.data(f.fim())
              + "   |   Técnico: "
              + (f.tecnico() == null ? "Todos" : f.nomeTecnico())
              + "   |   Situação: "
              + (f.situacao() == null ? "Todas" : f.situacao());
      Paragraph periodo = new Paragraph(filtros, fonte(10, Font.NORMAL, Color.DARK_GRAY));
      periodo.setSpacingAfter(5);
      doc.add(periodo);
      Paragraph emitido =
          new Paragraph(
              "Emitido em " + Formatos.dataHora(LocalDateTime.now()),
              fonte(9, Font.NORMAL, Color.GRAY));
      emitido.setSpacingAfter(14);
      doc.add(emitido);
      PdfPTable indicadores = new PdfPTable(4);
      indicadores.setWidthPercentage(100);
      for (String texto :
          new String[] {
            "OS no período: " + rel.ordens().size(),
            "Concluídas/entregues: " + rel.concluidas(),
            "Canceladas: " + rel.canceladas(),
            "Total dos serviços: " + Formatos.moeda(rel.total())
          }) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte(11, Font.BOLD, TEAL)));
        cell.setBackgroundColor(CLARO);
        cell.setPadding(10);
        cell.setBorderColor(Color.WHITE);
        indicadores.addCell(cell);
      }
      indicadores.setSpacingAfter(14);
      doc.add(indicadores);
      PdfPTable tabela =
          new PdfPTable(new float[] {0.45f, 0.85f, 1.35f, 1.7f, 1.25f, 1.3f, 1.25f, 0.85f});
      tabela.setWidthPercentage(100);
      tabela.setHeaderRows(1);
      tabela.setSplitLate(false);
      for (String nome :
          new String[] {
            "OS",
            "Abertura",
            "Cliente",
            "Equipamento",
            "Técnico",
            "Serviço",
            "Situação",
            "Valor final"
          }) {
        PdfPCell c = new PdfPCell(new Phrase(nome, fonte(9, Font.BOLD, Color.WHITE)));
        c.setBackgroundColor(TEAL);
        c.setPadding(7);
        c.setBorderColor(TEAL);
        tabela.addCell(c);
      }
      int linha = 0;
      for (OrdemServico os : rel.ordens()) {
        String[] valores = {
          String.valueOf(os.getId()),
          Formatos.data(os.getDataAbertura().toLocalDate()),
          os.getEquipamento().getCliente().getNome(),
          os.getEquipamento().getModelo(),
          os.getTecnico().getNome(),
          os.getServico().getNome(),
          os.getStatus().toString(),
          Formatos.moeda(os.getValorFinal())
        };
        for (int i = 0; i < valores.length; i++) {
          PdfPCell c = new PdfPCell(new Phrase(valores[i], fonte(9, Font.NORMAL, Color.DARK_GRAY)));
          c.setPadding(6);
          c.setBorderColor(new Color(220, 228, 230));
          if (linha % 2 == 0) c.setBackgroundColor(new Color(247, 250, 250));
          if (i == 7) c.setHorizontalAlignment(Element.ALIGN_RIGHT);
          tabela.addCell(c);
        }
        linha++;
      }
      if (rel.ordens().isEmpty()) {
        PdfPCell vazio =
            new PdfPCell(
                new Phrase(
                    "Nenhuma ordem de serviço encontrada para os filtros informados.",
                    fonte(11, Font.NORMAL, Color.DARK_GRAY)));
        vazio.setColspan(8);
        vazio.setPadding(18);
        tabela.addCell(vazio);
      }
      doc.add(tabela);
      Paragraph nota =
          new Paragraph(
              "O período considera a data de abertura, incluindo todo o último dia. As situações"
                  + " são as atuais. O total soma valores finais de OS concluídas ou entregues e"
                  + " não representa pagamentos recebidos.",
              fonte(9, Font.NORMAL, Color.GRAY));
      nota.setSpacingBefore(12);
      doc.add(nota);
      doc.close();
    } finally {
      if (doc.isOpen()) doc.close();
    }
  }
}
