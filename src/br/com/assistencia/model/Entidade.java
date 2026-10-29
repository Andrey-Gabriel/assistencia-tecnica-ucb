package br.com.assistencia.model;

import java.util.Objects;

public abstract class Entidade {
  private Integer id;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    return o != null && getClass() == o.getClass() && id != null && id.equals(((Entidade) o).id);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), id);
  }
}
