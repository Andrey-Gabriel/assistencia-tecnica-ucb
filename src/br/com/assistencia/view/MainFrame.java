package br.com.assistencia.view;

import br.com.assistencia.controller.*;
import br.com.assistencia.dao.*;
import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.service.RelatorioPdfService;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainFrame extends JFrame {
  private final JTabbedPane abas = new JTabbedPane();
  private final Runnable[] carregar;

  public MainFrame(ConnectionFactory factory) {
    super("Assistência Técnica - Gestão de atendimentos");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(1100, 720));
    Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(Math.min(1380, tela.width - 60), Math.min(860, tela.height - 60));
    setLocationRelativeTo(null);
    ClienteDAO clientes = new ClienteDAO(factory);
    TecnicoDAO tecnicos = new TecnicoDAO(factory);
    EquipamentoDAO equipamentos = new EquipamentoDAO(factory);
    ServicoDAO servicos = new ServicoDAO(factory);
    OrdemServicoDAO ordens = new OrdemServicoDAO(factory);
    ClienteView cv = new ClienteView();
    TecnicoView tv = new TecnicoView();
    EquipamentoView ev = new EquipamentoView();
    ServicoView sv = new ServicoView();
    OrdemServicoView ov = new OrdemServicoView();
    RelatorioView rv = new RelatorioView();
    ClienteController cc = new ClienteController(cv, clientes);
    TecnicoController tc = new TecnicoController(tv, tecnicos);
    EquipamentoController ec = new EquipamentoController(ev, equipamentos, clientes);
    ServicoController sc = new ServicoController(sv, servicos);
    OrdemServicoController oc =
        new OrdemServicoController(ov, ordens, equipamentos, tecnicos, servicos);
    RelatorioController rc =
        new RelatorioController(rv, ordens, tecnicos, new RelatorioPdfService());
    carregar =
        new Runnable[] {
          cc::atualizar, tc::atualizar, ec::atualizar, sc::atualizar, oc::atualizar, rc::atualizar
        };
    abas.addTab("Clientes", cv);
    abas.addTab("Técnicos", tv);
    abas.addTab("Equipamentos", ev);
    abas.addTab("Serviços", sv);
    abas.addTab("Ordens de serviço", ov);
    abas.addTab("Relatório de OS", rv);
    abas.addChangeListener(e -> carregar[abas.getSelectedIndex()].run());
    JPanel topo = new JPanel(new BorderLayout(12, 0));
    topo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
    JLabel titulo = new JLabel("Sistema de Assistência Técnica");
    titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20));
    topo.add(titulo, BorderLayout.WEST);
    JButton conexao = new JButton("Conexão com o banco");
    conexao.addActionListener(
        e ->
            new ConexaoDialog(this, factory, () -> carregar[abas.getSelectedIndex()].run())
                .setVisible(true));
    topo.add(conexao, BorderLayout.EAST);
    add(topo, BorderLayout.NORTH);
    add(abas, BorderLayout.CENTER);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowOpened(WindowEvent e) {
            if (factory.get("db.password").isEmpty()) {
              new ConexaoDialog(
                      MainFrame.this, factory, () -> carregar[abas.getSelectedIndex()].run())
                  .setVisible(true);
            } else carregar[abas.getSelectedIndex()].run();
          }
        });
  }
}
