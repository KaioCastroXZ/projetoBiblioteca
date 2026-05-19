package main.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void inicializar() {
        String criarTabelaLivros = """
                CREATE TABLE IF NOT EXISTS livro (
                    id SERIAL PRIMARY KEY,
                    nome VARCHAR(255) NOT NULL,
                    disponivel BOOLEAN NOT NULL DEFAULT TRUE
                )
                """;

        String criarTabelaUsuarios = """
                CREATE TABLE IF NOT EXISTS usuario (
                    id SERIAL PRIMARY KEY,
                    nome VARCHAR(255) NOT NULL
                )
                """;

        String criarTabelaEmprestimos = """
                CREATE TABLE IF NOT EXISTS emprestimo (
                    id SERIAL PRIMARY KEY,
                    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
                    livro_id INTEGER NOT NULL REFERENCES livro(id),
                    data_retirada TIMESTAMP NOT NULL,
                    data_devolucao TIMESTAMP NOT NULL,
                    devolvido BOOLEAN NOT NULL DEFAULT FALSE
                )
                """;

        String criarTabelaMultas = """
                CREATE TABLE IF NOT EXISTS multa (
                    id SERIAL PRIMARY KEY,
                    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
                    valor DOUBLE PRECISION NOT NULL,
                    paga BOOLEAN NOT NULL DEFAULT FALSE
                )
                """;

        try (Connection conn = Conexao.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(criarTabelaLivros);
            stmt.execute(criarTabelaUsuarios);
            stmt.execute(criarTabelaEmprestimos);
            stmt.execute(criarTabelaMultas);
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao inicializar banco de dados.", e);
        }
    }
}
