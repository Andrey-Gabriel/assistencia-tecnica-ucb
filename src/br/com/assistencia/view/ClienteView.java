package br.com.assistencia.view;

import br.com.assistencia.model.Cliente;

public class ClienteView extends CadastroView<Cliente> {
  public ClienteView() {
    super(
        "Clientes",
        "Cadastre os proprietários dos equipamentos. Campos com * são obrigatórios.",
        new String[] {"ID", "Nome", "Telefone", "E-mail"},
        e -> new Object[] {e.getId(), e.getNome(), e.getTelefone(), e.getEmail()});
    formulario.campoTexto("nome", "Nome *");
    formulario.campoTexto("documento", "CPF/CNPJ (opcional)");
    formulario.campoTexto("telefone", "Telefone *");
    formulario.campoTexto("email", "E-mail");
    formulario.area("endereco", "Endereço");
  }
}
