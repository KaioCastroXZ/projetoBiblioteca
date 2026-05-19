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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        println(" 2) Cadastrar usuario");
        println(" 3) Emprestar livro");
        println(" 4) Registrar multa");
        println(" 5) Pagar multa");
        println(" 6) Consultar historico de emprestimos do usuario");
        println(" 7) Listar usuarios");
        println(" 8) Devolver livro");
        println(" 9) Listar multas do usuario");
        println("10) Listar livros");
        println("11) Atualizar livro");
        println("12) Excluir livro");
        println("13) Atualizar usuario");
        println("14) Excluir usuario");
        println("15) Listar emprestimos");
        println("16) Cadastrar emprestimo (manual)");
        println("17) Atualizar emprestimo");
        println("18) Excluir emprestimo");
        println(" 0) Sair");
        linha();

        int opcao = lerInteiro("Opcao: ");
        switch (opcao) {
            case 1 -> cadastrarLivro();
            case 2 -> cadastrarUsuario();
            case 3 -> emprestarLivro();
            case 4 -> registrarMulta();
            case 5 -> pagarMulta();
            case 6 -> consultarHistorico();
            case 7 -> listarUsuarios();
            case 8 -> devolverLivro();
            case 9 -> listarMultas();
            case 10 -> listarLivros();
            case 11 -> atualizarLivro();
            case 12 -> excluirLivro();
            case 13 -> atualizarUsuario();
            case 14 -> excluirUsuario();
            case 15 -> listarEmprestimos();
            case 16 -> cadastrarEmprestimoManual();
            case 17 -> atualizarEmprestimo();
            case 18 -> excluirEmprestimo();
            case 0 -> {
                println("Encerrando...");
                System.exit(0);
            }
            default -> println("Opcao invalida.");
        }
        vazio();
    }

    private void cadastrarLivro() {
        subTitulo("Cadastro de Livro");
        String nome = lerLinha("Digite o nome do livro: ");
        Livro livro = controller.cadastrarLivro(nome);
        println("Livro cadastrado. Id: " + livro.getId());
    }

    private void cadastrarUsuario() {
        subTitulo("Cadastro de Usuario");
        String nome = lerLinha("Digite o nome do usuario: ");
        Cliente usuario = controller.cadastrarCliente(nome);
        println("Usuario cadastrado. Id: " + usuario.getId());
    }

    private void emprestarLivro() {
        subTitulo("Emprestimo de Livro");
        int usuarioId = lerInteiro("Digite o id do usuario: ");
        int livroId = lerInteiro("Digite o id do livro: ");

        try {
            Cliente usuario = controller.buscarClientePorId(usuarioId);
            Emprestimo emprestimo = controller.emprestarLivro(usuarioId, livroId);
            println("Emprestimo registrado com sucesso. Id: " + emprestimo.getId()
                    + " | Usuario: " + usuario.getNome()
                    + " | Livro: " + emprestimo.getLivro().getNome());
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            println(e.getMessage());
        }
    }

    private void registrarMulta() {
        subTitulo("Registro de Multa");
        int usuarioId = lerInteiro("Digite o id do usuario: ");
        String valorStr = lerLinha("Digite o valor da multa: ");

        try {
            double valor = Double.parseDouble(valorStr);
            if (valor <= 0.0) {
                println("Valor invalido.");
                return;
            }
            int idMulta = controller.registrarMulta(usuarioId, valor);
            println("Multa registrada. Id: " + idMulta);
        } catch (NumberFormatException e) {
            println("Valor invalido.");
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void pagarMulta() {
        subTitulo("Pagamento de Multa");
        int usuarioId = lerInteiro("Digite o id do usuario: ");

        try {
            Cliente usuario = controller.buscarClientePorId(usuarioId);
            double pendentes = controller.calcularValorMultasPendentes(usuarioId);
            if (pendentes <= 0.0) {
                println("Usuario nao possui multas pendentes.");
                return;
            }

            println("Multas pendentes:");
            for (Multa multa : controller.listarMultasDoCliente(usuarioId)) {
                if (!multa.isPaga()) {
                    println("Id " + multa.getId() + " | Valor: " + multa.getValor());
                }
            }
            println("Total pendente: " + pendentes + " | Usuario: " + usuario.getNome());

            int multaId = lerInteiro("Digite o id da multa para pagamento: ");
            boolean pago = controller.pagarMulta(usuarioId, multaId);
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
        subTitulo("Multas do Usuario");
        int usuarioId = lerInteiro("Digite o id do usuario: ");

        try {
            List<Multa> multas = controller.listarMultasDoCliente(usuarioId);
            if (multas.isEmpty()) {
                println("Usuario nao possui multas.");
                return;
            }

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
        int usuarioId = lerInteiro("Digite o id do usuario: ");

        try {
            Cliente usuario = controller.buscarClientePorId(usuarioId);
            List<Emprestimo> historico = controller.consultarHistorico(usuarioId);
            if (historico.isEmpty()) {
                println("Usuario nao possui emprestimos.");
                return;
            }

            println("Historico de emprestimos do usuario " + usuario.getNome() + ":");
            exibirListaEmprestimos(historico);
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void listarUsuarios() {
        subTitulo("Usuarios Cadastrados");
        List<Cliente> usuarios = controller.listarClientes();
        if (usuarios.isEmpty()) {
            println("Nenhum usuario cadastrado.");
            return;
        }

        for (Cliente usuario : usuarios) {
            try {
                println(
                        "Id " + usuario.getId()
                                + " | Nome: " + usuario.getNome()
                                + " | Multas pendentes: " + controller.calcularValorMultasPendentes(usuario.getId())
                                + " | Emprestimos em aberto: " + controller.quantidadeEmprestimosEmAberto(usuario.getId())
                );
            } catch (ClienteNaoEncontradoException e) {
                println("Erro ao consultar dados do usuario " + usuario.getId());
            }
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
        int usuarioId = lerInteiro("Digite o id do usuario: ");

        try {
            List<Emprestimo> emprestimosAbertos = controller.listarEmprestimosAbertos(usuarioId);
            if (emprestimosAbertos.isEmpty()) {
                println("Nao ha emprestimos em aberto para este usuario.");
                return;
            }

            println("Emprestimos em aberto:");
            exibirListaEmprestimos(emprestimosAbertos);

            int emprestimoId = lerInteiro("Digite o id do emprestimo para devolver: ");
            boolean devolvido = controller.devolverLivro(usuarioId, emprestimoId);
            if (devolvido) {
                println("Devolucao registrada com sucesso.");
            } else {
                println("Emprestimo nao encontrado, pertence a outro usuario ou ja foi devolvido.");
            }
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void atualizarLivro() {
        subTitulo("Atualizacao de Livro");
        int id = lerInteiro("Id do livro: ");
        String nome = lerLinha("Novo nome: ");

        try {
            boolean ok = controller.atualizarLivro(id, nome);
            println(ok ? "Livro atualizado." : "Livro nao encontrado.");
        } catch (LivroNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void excluirLivro() {
        subTitulo("Exclusao de Livro");
        int id = lerInteiro("Id do livro: ");
        boolean ok = controller.deletarLivro(id);
        println(ok ? "Livro removido." : "Livro nao encontrado ou vinculado a emprestimos.");
    }

    private void atualizarUsuario() {
        subTitulo("Atualizacao de Usuario");
        int id = lerInteiro("Id do usuario: ");
        String nome = lerLinha("Novo nome: ");
        try {
            boolean ok = controller.atualizarCliente(id, nome);
            println(ok ? "Usuario atualizado." : "Usuario nao encontrado.");
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void excluirUsuario() {
        subTitulo("Exclusao de Usuario");
        int id = lerInteiro("Id do usuario: ");
        boolean ok = controller.deletarCliente(id);
        println(ok ? "Usuario removido." : "Usuario nao encontrado.");
    }

    private void listarEmprestimos() {
        subTitulo("Emprestimos");
        List<Emprestimo> emprestimos = controller.listarEmprestimos();
        if (emprestimos.isEmpty()) {
            println("Nenhum emprestimo cadastrado.");
            return;
        }
        exibirListaEmprestimos(emprestimos);
    }

    private void cadastrarEmprestimoManual() {
        subTitulo("Cadastro Manual de Emprestimo");
        int usuarioId = lerInteiro("Id do usuario: ");
        int livroId = lerInteiro("Id do livro: ");
        Date retirada = lerData("Data de retirada (dd/MM/yyyy): ");
        Date devolucao = lerData("Data de devolucao (dd/MM/yyyy): ");
        boolean devolvido = lerBooleano("Ja devolvido? (s/n): ");

        try {
            boolean ok = controller.cadastrarEmprestimo(usuarioId, livroId, retirada, devolucao, devolvido);
            println(ok ? "Emprestimo cadastrado." : "Nao foi possivel cadastrar emprestimo.");
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            println(e.getMessage());
        }
    }

    private void atualizarEmprestimo() {
        subTitulo("Atualizacao de Emprestimo");
        int id = lerInteiro("Id do emprestimo: ");
        int usuarioId = lerInteiro("Novo id do usuario: ");
        int livroId = lerInteiro("Novo id do livro: ");
        Date retirada = lerData("Nova data de retirada (dd/MM/yyyy): ");
        Date devolucao = lerData("Nova data de devolucao (dd/MM/yyyy): ");
        boolean devolvido = lerBooleano("Devolvido? (s/n): ");

        try {
            boolean ok = controller.atualizarEmprestimo(id, usuarioId, livroId, retirada, devolucao, devolvido);
            println(ok ? "Emprestimo atualizado." : "Emprestimo nao encontrado.");
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void excluirEmprestimo() {
        subTitulo("Exclusao de Emprestimo");
        int id = lerInteiro("Id do emprestimo: ");
        boolean ok = controller.deletarEmprestimo(id);
        println(ok ? "Emprestimo removido." : "Emprestimo nao encontrado.");
    }

    private void exibirListaEmprestimos(List<Emprestimo> emprestimos) {
        for (Emprestimo emprestimo : emprestimos) {
            String status = emprestimo.isDevolvido() ? "devolvido" : "em aberto";
            println(
                    "Id " + emprestimo.getId()
                            + " | Usuario Id: " + emprestimo.getClienteId()
                            + " | Livro: " + emprestimo.getLivro().getNome()
                            + " | Retirada: " + formatarData(emprestimo.getDataRetirada())
                            + " | Devolucao: " + formatarData(emprestimo.getDataDevolucao())
                            + " | Status: " + status
            );
        }
    }

    private String formatarData(Date data) {
        return formatoData.format(data);
    }

    private Date lerData(String prompt) {
        while (true) {
            String entrada = lerLinha(prompt);
            try {
                return formatoData.parse(entrada);
            } catch (ParseException e) {
                println("Data invalida. Formato esperado: dd/MM/yyyy");
            }
        }
    }

    private boolean lerBooleano(String prompt) {
        while (true) {
            String valor = lerLinha(prompt).toLowerCase(Locale.ROOT);
            if ("s".equals(valor)) {
                return true;
            }
            if ("n".equals(valor)) {
                return false;
            }
            println("Valor invalido. Digite 's' ou 'n'.");
        }
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
