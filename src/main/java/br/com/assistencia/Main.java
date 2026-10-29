package br.com.assistencia;

/*
 * Java 17 ou superior. Importe o pom.xml como projeto Maven no IntelliJ.
 * Banco novo: execute sql/01 a sql/06, na ordem numérica, no MySQL Workbench.
 * Configure banco.properties ou utilize o botão "Conexão com o banco".
 * Compilar: mvn package
 * Executar: java -jar assistencia-tecnica.jar
 * Também é possível executar esta classe diretamente pela IDE.
 */
import br.com.assistencia.infra.ConnectionFactory;
import br.com.assistencia.view.MainFrame;
import java.awt.*;
import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
              if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
              }
            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 13));
            UIManager.put("nimbusBase", new Color(23, 91, 98));
            UIManager.put("nimbusSelectionBackground", new Color(23, 91, 98));
            UIManager.put("control", new Color(245, 248, 249));
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
