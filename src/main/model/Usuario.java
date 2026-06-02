package main.model;

public abstract class Usuario {

    private int id;
    private String nome;
    private final TipoUsuario tipo;

    protected Usuario(int id, String nome, TipoUsuario tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
