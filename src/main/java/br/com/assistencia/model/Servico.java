package br.com.assistencia.model;

public class Servico extends Entidade {
  private String nome;
  private String descricao;
  private java.math.BigDecimal valorReferencia;
  private int prazoEstimadoDias;
  private boolean ativo;

  public String getNome() {
    return nome;
  }

  public void setNome(String valor) {
    this.nome = valor;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String valor) {
    this.descricao = valor;
  }

  public java.math.BigDecimal getValorReferencia() {
    return valorReferencia;
  }

  public void setValorReferencia(java.math.BigDecimal valor) {
    this.valorReferencia = valor;
  }

  public int getPrazoEstimadoDias() {
    return prazoEstimadoDias;
  }

  public void setPrazoEstimadoDias(int valor) {
    this.prazoEstimadoDias = valor;
  }

  public boolean getAtivo() {
    return ativo;
  }

  public void setAtivo(boolean valor) {
    this.ativo = valor;
  }

  @Override
  public String toString() {
    return nome + (ativo ? "" : " (inativo)");
  }
}
