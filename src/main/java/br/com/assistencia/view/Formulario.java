package br.com.assistencia.view;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Formulario extends JPanel {
  private final Map<String, JComponent> campos = new LinkedHashMap<>();
  private int linha;

  public Formulario() {
    super(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
  }

  private void adicionar(String chave, String titulo, JComponent campo, JComponent visual) {
    campos.put(chave, campo);
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = linha++;
    g.weightx = 1;
    g.fill = GridBagConstraints.HORIZONTAL;
    g.anchor = GridBagConstraints.NORTHWEST;
    g.insets = new Insets(8, 0, 3, 0);
    add(new JLabel(titulo), g);
    g.gridy = linha++;
    g.insets = new Insets(0, 0, 2, 0);
    add(visual, g);
  }

  public void campoTexto(String chave, String titulo) {
    JTextField f = new JTextField(23);
    adicionar(chave, titulo, f, f);
  }

  public void area(String chave, String titulo) {
    JTextArea f = new JTextArea(3, 22);
    f.setLineWrap(true);
    f.setWrapStyleWord(true);
    adicionar(chave, titulo, f, new JScrollPane(f));
  }

  public void combo(String chave, String titulo) {
    JComboBox<Object> f = new JComboBox<>();
    f.setPrototypeDisplayValue("Selecione uma opção                        ");
    adicionar(chave, titulo, f, f);
  }

  public void marcado(String chave, String titulo) {
    JCheckBox f = new JCheckBox(titulo);
    adicionar(chave, "", f, f);
  }

  public String texto(String chave) {
    JComponent f = campos.get(chave);
    return f instanceof JTextField t ? t.getText() : ((JTextArea) f).getText();
  }

  public void texto(String chave, String valor) {
    JComponent f = campos.get(chave);
    if (f instanceof JTextField t) t.setText(valor == null ? "" : valor);
    else ((JTextArea) f).setText(valor == null ? "" : valor);
  }

  public boolean marcado(String chave) {
    return ((JCheckBox) campos.get(chave)).isSelected();
  }

  public void marcar(String chave, boolean valor) {
    ((JCheckBox) campos.get(chave)).setSelected(valor);
  }

  public Object selecionado(String chave) {
    return ((JComboBox<?>) campos.get(chave)).getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  public void opcoes(String chave, List<?> dados) {
    JComboBox<Object> c = (JComboBox<Object>) campos.get(chave);
    c.removeAllItems();
    for (Object item : dados) c.addItem(item);
    c.setSelectedIndex(-1);
  }

  public void selecionar(String chave, Object valor) {
    ((JComboBox<?>) campos.get(chave)).setSelectedItem(valor);
  }

  public void habilitar(String chave, boolean sim) {
    campos.get(chave).setEnabled(sim);
  }

  public void habilitarTodos(boolean sim) {
    campos.values().forEach(c -> c.setEnabled(sim));
  }

  public void limpar() {
    for (JComponent c : campos.values()) {
      c.setEnabled(true);
      if (c instanceof JTextField t) t.setText("");
      else if (c instanceof JTextArea t) t.setText("");
      else if (c instanceof JCheckBox t) t.setSelected(true);
      else if (c instanceof JComboBox<?> t) t.setSelectedIndex(-1);
    }
  }
}
