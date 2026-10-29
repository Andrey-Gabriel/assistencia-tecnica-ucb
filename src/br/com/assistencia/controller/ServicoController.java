package br.com.assistencia.controller;

import br.com.assistencia.dao.ServicoDAO;
import br.com.assistencia.model.Servico;
import br.com.assistencia.util.Formatos;
import br.com.assistencia.view.ServicoView;

public class ServicoController extends CadastroController<Servico> {
  public ServicoController(ServicoView v, ServicoDAO d) {
    super(v, d);
  }

  @Override
  protected Servico ler() {
    var f = view.formulario;
    Servico e = new Servico();
    e.setNome(Validacao.obrigatorio(f.texto("nome"), "Nome", 1, 100));
    e.setDescricao(Validacao.opcional(f.texto("descricao"), "Descrição", 10000));
    e.setValorReferencia(Formatos.valor(f.texto("valor"), true));
    try {
      int n = Integer.parseInt(f.texto("prazo").trim());
      if (n < 1 || n > 65535) throw new NumberFormatException();
      e.setPrazoEstimadoDias(n);
    } catch (NumberFormatException erro) {
      throw new IllegalArgumentException("Prazo deve ser um número inteiro de 1 a 65535 dias.");
    }
    e.setAtivo(f.marcado("ativo"));
    return e;
  }

  @Override
  protected void mostrar(Servico e) {
    var f = view.formulario;
    f.texto("nome", e.getNome());
    f.texto("descricao", e.getDescricao());
    f.texto("valor", Formatos.numero(e.getValorReferencia()));
    f.texto("prazo", String.valueOf(e.getPrazoEstimadoDias()));
    f.marcar("ativo", e.getAtivo());
  }
}
