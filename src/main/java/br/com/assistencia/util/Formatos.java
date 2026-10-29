package br.com.assistencia.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.*;
import java.util.Locale;

public final class Formatos {
  private Formatos() {}

  public static final DateTimeFormatter DATA =
      DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

  public static String moeda(BigDecimal valor) {
    return valor == null
        ? "-"
        : NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
  }

  public static String data(LocalDate valor) {
    return valor == null ? "" : DATA.format(valor);
  }

  public static String dataHora(LocalDateTime valor) {
    return valor == null ? "" : valor.format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"));
  }

  public static LocalDate lerData(String texto, boolean obrigatoria) {
    if (texto == null || texto.isBlank()) {
      if (obrigatoria) throw new IllegalArgumentException("Informe as datas inicial e final.");
      return null;
    }
    try {
      LocalDate d = LocalDate.parse(texto.trim(), DATA);
      if (d.getYear() < 1000 || d.getYear() > 9998) throw new DateTimeException("Ano inválido");
      return d;
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Data inválida. Utilize dd/mm/aaaa.");
    }
  }

  public static BigDecimal valor(String texto, boolean obrigatorio) {
    if (texto == null || texto.isBlank()) {
      if (obrigatorio) throw new IllegalArgumentException("Informe o valor.");
      return null;
    }
    try {
      String s = texto.trim();
      if (s.contains(",")) s = s.replace(".", "").replace(",", ".");
      BigDecimal n = new BigDecimal(s).setScale(2, java.math.RoundingMode.UNNECESSARY);
      if (n.signum() < 0 || n.compareTo(new BigDecimal("99999999.99")) > 0)
        throw new NumberFormatException();
      return n;
    } catch (NumberFormatException | ArithmeticException e) {
      throw new IllegalArgumentException(
          "Valor inválido. Use, por exemplo, 150,00 e no máximo duas casas decimais.");
    }
  }

  public static String numero(BigDecimal valor) {
    return valor == null ? "" : valor.toPlainString().replace('.', ',');
  }

  public static String opcional(String s) {
    return s == null || s.isBlank() ? null : s.trim();
  }
}
