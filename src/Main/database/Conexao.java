package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL = getEnvOrDefault("DB_URL", "jdbc:postgresql://127.0.0.1:5432/meubanco");
    private static final String USUARIO = getEnvOrDefault("DB_USER", "postgres");
    private static final String SENHA = getEnvOrDefault("DB_PASSWORD", "0123456");

    private Conexao() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    private static String getEnvOrDefault(String envVar, String fallback) {
        String valor = System.getenv(envVar);
        if (valor == null || valor.isBlank()) {
            return fallback;
        }
        return valor;
    }
}
