package br.com.assistencia.dao;

import br.com.assistencia.model.Entidade;
import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T extends Entidade> {
  List<T> listar() throws SQLException;

  void inserir(T entidade) throws SQLException;

  void alterar(T entidade) throws SQLException;

  void excluir(int id) throws SQLException;
}
