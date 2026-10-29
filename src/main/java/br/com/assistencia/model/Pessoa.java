package br.com.assistencia.model;

public abstract class Pessoa extends Entidade {
  private String nome;
  private String telefone;
  private String email;

  public String getNome() {
    return nome;
  }

  public void setNome(String valor) {
    this.nome = valor;
  }

  public String getTelefone() {
    return telefone;
  }

  public void setTelefone(String valor) {
    this.telefone = valor;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String valor) {
    this.email = valor;
  }

  @Override
  public String toString() {
    return nome;
  }
}
