package br.com.assistencia.model;

public class Tecnico extends Pessoa {
  @Override
  public String toString() {
    return getNome() + (ativo ? "" : " (inativo)");
  }

  private String especialidade;
  private boolean ativo;

  public String getEspecialidade() {
    return especialidade;
  }

  public void setEspecialidade(String valor) {
    this.especialidade = valor;
  }

  public boolean getAtivo() {
    return ativo;
  }

  public void setAtivo(boolean valor) {
    this.ativo = valor;
  }
}
