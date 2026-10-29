package br.com.assistencia.view;

import br.com.assistencia.model.*;
import br.com.assistencia.util.Formatos;
import javax.swing.*;

public class OrdemServicoView extends CadastroView<OrdemServico> {
  public final JButton historico = new JButton("Histórico");

  public OrdemServicoView() {
    super(
        "Ordens de serviço",
        "Abra o atendimento e acompanhe análise, orçamento, conserto e entrega.",
        new String[] {"OS", "Cliente", "Equipamento", "Técnico", "Situação", "Valor final"},
        e ->
            new Object[] {
              e.getId(),
              e.getEquipamento().getCliente().getNome(),
              e.getEquipamento().getModelo(),
              e.getTecnico().getNome(),
              e.getStatus(),
              Formatos.moeda(e.getValorFinal())
            });
    formulario.combo("equipamento", "Equipamento / cliente *");
    formulario.combo("tecnico", "Técnico *");
    formulario.combo("servico", "Serviço principal *");
    formulario.area("problema", "Problema relatado *");
    formulario.campoTexto("previsao", "Previsão (dd/mm/aaaa)");
    formulario.area("diagnostico", "Diagnóstico");
    formulario.campoTexto("orcamento", "Valor orçado (ex.: 150,00)");
    formulario.area("solucao", "Solução aplicada");
    formulario.campoTexto("final", "Valor final (ex.: 150,00)");
    formulario.combo("status", "Situação após salvar *");
    formulario.area("observacao", "Observação da mudança / motivo de cancelamento");
    acoes.add(historico);
    historico.setEnabled(false);
  }
}
