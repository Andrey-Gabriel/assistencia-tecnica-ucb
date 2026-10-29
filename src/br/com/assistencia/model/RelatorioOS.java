package br.com.assistencia.model;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioOS(FiltroRelatorio filtro, List<OrdemServico> ordens) {
  public RelatorioOS {
    ordens = List.copyOf(ordens);
  }

  public long concluidas() {
    return ordens.stream().filter(o -> o.getStatus().realizada()).count();
  }

  public long canceladas() {
    return ordens.stream().filter(o -> o.getStatus() == SituacaoOS.CANCELADA).count();
  }

  public BigDecimal total() {
    return ordens.stream()
        .filter(o -> o.getStatus().realizada())
        .map(OrdemServico::getValorFinal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
