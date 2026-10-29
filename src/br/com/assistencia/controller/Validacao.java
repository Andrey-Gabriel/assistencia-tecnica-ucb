package br.com.assistencia.controller;

public final class Validacao {
  private Validacao() {}

  public static String obrigatorio(String valor, String nome, int minimo, int maximo) {
    String s = valor == null ? "" : valor.trim();
    if (s.length() < minimo || s.length() > maximo)
      throw new IllegalArgumentException(
          nome + ": informe de " + minimo + " a " + maximo + " caracteres.");
    return s;
  }

  public static String opcional(String valor, String nome, int maximo) {
    if (valor == null || valor.isBlank()) return null;
    return obrigatorio(valor, nome, 1, maximo);
  }

  public static String telefone(String texto) {
    String s = obrigatorio(texto, "Telefone", 8, 20);
    int n = s.replaceAll("[^0-9]", "").length();
    if (n < 8 || n > 15) throw new IllegalArgumentException("Telefone deve ter de 8 a 15 dígitos.");
    return s;
  }

  public static String email(String texto) {
    String s = opcional(texto, "E-mail", 150);
    if (s != null && !s.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
      throw new IllegalArgumentException("Informe um e-mail válido.");
    return s;
  }

  public static String documento(String texto) {
    if (texto == null || texto.isBlank()) return null;
    if (!texto.matches("[0-9. /-]+"))
      throw new IllegalArgumentException("CPF/CNPJ deve conter números e pontuação válida.");
    String s = texto.replaceAll("[^0-9]", "");
    if ((s.length() != 11 && s.length() != 14) || s.chars().distinct().count() == 1)
      throw new IllegalArgumentException("CPF/CNPJ inválido.");
    int base = s.length() - 2;
    for (int etapa = 0; etapa < 2; etapa++) {
      int soma = 0;
      if (s.length() == 11) {
        for (int i = 0; i < base + etapa; i++) soma += (s.charAt(i) - '0') * (base + etapa + 1 - i);
      } else {
        int peso = 2;
        for (int i = base + etapa - 1; i >= 0; i--) {
          soma += (s.charAt(i) - '0') * peso;
          peso = peso == 9 ? 2 : peso + 1;
        }
      }
      int digito = 11 - soma % 11;
      if (digito >= 10) digito = 0;
      if (digito != s.charAt(base + etapa) - '0')
        throw new IllegalArgumentException("CPF/CNPJ com dígitos verificadores inválidos.");
    }
    return s;
  }

  public static <T> T escolhido(Object valor, Class<T> tipo, String nome) {
    if (!tipo.isInstance(valor)) throw new IllegalArgumentException("Selecione " + nome + ".");
    return tipo.cast(valor);
  }
}
