package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Sistema {

    private Scanner scan = new Scanner(System.in);

    private int opcao = 0;

    private List<Livro> livros = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();


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
        scan.next();
            nome = scan.nextLine();

        Livro livro = new Livro(id, nome);
            livros.add(livro);
    }

    private void cadastrarCliente() {
        int id = clientes.size() + 1;
        String nome = "";

        print("Digite o nome do cliente: ");
        scan.next();
            nome = scan.nextLine();

        Cliente cliente = new Cliente(id, nome);
            clientes.add(cliente);
    }

    public void iniciar() {
        bemVindo();
        opcoes();


    }

}
