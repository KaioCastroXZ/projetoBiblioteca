package main.model;

import java.util.Date;

public class Emprestimo {
    private int id;
    private Livro livro;
    private Date dataRetirada;
    private Date dataDevolucao;
    private boolean devolvido;

    public Emprestimo(int id, Livro livro, Date dataRetirada, Date dataDevolucao) {
        this.id = id;
        this.livro = livro;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.devolvido = false;
    }

    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public Date getDataRetirada() {
        return dataRetirada;
    }

    public Date getDataDevolucao() {
        return dataDevolucao;
    }

    public boolean isDevolvido() {
        return devolvido;
    }

    public void marcarDevolvido() {
        this.devolvido = true;
    }
}
