package br.com.assistencia.model;

import java.util.List;

public enum SituacaoOS {
  ABERTA("Aberta"),
  EM_ANALISE("Em análise"),
  AGUARDANDO_APROVACAO("Aguardando aprovação"),
  EM_MANUTENCAO("Em manutenção"),
  CONCLUIDA("Concluída"),
  ENTREGUE("Entregue"),
  CANCELADA("Cancelada");
  private final String descricao;

  SituacaoOS(String descricao) {
    this.descricao = descricao;
  }

  @Override
  public String toString() {
    return descricao;
  }

  public boolean encerrada() {
    return this == ENTREGUE || this == CANCELADA;
  }

  public boolean realizada() {
    return this == CONCLUIDA || this == ENTREGUE;
  }

  public List<SituacaoOS> destinos() {
    return switch (this) {
      case ABERTA -> List.of(this, EM_ANALISE, CANCELADA);
      case EM_ANALISE -> List.of(this, AGUARDANDO_APROVACAO, CANCELADA);
      case AGUARDANDO_APROVACAO -> List.of(this, EM_MANUTENCAO, CANCELADA);
      case EM_MANUTENCAO -> List.of(this, CONCLUIDA, CANCELADA);
      case CONCLUIDA -> List.of(this, ENTREGUE);
      default -> List.of(this);
    };
  }
}
