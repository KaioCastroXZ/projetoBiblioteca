package main.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {
    T inserir(T entidade) throws SQLException;

    Optional<T> buscarPorId(int id) throws SQLException;

    List<T> listarTodos() throws SQLException;

    boolean atualizar(T entidade) throws SQLException;

    boolean deletar(int id) throws SQLException;
}
