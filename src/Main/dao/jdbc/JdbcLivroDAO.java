package main.dao.jdbc;

import main.dao.LivroDAO;
import main.database.Conexao;
import main.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLivroDAO implements LivroDAO {

    @Override
    public Livro inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (nome, disponivel) VALUES (?, ?) RETURNING id";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, livro.getNome());
            stmt.setBoolean(2, livro.isDisponivel());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    livro.setId(rs.getInt("id"));
                }
            }
        }
        return livro;
    }

    @Override
    public Optional<Livro> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, disponivel FROM livro WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Livro livro = new Livro(rs.getInt("id"), rs.getString("nome"));
                    livro.setDisponivel(rs.getBoolean("disponivel"));
                    return Optional.of(livro);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Livro> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, disponivel FROM livro ORDER BY id";
        List<Livro> livros = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Livro livro = new Livro(rs.getInt("id"), rs.getString("nome"));
                livro.setDisponivel(rs.getBoolean("disponivel"));
                livros.add(livro);
            }
        }
        return livros;
    }

    @Override
    public boolean atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET nome = ?, disponivel = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, livro.getNome());
            stmt.setBoolean(2, livro.isDisponivel());
            stmt.setInt(3, livro.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM livro WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
