package br.com.assistencia;

import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.view.MainFrame;
import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new MainFrame(new ConnectionFactory()).setVisible(true);
          } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Não foi possível iniciar: " + e.getMessage(),
                "Assistência Técnica",
                JOptionPane.ERROR_MESSAGE);
          }
        });
  }
}
