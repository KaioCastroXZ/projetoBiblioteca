package main;
import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private int id;
    private String nome;

    private List<Multa> multas = new ArrayList<>();

    private Cliente(){}

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
}
