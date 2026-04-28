package main.dao.jdbc;

import main.dao.UsuarioDAO;
import main.database.Conexao;
import main.model.Cliente;

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
        String sql = "INSERT INTO usuario (nome) VALUES (?) RETURNING id";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt("id"));
                }
            }
        }
        return usuario;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome FROM usuario WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(rs.getInt("id"), rs.getString("nome")));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT id, nome FROM usuario ORDER BY id";
        List<Cliente> usuarios = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Cliente(rs.getInt("id"), rs.getString("nome")));
            }
        }
        return usuarios;
    }

    @Override
    public boolean atualizar(Cliente usuario) throws SQLException {
        String sql = "UPDATE usuario SET nome = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setInt(2, usuario.getId());
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
}
