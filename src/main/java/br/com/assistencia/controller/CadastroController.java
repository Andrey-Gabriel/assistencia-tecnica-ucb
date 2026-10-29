package br.com.assistencia.controller;

import br.com.assistencia.dao.CrudDAO;
import br.com.assistencia.model.Entidade;
import br.com.assistencia.util.TarefaUI;
import br.com.assistencia.view.CadastroView;
import java.util.*;
import javax.swing.*;

public abstract class CadastroController<T extends Entidade> {
  protected final CadastroView<T> view;
  protected final CrudDAO<T> dao;
  protected T selecionado;

  protected CadastroController(CadastroView<T> view, CrudDAO<T> dao) {
    this.view = view;
    this.dao = dao;
    view.novo.addActionListener(e -> novo());
    view.atualizar.addActionListener(e -> atualizar());
    view.salvar.addActionListener(e -> salvar());
    view.excluir.addActionListener(e -> excluir());
    view.aoSelecionar(this::selecionar);
  }

  protected abstract T ler();

  protected abstract void mostrar(T e);

  protected Map<String, List<?>> auxiliares() throws Exception {
    return Map.of();
  }

  private record Carga<E>(List<E> dados, Map<String, List<?>> opcoes) {}

  public void atualizar() {
    TarefaUI.executar(
        view,
        () -> new Carga<>(dao.listar(), auxiliares()),
        c -> {
          c.opcoes().forEach(view.formulario::opcoes);
          view.dados(c.dados());
          novo();
        });
  }

  protected void novo() {
    selecionado = null;
    view.limparSelecao();
    view.formulario.limpar();
    view.detalhe.setText("Novo registro");
    view.excluir.setEnabled(false);
  }

  private void selecionar(T e) {
    selecionado = e;
    view.formulario.limpar();
    mostrar(e);
    view.detalhe.setText("Editando registro #" + e.getId());
    view.excluir.setEnabled(true);
  }

  private void salvar() {
    try {
      T e = ler();
      e.setId(selecionado == null ? null : selecionado.getId());
      TarefaUI.executar(
          view,
          () -> {
            if (e.getId() == null) dao.inserir(e);
            else dao.alterar(e);
            return e;
          },
          salvo -> {
            atualizar();
          });
    } catch (Exception e) {
      TarefaUI.erro(view, e);
    }
  }

  private void excluir() {
    if (selecionado == null) return;
    if (JOptionPane.showConfirmDialog(
            view,
            "Excluir o registro #" + selecionado.getId() + "?",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    int id = selecionado.getId();
    TarefaUI.executar(
        view,
        () -> {
          dao.excluir(id);
          return true;
        },
        ok -> atualizar());
  }
}
