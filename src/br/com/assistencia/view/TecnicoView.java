package br.com.assistencia.view;

import br.com.assistencia.model.Tecnico;

public class TecnicoView extends CadastroView<Tecnico> {
  public TecnicoView() {
    super(
        "Técnicos",
        "Gerencie os profissionais e sua disponibilidade para novos atendimentos.",
        new String[] {"ID", "Nome", "Especialidade", "Ativo"},
        e ->
            new Object[] {
              e.getId(), e.getNome(), e.getEspecialidade(), e.getAtivo() ? "Sim" : "Não"
            });
    formulario.campoTexto("nome", "Nome *");
    formulario.campoTexto("telefone", "Telefone *");
    formulario.campoTexto("email", "E-mail");
    formulario.campoTexto("especialidade", "Especialidade *");
    formulario.marcado("ativo", "Disponível para novas OS");
  }
}
