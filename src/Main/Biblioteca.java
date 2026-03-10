package Main;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Biblioteca {

    private List<Livro> livros;
    private static final int quantidadeFixaDias = 14;
    private Date dataDevolucao;
    private Date dataRetirada;
    private boolean multa;

    public Biblioteca(){}

    public Biblioteca(List<Livro> livros, Date dataDevolucao, Date dataRetirada, boolean multa){
        this.livros = livros;
        this.dataDevolucao = dataDevolucao;
        this.dataRetirada = dataRetirada;
        this.multa = multa;
    }

    public List<Livro> getLivros() { return livros; }

    public void setLivros(List<Livro> livros) {
        this.livros = livros;
    }

    public Date getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(Date dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public Date getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(Date dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public boolean getMulta() {
        return multa;
    }

    public void setMulta(boolean multa) {
        this.multa = multa;
    }


    public Date calcularDataDevolucao() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataRetirada);
        cal.add(Calendar.DAY_OF_MONTH, quantidadeFixaDias);
        return cal.getTime();
    }

    public void verificarMulta() {
        multa = dataDevolucao.compareTo(calcularDataDevolucao()) > 0;
    }
}