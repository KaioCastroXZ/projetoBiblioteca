package main.model;

public class Multa {
    private int id;
    private double valor;
    private boolean paga;

    public Multa(int id, double valor) {
        this.id = id;
        this.valor = valor;
        this.paga = false;
    }

    public int getId() {
        return id;
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

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void pagar() {
        this.paga = true;
    }
}
