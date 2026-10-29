package br.com.assistencia.model;

import java.time.LocalDate;

public record FiltroRelatorio(
    LocalDate inicio, LocalDate fim, Integer tecnico, String nomeTecnico, SituacaoOS situacao) {
  public FiltroRelatorio {
    if (inicio == null || fim == null || inicio.isAfter(fim) || fim.equals(LocalDate.MAX))
      throw new IllegalArgumentException(
          "Informe um período válido, com início anterior ou igual ao fim.");
  }
}
