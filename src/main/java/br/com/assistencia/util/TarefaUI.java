package br.com.assistencia.util;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;

public final class TarefaUI {
  private TarefaUI() {}

  public static <T> void executar(JComponent painel, Callable<T> acao, Consumer<T> sucesso) {
    Map<Component, Boolean> estados = new IdentityHashMap<>();
    guardar(painel, estados);
    painel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    new SwingWorker<T, Void>() {
      @Override
      protected T doInBackground() throws Exception {
        return acao.call();
      }

      @Override
      protected void done() {
        estados.forEach(Component::setEnabled);
        painel.setCursor(Cursor.getDefaultCursor());
        try {
          sucesso.accept(get());
        } catch (Exception e) {
          erro(painel, e);
        }
      }
    }.execute();
  }

  private static void guardar(Component c, Map<Component, Boolean> estados) {
    estados.put(c, c.isEnabled());
    c.setEnabled(false);
    if (c instanceof Container p) for (Component filho : p.getComponents()) guardar(filho, estados);
  }

  public static void erro(Component pai, Throwable erro) {
    while (erro.getCause() != null && !(erro instanceof SQLException)) erro = erro.getCause();
    String texto = erro.getMessage();
    if (erro instanceof SQLException sql) {
      texto =
          switch (sql.getErrorCode()) {
            case 1451 -> "Este registro está vinculado a outros dados e não pode ser excluído.";
            case 1452 -> "Um cadastro relacionado não existe mais. Atualize a tela.";
            case 1062 ->
                "Já existe um registro com o mesmo documento, nome de serviço ou número de série.";
            case 1045 -> "Acesso negado ao MySQL. Confira usuário e senha em Conexão.";
            case 1049, 1146, 1305 ->
                "Banco incompleto ou inexistente. Execute os scripts da pasta sql na ordem"
                    + " numérica.";
            case 3819 ->
                "Os dados não atendem às validações do banco. Confira campos obrigatórios, valores"
                    + " e situação da OS.";
            default ->
                sql.getSQLState() != null && sql.getSQLState().startsWith("08")
                    ? "Não foi possível conectar ao MySQL. Verifique o servidor e os dados em"
                          + " Conexão."
                    : texto;
          };
    }
    JOptionPane.showMessageDialog(
        pai,
        texto == null ? "Não foi possível concluir a operação." : texto,
        "Não foi possível concluir",
        JOptionPane.ERROR_MESSAGE);
  }
}
