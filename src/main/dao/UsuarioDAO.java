package main.dao;

import main.model.Administrador;
import main.model.Cliente;
import main.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UsuarioDAO extends CrudDAO<Cliente> {
    Administrador inserirAdministrador(Administrador administrador) throws SQLException;

    Optional<Usuario> buscarUsuarioPorId(int id) throws SQLException;

    Optional<Administrador> buscarAdministradorPorId(int id) throws SQLException;

    List<Usuario> listarUsuarios() throws SQLException;

    boolean atualizarUsuario(Usuario usuario) throws SQLException;
}
