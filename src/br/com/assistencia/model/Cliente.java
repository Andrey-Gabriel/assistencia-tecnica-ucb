package br.com.assistencia.model;

public class Cliente extends Pessoa {
  private String documento;
  private String endereco;

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String valor) {
    this.documento = valor;
  }

  public String getEndereco() {
    return endereco;
  }

  public void setEndereco(String valor) {
    this.endereco = valor;
  }
}
