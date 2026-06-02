package main.model;

public class Multa {
    private int id;
    private int usuarioId;
    private double valor;
    private boolean paga;

    public Multa(int id, double valor) {
        this(id, 0, valor);
    }

    public Multa(int id, int usuarioId, double valor) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.valor = valor;
        this.paga = false;
    }

    public int getId() {
        return id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public double getValor() {
        return valor;
    }

    public boolean isPaga() {
        return paga;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void pagar() {
        this.paga = true;
    }
}
