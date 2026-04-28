package main.dao;

import main.model.Emprestimo;

import java.sql.SQLException;
import java.util.List;

public interface EmprestimoDAO extends CrudDAO<Emprestimo> {
    List<Emprestimo> listarPorUsuario(int usuarioId) throws SQLException;

    List<Emprestimo> listarAbertosPorUsuario(int usuarioId) throws SQLException;
}
