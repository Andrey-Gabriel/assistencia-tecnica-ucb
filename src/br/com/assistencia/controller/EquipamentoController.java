package br.com.assistencia.controller;

import br.com.assistencia.dao.*;
import br.com.assistencia.model.*;
import br.com.assistencia.view.EquipamentoView;
import java.util.*;

public class EquipamentoController extends CadastroController<Equipamento> {
  private final ClienteDAO clientes;

  public EquipamentoController(EquipamentoView v, EquipamentoDAO d, ClienteDAO c) {
    super(v, d);
    clientes = c;
  }

  @Override
  protected Map<String, List<?>> auxiliares() throws Exception {
    return Map.of("cliente", clientes.listar());
  }

  @Override
  protected Equipamento ler() {
    var f = view.formulario;
    Equipamento e = new Equipamento();
    e.setCliente(Validacao.escolhido(f.selecionado("cliente"), Cliente.class, "o cliente"));
    e.setTipo(Validacao.obrigatorio(f.texto("tipo"), "Tipo", 1, 60));
    e.setMarca(Validacao.obrigatorio(f.texto("marca"), "Marca", 1, 60));
    e.setModelo(Validacao.obrigatorio(f.texto("modelo"), "Modelo", 1, 100));
    e.setNumeroSerie(Validacao.opcional(f.texto("serie"), "Número de série", 100));
    e.setObservacoes(Validacao.opcional(f.texto("observacoes"), "Observações", 10000));
    return e;
  }

  @Override
  protected void mostrar(Equipamento e) {
    var f = view.formulario;
    f.selecionar("cliente", e.getCliente());
    f.texto("tipo", e.getTipo());
    f.texto("marca", e.getMarca());
    f.texto("modelo", e.getModelo());
    f.texto("serie", e.getNumeroSerie());
    f.texto("observacoes", e.getObservacoes());
  }
}
