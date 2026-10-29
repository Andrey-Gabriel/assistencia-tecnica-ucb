package br.com.assistencia.model;

public class Equipamento extends Entidade {
  private Cliente cliente;
  private String tipo;
  private String marca;
  private String modelo;
  private String numeroSerie;
  private String observacoes;

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente valor) {
    this.cliente = valor;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String valor) {
    this.tipo = valor;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String valor) {
    this.marca = valor;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String valor) {
    this.modelo = valor;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public void setNumeroSerie(String valor) {
    this.numeroSerie = valor;
  }

  public String getObservacoes() {
    return observacoes;
  }

  public void setObservacoes(String valor) {
    this.observacoes = valor;
  }

  @Override
  public String toString() {
    return ((marca == null ? "" : marca) + " " + (modelo == null ? "" : modelo)).trim()
        + (cliente == null ? "" : " - " + cliente.getNome());
  }
}
