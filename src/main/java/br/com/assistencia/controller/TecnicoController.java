package br.com.assistencia.controller;

import br.com.assistencia.dao.TecnicoDAO;
import br.com.assistencia.model.Tecnico;
import br.com.assistencia.view.TecnicoView;

public class TecnicoController extends CadastroController<Tecnico> {
  public TecnicoController(TecnicoView v, TecnicoDAO d) {
    super(v, d);
  }

  @Override
  protected Tecnico ler() {
    var f = view.formulario;
    Tecnico e = new Tecnico();
    e.setNome(Validacao.obrigatorio(f.texto("nome"), "Nome", 3, 150));
    e.setTelefone(Validacao.telefone(f.texto("telefone")));
    e.setEmail(Validacao.email(f.texto("email")));
    e.setEspecialidade(Validacao.obrigatorio(f.texto("especialidade"), "Especialidade", 1, 100));
    e.setAtivo(f.marcado("ativo"));
    return e;
  }

  @Override
  protected void mostrar(Tecnico e) {
    var f = view.formulario;
    f.texto("nome", e.getNome());
    f.texto("telefone", e.getTelefone());
    f.texto("email", e.getEmail());
    f.texto("especialidade", e.getEspecialidade());
    f.marcar("ativo", e.getAtivo());
  }
}
