package main.model;

import java.util.Date;

public class Emprestimo {
    private int id;
    private int clienteId;
    private Livro livro;
    private Date dataRetirada;
    private Date dataDevolucao;
    private boolean devolvido;

    public Emprestimo(int id, int clienteId, Livro livro, Date dataRetirada, Date dataDevolucao) {
        this.id = id;
        this.clienteId = clienteId;
        this.livro = livro;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.devolvido = false;
    }

    public Emprestimo(int id, Livro livro, Date dataRetirada, Date dataDevolucao) {
        this(id, 0, livro, dataRetirada, dataDevolucao);
    }

    public int getId() {
        return id;
    }

    public Livro getLivro() {
        return livro;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public Date getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(Date dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public Date getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(Date dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public boolean isDevolvido() {
        return devolvido;
    }

    public void setDevolvido(boolean devolvido) {
        this.devolvido = devolvido;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public void marcarDevolvido() {
        this.devolvido = true;
    }
}
