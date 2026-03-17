package main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private void opcoes() {
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
        opcao = lerInteiro("Opcao: ");
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
        int id = livros.size() + 1;
        String nome = lerLinha("Digite o nome do livro: ");

        Livro livro = new Livro(id, nome);
            livros.add(livro);
        println("Livro cadastrado. Id: " + id);
    }

    private void cadastrarCliente() {
        subTitulo("Cadastro de Cliente");
        int id = clientes.size() + 1;
        String nome = lerLinha("Digite o nome do cliente: ");

        Cliente cliente = new Cliente(id, nome);
            clientes.add(cliente);
        println("Cliente cadastrado. Id: " + id);
    }

    private void emprestarLivro() {
        subTitulo("Emprestimo de Livro");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        int livroId = lerInteiro("Digite o id do livro: ");

        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

        try {
            Livro livro = buscarLivroPorId(livroId);
            validarEmprestimo(cliente, livro);

            Date dataRetirada = new Date();
            Date dataDevolucao = calcularDataDevolucao(dataRetirada);
            Emprestimo emprestimo = new Emprestimo(
                    cliente.getHistoricoEmprestimos().size() + 1,
                    livro,
                    dataRetirada,
                    dataDevolucao
            );

            cliente.registrarEmprestimo(emprestimo);
            livro.marcarEmprestado();
            println("Emprestimo registrado com sucesso. Id: " + emprestimo.getId()
                    + " | Cliente: " + cliente.getNome()
                    + " | Livro: " + livro.getNome());
        } catch (LivroNaoEncontradoException | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            println(e.getMessage());
        }
    }

    private void registrarMulta() {
        subTitulo("Registro de Multa");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

        String valorStr = lerLinha("Digite o valor da multa: ");
        try {
            double valor = Double.parseDouble(valorStr);
            if (valor <= 0.0) {
                println("Valor invalido.");
                return;
            }
            int idMulta = cliente.registrarMulta(valor);
            println("Multa registrada. Id: " + idMulta);
        } catch (NumberFormatException e) {
            println("Valor invalido.");
        }
    }

    private void pagarMulta() {
        subTitulo("Pagamento de Multa");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

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

        int idMulta = lerInteiro("Digite o id da multa para pagamento: ");
        boolean pago = cliente.pagarMulta(idMulta);
        if (pago) {
            println("Multa paga.");
        } else {
            println("Multa nao encontrada ou ja paga.");
        }
    }

    private void listarMultas() {
        subTitulo("Multas do Cliente");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

        if (cliente.getMultas().isEmpty()) {
            println("Cliente nao possui multas.");
            return;
        }

        println("Multas cadastradas:");
        for (Multa multa : cliente.getMultas()) {
            String status = multa.isPaga() ? "paga" : "pendente";
            println("Id " + multa.getId() + " | Valor: " + multa.getValor() + " | Status: " + status);
        }
    }

    private void consultarHistorico() {
        subTitulo("Historico de Emprestimos");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

        List<Emprestimo> historico = cliente.getHistoricoEmprestimos();
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
                            + " | Retirada: " + emprestimo.getDataRetirada()
                            + " | Devolucao: " + emprestimo.getDataDevolucao()
                            + " | Status: " + status
            );
        }
    }

    private void listarClientes() {
        subTitulo("Clientes Cadastrados");
        if (clientes.isEmpty()) {
            println("Nenhum cliente cadastrado.");
            return;
        }

        println("Clientes cadastrados:");
        for (Cliente cliente : clientes) {
            int emprestimosAbertos = 0;
            for (Emprestimo emprestimo : cliente.getHistoricoEmprestimos()) {
                if (!emprestimo.isDevolvido()) {
                    emprestimosAbertos++;
                }
            }
            println(
                    "Id " + cliente.getId()
                            + " | Nome: " + cliente.getNome()
                            + " | Multas pendentes: " + cliente.getValorMultasPendentes()
                            + " | Emprestimos em aberto: " + emprestimosAbertos
            );
        }
    }

    private void listarLivros() {
        subTitulo("Livros Cadastrados");
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
        Cliente cliente = buscarClientePorId(clienteId);
        if (cliente == null) {
            println("Cliente nao encontrado.");
            return;
        }

        List<Emprestimo> historico = cliente.getHistoricoEmprestimos();
        boolean temAbertos = false;
        for (Emprestimo emprestimo : historico) {
            if (!emprestimo.isDevolvido()) {
                if (!temAbertos) {
                    println("Emprestimos em aberto:");
                }
                temAbertos = true;
                println(
                        "Id " + emprestimo.getId()
                                + " | Livro: " + emprestimo.getLivro().getNome()
                                + " | Retirada: " + emprestimo.getDataRetirada()
                                + " | Devolucao: " + emprestimo.getDataDevolucao()
                );
            }
        }

        if (!temAbertos) {
            println("Nao ha emprestimos em aberto para este cliente.");
            return;
        }

        int emprestimoId = lerInteiro("Digite o id do emprestimo para devolver: ");
        Emprestimo alvo = null;
        for (Emprestimo emprestimo : historico) {
            if (emprestimo.getId() == emprestimoId) {
                alvo = emprestimo;
                break;
            }
        }

        if (alvo == null) {
            println("Emprestimo nao encontrado.");
            return;
        }
        if (alvo.isDevolvido()) {
            println("Emprestimo ja devolvido.");
            return;
        }

        alvo.marcarDevolvido();
        alvo.getLivro().marcarDevolvido();
        println("Devolucao registrada com sucesso.");
    }

    private Cliente buscarClientePorId(int id) {
        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }

    private Livro buscarLivroPorId(int id) throws LivroNaoEncontradoException {
        for (Livro livro : livros) {
            if (livro.getId() == id) {
                return livro;
            }
        }
        throw new LivroNaoEncontradoException("Livro nao encontrado no sistema.");
    }

    private void validarEmprestimo(Cliente cliente, Livro livro)
            throws LivroIndisponivelException, ClienteComMultasPendentesException {
        if (!livro.isDisponivel()) {
            throw new LivroIndisponivelException("Livro solicitado nao esta disponivel.");
        }
        if (cliente.temMultasPendentes()) {
            throw new ClienteComMultasPendentesException(
                    "Cliente possui multas pendentes e nao pode realizar emprestimo."
            );
        }
    }

    private Date calcularDataDevolucao(Date dataRetirada) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataRetirada);
        cal.add(Calendar.DAY_OF_MONTH, 14);
        return cal.getTime();
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

    public void iniciar() {
        bemVindo();
        while (true) {
            opcoes();
        }
    }

}
