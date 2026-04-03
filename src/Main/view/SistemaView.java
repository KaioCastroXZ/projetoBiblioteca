package main.view;

import main.controller.BibliotecaController;
import main.exception.ClienteComMultasPendentesException;
import main.exception.ClienteNaoEncontradoException;
import main.exception.LivroIndisponivelException;
import main.exception.LivroNaoEncontradoException;
import main.model.Cliente;
import main.model.Emprestimo;
import main.model.Livro;
import main.model.Multa;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class SistemaView {

    private final BibliotecaController controller;
    private final Scanner scan = new Scanner(System.in);
    private final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    public SistemaView(BibliotecaController controller) {
        this.controller = controller;
    }

    public void iniciar() {
        bemVindo();
        while (true) {
            exibirMenu();
        }
    }

    private void exibirMenu() {
        subTitulo("Menu Principal");
        println(" 1) Cadastrar livro");
        println(" 2) Cadastrar cliente");
        println(" 3) Emprestar livro");
        println(" 4) Registrar multa");
        println(" 5) Pagar multa");
        println(" 6) Consultar historico de emprestimos");
        println(" 7) Listar clientes");
        println(" 8) Devolver livro");
        println(" 9) Listar multas do cliente");
        println("10) Listar livros");
        linha();

        int opcao = lerInteiro("Opcao: ");
        switch (opcao) {
            case 1:
                cadastrarLivro();
                break;
            case 2:
                cadastrarCliente();
                break;
            case 3:
                emprestarLivro();
                break;
            case 4:
                registrarMulta();
                break;
            case 5:
                pagarMulta();
                break;
            case 6:
                consultarHistorico();
                break;
            case 7:
                listarClientes();
                break;
            case 8:
                devolverLivro();
                break;
            case 9:
                listarMultas();
                break;
            case 10:
                listarLivros();
                break;
            default:
                println("Opcao invalida.");
                break;
        }
        vazio();
    }

    private void cadastrarLivro() {
        subTitulo("Cadastro de Livro");
        String nome = lerLinha("Digite o nome do livro: ");
        Livro livro = controller.cadastrarLivro(nome);
        println("Livro cadastrado. Id: " + livro.getId());
    }

    private void cadastrarCliente() {
        subTitulo("Cadastro de Cliente");
        String nome = lerLinha("Digite o nome do cliente: ");
        Cliente cliente = controller.cadastrarCliente(nome);
        println("Cliente cadastrado. Id: " + cliente.getId());
    }

    private void emprestarLivro() {
        subTitulo("Emprestimo de Livro");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        int livroId = lerInteiro("Digite o id do livro: ");

        try {
            Cliente cliente = controller.buscarClientePorId(clienteId);
            Emprestimo emprestimo = controller.emprestarLivro(clienteId, livroId);
            println("Emprestimo registrado com sucesso. Id: " + emprestimo.getId()
                    + " | Cliente: " + cliente.getNome()
                    + " | Livro: " + emprestimo.getLivro().getNome());
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            println(e.getMessage());
        }
    }

    private void registrarMulta() {
        subTitulo("Registro de Multa");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        String valorStr = lerLinha("Digite o valor da multa: ");

        try {
            double valor = Double.parseDouble(valorStr);
            if (valor <= 0.0) {
                println("Valor invalido.");
                return;
            }
            int idMulta = controller.registrarMulta(clienteId, valor);
            println("Multa registrada. Id: " + idMulta);
        } catch (NumberFormatException e) {
            println("Valor invalido.");
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void pagarMulta() {
        subTitulo("Pagamento de Multa");
        int clienteId = lerInteiro("Digite o id do cliente: ");

        try {
            Cliente cliente = controller.buscarClientePorId(clienteId);
            double pendentes = cliente.getValorMultasPendentes();
            if (pendentes <= 0.0) {
                println("Cliente nao possui multas pendentes.");
                return;
            }

            println("Multas pendentes:");
            for (Multa multa : cliente.getMultas()) {
                if (!multa.isPaga()) {
                    println("Id " + multa.getId() + " | Valor: " + multa.getValor());
                }
            }
            println("Total pendente: " + pendentes);

            int multaId = lerInteiro("Digite o id da multa para pagamento: ");
            boolean pago = controller.pagarMulta(clienteId, multaId);
            if (pago) {
                println("Multa paga.");
            } else {
                println("Multa nao encontrada ou ja paga.");
            }
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void listarMultas() {
        subTitulo("Multas do Cliente");
        int clienteId = lerInteiro("Digite o id do cliente: ");

        try {
            List<Multa> multas = controller.listarMultasDoCliente(clienteId);
            if (multas.isEmpty()) {
                println("Cliente nao possui multas.");
                return;
            }

            println("Multas cadastradas:");
            for (Multa multa : multas) {
                String status = multa.isPaga() ? "paga" : "pendente";
                println("Id " + multa.getId() + " | Valor: " + multa.getValor() + " | Status: " + status);
            }
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void consultarHistorico() {
        subTitulo("Historico de Emprestimos");
        int clienteId = lerInteiro("Digite o id do cliente: ");

        try {
            Cliente cliente = controller.buscarClientePorId(clienteId);
            List<Emprestimo> historico = controller.consultarHistorico(clienteId);
            if (historico.isEmpty()) {
                println("Cliente nao possui emprestimos.");
                return;
            }

            println("Historico de emprestimos do cliente " + cliente.getNome() + ":");
            for (Emprestimo emprestimo : historico) {
                String status = emprestimo.isDevolvido() ? "devolvido" : "em aberto";
                println(
                        "Id " + emprestimo.getId()
                                + " | Livro: " + emprestimo.getLivro().getNome()
                                + " | Retirada: " + formatarData(emprestimo.getDataRetirada())
                                + " | Devolucao: " + formatarData(emprestimo.getDataDevolucao())
                                + " | Status: " + status
                );
            }
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void listarClientes() {
        subTitulo("Clientes Cadastrados");
        List<Cliente> clientes = controller.listarClientes();
        if (clientes.isEmpty()) {
            println("Nenhum cliente cadastrado.");
            return;
        }

        println("Clientes cadastrados:");
        for (Cliente cliente : clientes) {
            println(
                    "Id " + cliente.getId()
                            + " | Nome: " + cliente.getNome()
                            + " | Multas pendentes: " + cliente.getValorMultasPendentes()
                            + " | Emprestimos em aberto: " + cliente.getQuantidadeEmprestimosEmAberto()
            );
        }
    }

    private void listarLivros() {
        subTitulo("Livros Cadastrados");
        List<Livro> livros = controller.listarLivros();
        if (livros.isEmpty()) {
            println("Nenhum livro cadastrado.");
            return;
        }

        for (Livro livro : livros) {
            String status = livro.isDisponivel() ? "disponivel" : "emprestado";
            println("Id " + livro.getId() + " | Nome: " + livro.getNome() + " | Status: " + status);
        }
    }

    private void devolverLivro() {
        subTitulo("Devolucao de Livro");
        int clienteId = lerInteiro("Digite o id do cliente: ");

        try {
            List<Emprestimo> emprestimosAbertos = controller.listarEmprestimosAbertos(clienteId);
            if (emprestimosAbertos.isEmpty()) {
                println("Nao ha emprestimos em aberto para este cliente.");
                return;
            }

            println("Emprestimos em aberto:");
            for (Emprestimo emprestimo : emprestimosAbertos) {
                println(
                        "Id " + emprestimo.getId()
                                + " | Livro: " + emprestimo.getLivro().getNome()
                                + " | Retirada: " + formatarData(emprestimo.getDataRetirada())
                                + " | Devolucao: " + formatarData(emprestimo.getDataDevolucao())
                );
            }

            int emprestimoId = lerInteiro("Digite o id do emprestimo para devolver: ");
            boolean devolvido = controller.devolverLivro(clienteId, emprestimoId);
            if (devolvido) {
                println("Devolucao registrada com sucesso.");
            } else {
                println("Emprestimo nao encontrado ou ja devolvido.");
            }
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private String formatarData(java.util.Date data) {
        return formatoData.format(data);
    }

    private void print(String str) {
        System.out.print(str);
    }

    private void println(String str) {
        System.out.println(str);
    }

    private void linha() {
        println("==================================================");
    }

    private void titulo(String texto) {
        linha();
        println(texto);
        linha();
    }

    private void subTitulo(String texto) {
        println("---- " + texto + " ----");
    }

    private void vazio() {
        println("");
    }

    private void bemVindo() {
        titulo("Sistema de Biblioteca");
    }

    private String lerLinha(String prompt) {
        print(prompt);
        return scan.nextLine().trim();
    }

    private int lerInteiro(String prompt) {
        while (true) {
            String valor = lerLinha(prompt);
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                println("Valor invalido. Tente novamente.");
            }
        }
    }
}
