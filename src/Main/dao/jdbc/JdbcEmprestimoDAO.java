package main.dao.jdbc;

import main.dao.EmprestimoDAO;
import main.database.Conexao;
import main.model.Emprestimo;
import main.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmprestimoDAO implements EmprestimoDAO {

    @Override
    public Emprestimo inserir(Emprestimo emprestimo) throws SQLException {
        String sql = """
                INSERT INTO emprestimo (usuario_id, livro_id, data_retirada, data_devolucao, devolvido)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, emprestimo.getClienteId());
            stmt.setInt(2, emprestimo.getLivro().getId());
            stmt.setTimestamp(3, new Timestamp(emprestimo.getDataRetirada().getTime()));
            stmt.setTimestamp(4, new Timestamp(emprestimo.getDataDevolucao().getTime()));
            stmt.setBoolean(5, emprestimo.isDevolvido());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Emprestimo(
                            rs.getInt("id"),
                            emprestimo.getClienteId(),
                            emprestimo.getLivro(),
                            emprestimo.getDataRetirada(),
                            emprestimo.getDataDevolucao()
                    );
                }
            }
        }
        return emprestimo;
    }

    @Override
    public Optional<Emprestimo> buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT e.id,
                       e.usuario_id,
                       e.data_retirada,
                       e.data_devolucao,
                       e.devolvido,
                       l.id AS livro_id,
                       l.nome AS livro_nome,
                       l.disponivel AS livro_disponivel
                FROM emprestimo e
                JOIN livro l ON l.id = e.livro_id
                WHERE e.id = ?
                """;

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEmprestimo(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Emprestimo> listarTodos() throws SQLException {
        String sql = """
                SELECT e.id,
                       e.usuario_id,
                       e.data_retirada,
                       e.data_devolucao,
                       e.devolvido,
                       l.id AS livro_id,
                       l.nome AS livro_nome,
                       l.disponivel AS livro_disponivel
                FROM emprestimo e
                JOIN livro l ON l.id = e.livro_id
                ORDER BY e.id
                """;
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                emprestimos.add(mapEmprestimo(rs));
            }
        }
        return emprestimos;
    }

    @Override
    public boolean atualizar(Emprestimo emprestimo) throws SQLException {
        String sql = """
                UPDATE emprestimo
                SET usuario_id = ?, livro_id = ?, data_retirada = ?, data_devolucao = ?, devolvido = ?
                WHERE id = ?
                """;

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, emprestimo.getClienteId());
            stmt.setInt(2, emprestimo.getLivro().getId());
            stmt.setTimestamp(3, new Timestamp(emprestimo.getDataRetirada().getTime()));
            stmt.setTimestamp(4, new Timestamp(emprestimo.getDataDevolucao().getTime()));
            stmt.setBoolean(5, emprestimo.isDevolvido());
            stmt.setInt(6, emprestimo.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM emprestimo WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Emprestimo> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = """
                SELECT e.id,
                       e.usuario_id,
                       e.data_retirada,
                       e.data_devolucao,
                       e.devolvido,
                       l.id AS livro_id,
                       l.nome AS livro_nome,
                       l.disponivel AS livro_disponivel
                FROM emprestimo e
                JOIN livro l ON l.id = e.livro_id
                WHERE e.usuario_id = ?
                ORDER BY e.id
                """;
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(mapEmprestimo(rs));
                }
            }
        }
        return emprestimos;
    }

    @Override
    public List<Emprestimo> listarAbertosPorUsuario(int usuarioId) throws SQLException {
        String sql = """
                SELECT e.id,
                       e.usuario_id,
                       e.data_retirada,
                       e.data_devolucao,
                       e.devolvido,
                       l.id AS livro_id,
                       l.nome AS livro_nome,
                       l.disponivel AS livro_disponivel
                FROM emprestimo e
                JOIN livro l ON l.id = e.livro_id
                WHERE e.usuario_id = ? AND e.devolvido = FALSE
                ORDER BY e.id
                """;
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(mapEmprestimo(rs));
                }
            }
        }
        return emprestimos;
    }

    private Emprestimo mapEmprestimo(ResultSet rs) throws SQLException {
        Livro livro = new Livro(rs.getInt("livro_id"), rs.getString("livro_nome"));
        livro.setDisponivel(rs.getBoolean("livro_disponivel"));

        Emprestimo emprestimo = new Emprestimo(
                rs.getInt("id"),
                rs.getInt("usuario_id"),
                livro,
                rs.getTimestamp("data_retirada"),
                rs.getTimestamp("data_devolucao")
        );
        emprestimo.setDevolvido(rs.getBoolean("devolvido"));
        return emprestimo;
    }
}
