package main.dao.jdbc;

import main.dao.UsuarioDAO;
import main.database.Conexao;
import main.model.Administrador;
import main.model.Cliente;
import main.model.TipoUsuario;
import main.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUsuarioDAO implements UsuarioDAO {

    @Override
    public Cliente inserir(Cliente usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, tipo) VALUES (?, ?) RETURNING id";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, TipoUsuario.CLIENTE.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt("id"));
                }
            }
        }
        return usuario;
    }

    @Override
    public Administrador inserirAdministrador(Administrador administrador) throws SQLException {
        String sql = "INSERT INTO usuario (nome, tipo) VALUES (?, ?) RETURNING id";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, administrador.getNome());
            stmt.setString(2, TipoUsuario.ADMINISTRADOR.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    administrador.setId(rs.getInt("id"));
                }
            }
        }
        return administrador;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome FROM usuario WHERE id = ? AND tipo = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, TipoUsuario.CLIENTE.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarUsuarioPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, tipo FROM usuario WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Administrador> buscarAdministradorPorId(int id) throws SQLException {
        String sql = "SELECT id, nome FROM usuario WHERE id = ? AND tipo = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, TipoUsuario.ADMINISTRADOR.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Administrador(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT id, nome FROM usuario WHERE tipo = ? ORDER BY id";
        List<Cliente> usuarios = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, TipoUsuario.CLIENTE.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapCliente(rs));
                }
            }
        }
        return usuarios;
    }

    @Override
    public List<Usuario> listarUsuarios() throws SQLException {
        String sql = "SELECT id, nome, tipo FROM usuario ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapUsuario(rs));
            }
        }
        return usuarios;
    }

    @Override
    public boolean atualizar(Cliente usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ? WHERE id = ? AND tipo = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setInt(2, usuario.getId());
            stmt.setString(3, TipoUsuario.CLIENTE.name());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean atualizarUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ? WHERE id = ? AND tipo = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setInt(2, usuario.getId());
            stmt.setString(3, usuario.getTipo().name());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nome"));
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        TipoUsuario tipo = lerTipo(rs.getString("tipo"));
        if (tipo == TipoUsuario.ADMINISTRADOR) {
            return new Administrador(rs.getInt("id"), rs.getString("nome"));
        }
        return new Cliente(rs.getInt("id"), rs.getString("nome"));
    }

    private TipoUsuario lerTipo(String tipo) {
        try {
            return TipoUsuario.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            return TipoUsuario.CLIENTE;
        }
    }
}
