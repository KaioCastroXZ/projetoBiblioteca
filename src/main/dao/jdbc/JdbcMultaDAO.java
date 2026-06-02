package main.dao.jdbc;

import main.dao.MultaDAO;
import main.database.Conexao;
import main.model.Multa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMultaDAO implements MultaDAO {

    @Override
    public Multa inserir(int usuarioId, double valor) throws SQLException {
        String sql = "INSERT INTO multa (usuario_id, valor, paga) VALUES (?, ?, FALSE) RETURNING id, paga";
        Multa multa = new Multa(0, usuarioId, valor);
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDouble(2, valor);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    multa.setId(rs.getInt("id"));
                    if (rs.getBoolean("paga")) {
                        multa.pagar();
                    }
                }
            }
        }
        return multa;
    }

    @Override
    public Optional<Multa> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, usuario_id, valor, paga FROM multa WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMulta(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Multa> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT id, usuario_id, valor, paga FROM multa WHERE usuario_id = ? ORDER BY id";
        List<Multa> multas = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapMulta(rs));
                }
            }
        }
        return multas;
    }

    @Override
    public List<Multa> listarTodos() throws SQLException {
        String sql = "SELECT id, usuario_id, valor, paga FROM multa ORDER BY id";
        List<Multa> multas = new ArrayList<>();
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                multas.add(mapMulta(rs));
            }
        }
        return multas;
    }

    @Override
    public boolean atualizar(Multa multa) throws SQLException {
        String sql = "UPDATE multa SET valor = ?, paga = ? WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, multa.getValor());
            stmt.setBoolean(2, multa.isPaga());
            stmt.setInt(3, multa.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM multa WHERE id = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Multa mapMulta(ResultSet rs) throws SQLException {
        Multa multa = new Multa(rs.getInt("id"), rs.getInt("usuario_id"), rs.getDouble("valor"));
        if (rs.getBoolean("paga")) {
            multa.pagar();
        }
        return multa;
    }
}
