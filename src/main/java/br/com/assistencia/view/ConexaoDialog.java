package br.com.assistencia.view;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.util.TarefaUI;
import java.awt.*;
import java.sql.DriverManager;
import javax.swing.*;

public class ConexaoDialog extends JDialog {
  public ConexaoDialog(JFrame pai, ConnectionFactory factory, Runnable aoSalvar) {
    super(pai, "Conexão com o MySQL", true);
    JPanel painel = new JPanel(new BorderLayout(10, 12));
    painel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    JPanel campos = new JPanel(new GridLayout(0, 1, 0, 5));
    JTextField url = new JTextField(factory.get("db.url"), 68),
        usuario = new JTextField(factory.get("db.user"));
    JPasswordField senha = new JPasswordField(factory.get("db.password"));
    campos.add(new JLabel("URL JDBC do banco"));
    campos.add(url);
    campos.add(new JLabel("Usuário"));
    campos.add(usuario);
    campos.add(new JLabel("Senha"));
    campos.add(senha);
    painel.add(campos);
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton testar = new JButton("Testar conexão"),
        salvar = new JButton("Salvar"),
        cancelar = new JButton("Cancelar");
    botoes.add(testar);
    botoes.add(salvar);
    botoes.add(cancelar);
    painel.add(botoes, BorderLayout.SOUTH);
    testar.addActionListener(
        e -> {
          String u = url.getText().trim(),
              nome = usuario.getText().trim(),
              segredo = new String(senha.getPassword());
          TarefaUI.executar(
              painel,
              () -> {
                try (var c = DriverManager.getConnection(u, nome, segredo);
                    var p = c.prepareStatement("SELECT COUNT(*) FROM ordem_servico")) {
                  try (var r = p.executeQuery()) {
                    r.next();
                    return r.getInt(1);
                  }
                }
              },
              qtd ->
                  JOptionPane.showMessageDialog(
                      this, "Conexão funcionando. " + qtd + " ordens de serviço encontradas."));
        });
    salvar.addActionListener(
        e -> {
          try {
            factory.configurar(url.getText(), usuario.getText(), new String(senha.getPassword()));
            dispose();
            aoSalvar.run();
          } catch (Exception erro) {
            TarefaUI.erro(this, erro);
          }
        });
    cancelar.addActionListener(e -> dispose());
    setContentPane(painel);
    pack();
    setLocationRelativeTo(pai);
  }
}
