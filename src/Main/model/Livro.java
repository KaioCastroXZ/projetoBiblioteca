package main.model;

public class Livro {

    private int id;
    private String nome;
    private boolean disponivel = true;

    public Livro(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public void marcarEmprestado() {
        this.disponivel = false;
    }

    public void marcarDevolvido() {
        this.disponivel = true;
    }
}
