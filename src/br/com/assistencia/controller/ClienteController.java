package br.com.assistencia.controller;

import br.com.assistencia.dao.ClienteDAO;
import br.com.assistencia.model.Cliente;
import br.com.assistencia.view.ClienteView;

public class ClienteController extends CadastroController<Cliente> {
  public ClienteController(ClienteView v, ClienteDAO d) {
    super(v, d);
  }

  @Override
  protected Cliente ler() {
    var f = view.formulario;
    Cliente e = new Cliente();
    e.setNome(Validacao.obrigatorio(f.texto("nome"), "Nome", 3, 150));
    e.setDocumento(Validacao.documento(f.texto("documento")));
    e.setTelefone(Validacao.telefone(f.texto("telefone")));
    e.setEmail(Validacao.email(f.texto("email")));
    e.setEndereco(Validacao.opcional(f.texto("endereco"), "Endereço", 255));
    return e;
  }

  @Override
  protected void mostrar(Cliente e) {
    var f = view.formulario;
    f.texto("nome", e.getNome());
    f.texto("documento", e.getDocumento());
    f.texto("telefone", e.getTelefone());
    f.texto("email", e.getEmail());
    f.texto("endereco", e.getEndereco());
  }
}
