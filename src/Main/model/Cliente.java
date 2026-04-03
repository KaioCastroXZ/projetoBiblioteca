package main.model;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private int id;
    private String nome;
    private final List<Multa> multas = new ArrayList<>();
    private final List<Emprestimo> historicoEmprestimos = new ArrayList<>();

    public Cliente(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Multa> getMultas() {
        return multas;
    }

    public List<Emprestimo> getHistoricoEmprestimos() {
        return historicoEmprestimos;
    }

    public int registrarMulta(double valor) {
        int idMulta = multas.size() + 1;
        multas.add(new Multa(idMulta, valor));
        return idMulta;
    }

    public double getValorMultasPendentes() {
        double total = 0.0;
        for (Multa multa : multas) {
            if (!multa.isPaga()) {
                total += multa.getValor();
            }
        }
        return total;
    }

    public boolean temMultasPendentes() {
        return getValorMultasPendentes() > 0.0;
    }

    public boolean pagarMulta(int idMulta) {
        for (Multa multa : multas) {
            if (multa.getId() == idMulta && !multa.isPaga()) {
                multa.pagar();
                return true;
            }
        }
        return false;
    }

    public void registrarEmprestimo(Emprestimo emprestimo) {
        historicoEmprestimos.add(emprestimo);
    }

    public Emprestimo buscarEmprestimoPorId(int emprestimoId) {
        for (Emprestimo emprestimo : historicoEmprestimos) {
            if (emprestimo.getId() == emprestimoId) {
                return emprestimo;
            }
        }
        return null;
    }

    public int getQuantidadeEmprestimosEmAberto() {
        int quantidade = 0;
        for (Emprestimo emprestimo : historicoEmprestimos) {
            if (!emprestimo.isDevolvido()) {
                quantidade++;
            }
        }
        return quantidade;
    }
}
