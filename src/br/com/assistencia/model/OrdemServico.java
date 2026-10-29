package br.com.assistencia.model;

public class OrdemServico extends Entidade {
  private Equipamento equipamento;
  private Tecnico tecnico;
  private Servico servico;
  private String problemaRelatado;
  private String diagnostico;
  private String solucaoAplicada;
  private java.math.BigDecimal valorOrcado;
  private java.math.BigDecimal valorFinal;
  private SituacaoOS status;
  private java.time.LocalDateTime dataAbertura;
  private java.time.LocalDate dataPrevisao;
  private java.time.LocalDateTime dataAprovacao;
  private java.time.LocalDateTime dataConclusao;
  private java.time.LocalDateTime dataEntrega;
  private java.time.LocalDateTime dataCancelamento;
  private String motivoCancelamento;
  private java.time.LocalDateTime dataStatus;

  public Equipamento getEquipamento() {
    return equipamento;
  }

  public void setEquipamento(Equipamento valor) {
    this.equipamento = valor;
  }

  public Tecnico getTecnico() {
    return tecnico;
  }

  public void setTecnico(Tecnico valor) {
    this.tecnico = valor;
  }

  public Servico getServico() {
    return servico;
  }

  public void setServico(Servico valor) {
    this.servico = valor;
  }

  public String getProblemaRelatado() {
    return problemaRelatado;
  }

  public void setProblemaRelatado(String valor) {
    this.problemaRelatado = valor;
  }

  public String getDiagnostico() {
    return diagnostico;
  }

  public void setDiagnostico(String valor) {
    this.diagnostico = valor;
  }

  public String getSolucaoAplicada() {
    return solucaoAplicada;
  }

  public void setSolucaoAplicada(String valor) {
    this.solucaoAplicada = valor;
  }

  public java.math.BigDecimal getValorOrcado() {
    return valorOrcado;
  }

  public void setValorOrcado(java.math.BigDecimal valor) {
    this.valorOrcado = valor;
  }

  public java.math.BigDecimal getValorFinal() {
    return valorFinal;
  }

  public void setValorFinal(java.math.BigDecimal valor) {
    this.valorFinal = valor;
  }

  public SituacaoOS getStatus() {
    return status;
  }

  public void setStatus(SituacaoOS valor) {
    this.status = valor;
  }

  public java.time.LocalDateTime getDataAbertura() {
    return dataAbertura;
  }

  public void setDataAbertura(java.time.LocalDateTime valor) {
    this.dataAbertura = valor;
  }

  public java.time.LocalDate getDataPrevisao() {
    return dataPrevisao;
  }

  public void setDataPrevisao(java.time.LocalDate valor) {
    this.dataPrevisao = valor;
  }

  public java.time.LocalDateTime getDataAprovacao() {
    return dataAprovacao;
  }

  public void setDataAprovacao(java.time.LocalDateTime valor) {
    this.dataAprovacao = valor;
  }

  public java.time.LocalDateTime getDataConclusao() {
    return dataConclusao;
  }

  public void setDataConclusao(java.time.LocalDateTime valor) {
    this.dataConclusao = valor;
  }

  public java.time.LocalDateTime getDataEntrega() {
    return dataEntrega;
  }

  public void setDataEntrega(java.time.LocalDateTime valor) {
    this.dataEntrega = valor;
  }

  public java.time.LocalDateTime getDataCancelamento() {
    return dataCancelamento;
  }

  public void setDataCancelamento(java.time.LocalDateTime valor) {
    this.dataCancelamento = valor;
  }

  public String getMotivoCancelamento() {
    return motivoCancelamento;
  }

  public void setMotivoCancelamento(String valor) {
    this.motivoCancelamento = valor;
  }

  public java.time.LocalDateTime getDataStatus() {
    return dataStatus;
  }

  public void setDataStatus(java.time.LocalDateTime valor) {
    this.dataStatus = valor;
  }
}
