package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Sistema {

    private Scanner scan = new Scanner(System.in);

    private int opcao = 0;

    private List<Livro> livros = new ArrayList<>();

    public Sistema() {
    }

    private void print(String str) {
        System.out.print(str);
    }

    private void println(String str) {
        System.out.println(str);
    }

    private void bemVindo() {
        println("----=--------=----------=---------=----");
        println("Bem-Vindo ao sistema de biblioteca");
        println("----=--------=----------=---------=----");

        opcoes();
    }

    private void opcoes() {
        println("Digite:\n 1 para cadastrar livro\n 2 para cadastrar cliente");
        println("----=--------=----------=---------=----");
        switch (opcao) {
            case 1:
                cadastrarLivro();
                break;
            case 2:
                cadastrarCliente();
                break;
        }

    }

    private void cadastrarLivro() {
        int id = livros.size() + 1;
        String nome = "";


        print("Digite o nome do livro: ");
        nome = scan.next();

        Livro livro = new Livro(id, nome);
        livros.add(livro);
    }

    private void cadastrarCliente() {
    }

    public void iniciar() {
        bemVindo();

    }

}
