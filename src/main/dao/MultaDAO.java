package main.dao;

import main.model.Multa;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MultaDAO {
    Multa inserir(int usuarioId, double valor) throws SQLException;

    Optional<Multa> buscarPorId(int id) throws SQLException;

    List<Multa> listarPorUsuario(int usuarioId) throws SQLException;

    List<Multa> listarTodos() throws SQLException;

    boolean atualizar(Multa multa) throws SQLException;

    boolean deletar(int id) throws SQLException;
}
