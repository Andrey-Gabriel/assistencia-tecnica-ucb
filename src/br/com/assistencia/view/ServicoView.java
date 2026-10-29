package br.com.assistencia.view;

import br.com.assistencia.model.Servico;
import br.com.assistencia.util.Formatos;

public class ServicoView extends CadastroView<Servico> {
  public ServicoView() {
    super(
        "Serviços",
        "Mantenha o catálogo de serviços, preços sugeridos e prazos.",
        new String[] {"ID", "Serviço", "Valor sugerido", "Prazo", "Ativo"},
        e ->
            new Object[] {
              e.getId(),
              e.getNome(),
              Formatos.moeda(e.getValorReferencia()),
              e.getPrazoEstimadoDias() + " dia(s)",
              e.getAtivo() ? "Sim" : "Não"
            });
    formulario.campoTexto("nome", "Nome do serviço *");
    formulario.area("descricao", "Descrição");
    formulario.campoTexto("valor", "Valor de referência * (ex.: 150,00)");
    formulario.campoTexto("prazo", "Prazo estimado em dias *");
    formulario.marcado("ativo", "Disponível para novas OS");
  }
}
