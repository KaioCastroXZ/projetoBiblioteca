package main;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args){
        Livro livro = new Livro(1, "Biblia");
        List<Livro> livros = new ArrayList<>();
        livros.add(livro);

        Calendar calRetirada = Calendar.getInstance();

        calRetirada.set(2026, Calendar.FEBRUARY, 24);
        Date dataRetirada = calRetirada.getTime();

        Calendar calDevolucao = Calendar.getInstance();

        calDevolucao.set(2026, Calendar.MARCH, 20);
        Date dataDevolucao = calDevolucao.getTime();

        Biblioteca biblioteca = new Biblioteca(livros, dataDevolucao, dataRetirada, false );

        biblioteca.verificarMulta();

        if(biblioteca.getMulta()) {
            System.out.println("Houve multa");
        }else
        {
            System.out.println("Não houve multa");
        }
    }
}
