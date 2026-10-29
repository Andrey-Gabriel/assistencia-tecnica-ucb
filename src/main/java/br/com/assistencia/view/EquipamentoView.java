package br.com.assistencia.view;

import br.com.assistencia.model.Equipamento;

public class EquipamentoView extends CadastroView<Equipamento> {
  public EquipamentoView() {
    super(
        "Equipamentos",
        "Selecione o proprietário e identifique o aparelho recebido.",
        new String[] {"ID", "Cliente", "Tipo", "Marca", "Modelo"},
        e ->
            new Object[] {
              e.getId(), e.getCliente().getNome(), e.getTipo(), e.getMarca(), e.getModelo()
            });
    formulario.combo("cliente", "Cliente *");
    formulario.campoTexto("tipo", "Tipo *");
    formulario.campoTexto("marca", "Marca *");
    formulario.campoTexto("modelo", "Modelo *");
    formulario.campoTexto("serie", "Número de série");
    formulario.area("observacoes", "Acessórios e observações");
  }
}
