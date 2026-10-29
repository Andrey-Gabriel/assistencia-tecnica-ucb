package br.com.assistencia.view;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.util.TarefaUI;
import java.awt.*;
import javax.swing.*;

public class ConexaoDialog extends JDialog {
  public ConexaoDialog(JFrame pai, ConnectionFactory factory, Runnable aoSalvar) {
    super(pai, "Conexão com o MySQL", true);
    factory.recarregar();
    JPanel painel = new JPanel(new BorderLayout(10, 12));
    painel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    painel.add(
        new JLabel("Informe os dados do seu servidor MySQL e teste antes de salvar."),
        BorderLayout.NORTH);
    JPanel campos = new JPanel(new GridLayout(0, 1, 0, 5));
    JTextField url = new JTextField(factory.get("db.url"), 68),
        usuario = new JTextField(factory.get("db.user"));
    JPasswordField senha = new JPasswordField(factory.get("db.password"));
    campos.add(new JLabel("URL JDBC do banco"));
    campos.add(url);
    campos.add(new JLabel("Usuário"));
    campos.add(usuario);
    campos.add(new JLabel("Senha do MySQL"));
    campos.add(senha);
    JTextField arquivo = new JTextField(factory.getArquivoConfiguracao().toString());
    arquivo.setEditable(false);
    campos.add(
        new JLabel("Arquivo de configuração em uso (a senha é salva somente neste arquivo)"));
    campos.add(arquivo);
    painel.add(campos);
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    JButton testar = new JButton("Testar conexão"),
        salvar = new JButton("Testar e salvar"),
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
                try (var c = factory.conectar(u, nome, segredo);
                    var p = c.prepareStatement("SELECT COUNT(*) FROM ordem_servico");
                    var rs = p.executeQuery()) {
                  rs.next();
                  return rs.getInt(1);
                }
              },
              qtd ->
                  JOptionPane.showMessageDialog(
                      this,
                      "Conexão funcionando. "
                          + qtd
                          + " ordens de serviço encontradas. Para guardar, clique em Testar e"
                          + " salvar."));
        });
    salvar.addActionListener(
        e -> {
          String u = url.getText().trim(),
              nome = usuario.getText().trim(),
              segredo = new String(senha.getPassword());
          TarefaUI.executar(
              painel,
              () -> {
                factory.configurar(u, nome, segredo);
                return true;
              },
              ok -> {
                dispose();
                aoSalvar.run();
              });
        });
    cancelar.addActionListener(e -> dispose());
    setContentPane(painel);
    getRootPane().setDefaultButton(salvar);
    pack();
    setLocationRelativeTo(pai);
  }
}
