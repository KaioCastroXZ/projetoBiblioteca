package main.view;

import main.controller.BibliotecaController;
import main.exception.ClienteComMultasPendentesException;
import main.exception.ClienteNaoEncontradoException;
import main.exception.LivroIndisponivelException;
import main.exception.LivroNaoEncontradoException;
import main.model.Administrador;
import main.model.Cliente;
import main.model.Emprestimo;
import main.model.Livro;
import main.model.Multa;
import main.model.TipoUsuario;
import main.model.Usuario;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class SistemaView {

    private static final int LARGURA = 96;

    private final BibliotecaController controller;
    private final Scanner scan = new Scanner(System.in);
    private final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    public SistemaView(BibliotecaController controller) {
        this.controller = controller;
        this.formatoData.setLenient(false);
    }

    public void iniciar() {
        bemVindo();
        while (true) {
            exibirMenuAcesso();
        }
    }

    private void exibirMenuAcesso() {
        exibirMenu(
                "Acesso",
                "1  Entrar com usuario cadastrado",
                "2  Cadastrar administrador inicial",
                "0  Sair"
        );

        int opcao = lerInteiro("Opcao: ");
        switch (opcao) {
            case 1 -> executarAcao(this::entrar);
            case 2 -> executarAcao(this::cadastrarAdministradorInicial);
            case 0 -> sair();
            default -> erro("Opcao invalida.");
        }
        vazio();
    }

    private void entrar() {
        subTitulo("Entrada no Sistema");
        int usuarioId = lerInteiro("Digite o id do usuario: ");
        Optional<Usuario> usuarioOpt = controller.buscarUsuarioPorId(usuarioId);

        if (usuarioOpt.isEmpty()) {
            erro("Usuario nao encontrado.");
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getTipo() == TipoUsuario.ADMINISTRADOR) {
            menuAdministrador((Administrador) usuario);
            return;
        }

        menuCliente((Cliente) usuario);
    }

    private void cadastrarAdministradorInicial() {
        subTitulo("Cadastro de Administrador");
        String nome = lerLinha("Digite o nome do administrador: ");
        Administrador administrador = controller.cadastrarAdministrador(nome);
        sucesso("Administrador cadastrado. Id: " + administrador.getId());
    }

    private void menuCliente(Cliente cliente) {
        while (true) {
            exibirMenu(
                    "Cliente - " + cliente.getNome(),
                    "1  Listar livros",
                    "2  Pegar emprestimo",
                    "3  Devolver livro",
                    "4  Consultar minhas multas",
                    "5  Pagar multa",
                    "6  Consultar meu historico de emprestimos",
                    "0  Voltar"
            );

            int opcao = lerInteiro("Opcao: ");
            switch (opcao) {
                case 1 -> executarAcao(this::listarLivros);
                case 2 -> executarAcao(() -> emprestarLivro(cliente));
                case 3 -> executarAcao(() -> devolverLivro(cliente));
                case 4 -> executarAcao(() -> listarMultas(cliente));
                case 5 -> executarAcao(() -> pagarMulta(cliente));
                case 6 -> executarAcao(() -> consultarHistorico(cliente));
                case 0 -> {
                    return;
                }
                default -> erro("Opcao invalida.");
            }
            vazio();
        }
    }

    private void menuAdministrador(Administrador administrador) {
        while (true) {
            exibirMenu(
                    "Administrador - " + administrador.getNome(),
                    " 1  Cadastrar livro",
                    " 2  Cadastrar cliente",
                    " 3  Cadastrar administrador",
                    " 4  Listar todos os usuarios",
                    " 5  Listar livros",
                    " 6  Registrar emprestimo para cliente",
                    " 7  Registrar multa",
                    " 8  Consultar historico de cliente",
                    " 9  Listar multas de cliente",
                    "10  Consultar todos os emprestimos",
                    "11  Consultar todas as multas",
                    "12  Cadastrar emprestimo manual",
                    "13  Atualizar livro",
                    "14  Excluir livro",
                    "15  Atualizar usuario",
                    "16  Excluir usuario",
                    "17  Atualizar emprestimo",
                    "18  Excluir emprestimo",
                    " 0  Voltar"
            );

            int opcao = lerInteiro("Opcao: ");
            switch (opcao) {
                case 1 -> executarAcao(this::cadastrarLivro);
                case 2 -> executarAcao(() -> cadastrarUsuario(TipoUsuario.CLIENTE));
                case 3 -> executarAcao(() -> cadastrarUsuario(TipoUsuario.ADMINISTRADOR));
                case 4 -> executarAcao(this::listarUsuarios);
                case 5 -> executarAcao(this::listarLivros);
                case 6 -> executarAcao(this::registrarEmprestimoParaCliente);
                case 7 -> executarAcao(this::registrarMulta);
                case 8 -> executarAcao(this::consultarHistoricoCliente);
                case 9 -> executarAcao(this::listarMultasCliente);
                case 10 -> executarAcao(this::listarEmprestimos);
                case 11 -> executarAcao(this::listarTodasMultas);
                case 12 -> executarAcao(this::cadastrarEmprestimoManual);
                case 13 -> executarAcao(this::atualizarLivro);
                case 14 -> executarAcao(this::excluirLivro);
                case 15 -> executarAcao(this::atualizarUsuario);
                case 16 -> executarAcao(this::excluirUsuario);
                case 17 -> executarAcao(this::atualizarEmprestimo);
                case 18 -> executarAcao(this::excluirEmprestimo);
                case 0 -> {
                    return;
                }
                default -> erro("Opcao invalida.");
            }
            vazio();
        }
    }

    private void cadastrarLivro() {
        subTitulo("Cadastro de Livro");
        String nome = lerLinha("Digite o nome do livro: ");
        Livro livro = controller.cadastrarLivro(nome);
        sucesso("Livro cadastrado. Id: " + livro.getId());
    }

    private void cadastrarUsuario(TipoUsuario tipo) {
        String titulo = tipo == TipoUsuario.ADMINISTRADOR ? "Cadastro de Administrador" : "Cadastro de Cliente";
        String rotulo = tipo == TipoUsuario.ADMINISTRADOR ? "administrador" : "cliente";

        subTitulo(titulo);
        String nome = lerLinha("Digite o nome do " + rotulo + ": ");
        Usuario usuario = controller.cadastrarUsuario(nome, tipo);
        sucesso(capitalizar(rotulo) + " cadastrado. Id: " + usuario.getId());
    }

    private void registrarEmprestimoParaCliente() {
        subTitulo("Emprestimo de Livro");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        int livroId = lerInteiro("Digite o id do livro: ");

        try {
            Cliente cliente = controller.buscarClientePorId(clienteId);
            Emprestimo emprestimo = controller.emprestarLivro(clienteId, livroId);
            sucesso("Emprestimo registrado. Id: " + emprestimo.getId()
                    + " | Cliente: " + cliente.getNome()
                    + " | Livro: " + emprestimo.getLivro().getNome());
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            erro(e.getMessage());
        }
    }

    private void emprestarLivro(Cliente cliente) {
        subTitulo("Emprestimo de Livro");
        int livroId = lerInteiro("Digite o id do livro: ");

        try {
            Emprestimo emprestimo = controller.emprestarLivro(cliente.getId(), livroId);
            sucesso("Emprestimo registrado. Id: " + emprestimo.getId()
                    + " | Livro: " + emprestimo.getLivro().getNome());
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            erro(e.getMessage());
        }
    }

    private void registrarMulta() {
        subTitulo("Registro de Multa");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        double valor = lerDecimalPositivo("Digite o valor da multa: ");

        try {
            int idMulta = controller.registrarMulta(clienteId, valor);
            sucesso("Multa registrada. Id: " + idMulta);
        } catch (ClienteNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void pagarMulta(Cliente cliente) {
        subTitulo("Pagamento de Multa");

        try {
            double pendentes = controller.calcularValorMultasPendentes(cliente.getId());
            if (pendentes <= 0.0) {
                info("Cliente nao possui multas pendentes.");
                return;
            }

            List<Multa> pendentesCliente = controller.listarMultasDoCliente(cliente.getId())
                    .stream()
                    .filter(multa -> !multa.isPaga())
                    .toList();
            info("Total pendente: " + formatarValor(pendentes) + " | Cliente: " + cliente.getNome());
            exibirListaMultas(pendentesCliente, false);

            int multaId = lerInteiro("Digite o id da multa para pagamento: ");
            boolean pago = controller.pagarMulta(cliente.getId(), multaId);
            if (pago) {
                sucesso("Multa paga.");
            } else {
                erro("Multa nao encontrada ou ja paga.");
            }
        } catch (ClienteNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void listarMultas(Cliente cliente) {
        subTitulo("Minhas Multas");
        listarMultasDoCliente(cliente.getId(), false);
    }

    private void listarMultasCliente() {
        subTitulo("Multas do Cliente");
        int clienteId = lerInteiro("Digite o id do cliente: ");
        listarMultasDoCliente(clienteId, false);
    }

    private void listarMultasDoCliente(int clienteId, boolean mostrarUsuario) {
        try {
            List<Multa> multas = controller.listarMultasDoCliente(clienteId);
            if (multas.isEmpty()) {
                info("Cliente nao possui multas.");
                return;
            }

            exibirListaMultas(multas, mostrarUsuario);
        } catch (ClienteNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void listarTodasMultas() {
        subTitulo("Todas as Multas");
        List<Multa> multas = controller.listarTodasMultas();
        if (multas.isEmpty()) {
            info("Nenhuma multa cadastrada.");
            return;
        }
        exibirListaMultas(multas, true);
    }

    private void consultarHistorico(Cliente cliente) {
        subTitulo("Historico de Emprestimos");
        consultarHistoricoDoCliente(cliente.getId(), cliente.getNome());
    }

    private void consultarHistoricoCliente() {
        subTitulo("Historico de Emprestimos do Cliente");
        int clienteId = lerInteiro("Digite o id do cliente: ");

        try {
            Cliente cliente = controller.buscarClientePorId(clienteId);
            consultarHistoricoDoCliente(clienteId, cliente.getNome());
        } catch (ClienteNaoEncontradoException e) {
            println(e.getMessage());
        }
    }

    private void consultarHistoricoDoCliente(int clienteId, String nomeCliente) {
        try {
            List<Emprestimo> historico = controller.consultarHistorico(clienteId);
            if (historico.isEmpty()) {
                info("Cliente nao possui emprestimos.");
                return;
            }

            info("Cliente: " + nomeCliente);
            exibirListaEmprestimos(historico);
        } catch (ClienteNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void listarUsuarios() {
        subTitulo("Usuarios Cadastrados");
        List<Usuario> usuarios = controller.listarUsuarios();
        if (usuarios.isEmpty()) {
            info("Nenhum usuario cadastrado.");
            return;
        }

        linhaFina();
        imprimirLinha("%-6s %-28s %-16s %-18s %-18s", "ID", "NOME", "TIPO", "MULTAS", "ABERTOS");
        linhaFina();
        for (Usuario usuario : usuarios) {
            String multas = "-";
            String abertos = "-";

            if (usuario.getTipo() == TipoUsuario.CLIENTE) {
                try {
                    multas = formatarValor(controller.calcularValorMultasPendentes(usuario.getId()));
                    abertos = String.valueOf(controller.quantidadeEmprestimosEmAberto(usuario.getId()));
                } catch (ClienteNaoEncontradoException e) {
                    multas = "erro";
                    abertos = "erro";
                }
            }

            imprimirLinha(
                    "%-6d %-28s %-16s %-18s %-18s",
                    usuario.getId(),
                    limitar(usuario.getNome(), 28),
                    formatarTipo(usuario.getTipo()),
                    multas,
                    abertos
            );
        }
        linhaFina();
    }

    private void listarLivros() {
        subTitulo("Livros Cadastrados");
        List<Livro> livros = controller.listarLivros();
        if (livros.isEmpty()) {
            info("Nenhum livro cadastrado.");
            return;
        }

        linhaFina();
        imprimirLinha("%-6s %-58s %-18s", "ID", "NOME", "STATUS");
        linhaFina();
        for (Livro livro : livros) {
            String status = livro.isDisponivel() ? "disponivel" : "emprestado";
            imprimirLinha("%-6d %-58s %-18s", livro.getId(), limitar(livro.getNome(), 58), status);
        }
        linhaFina();
    }

    private void devolverLivro(Cliente cliente) {
        subTitulo("Devolucao de Livro");

        try {
            List<Emprestimo> emprestimosAbertos = controller.listarEmprestimosAbertos(cliente.getId());
            if (emprestimosAbertos.isEmpty()) {
                info("Nao ha emprestimos em aberto para este cliente.");
                return;
            }

            info("Emprestimos em aberto:");
            exibirListaEmprestimos(emprestimosAbertos);

            int emprestimoId = lerInteiro("Digite o id do emprestimo para devolver: ");
            boolean devolvido = controller.devolverLivro(cliente.getId(), emprestimoId);
            if (devolvido) {
                sucesso("Devolucao registrada.");
            } else {
                erro("Emprestimo nao encontrado, pertence a outro cliente ou ja foi devolvido.");
            }
        } catch (ClienteNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void atualizarLivro() {
        subTitulo("Atualizacao de Livro");
        int id = lerInteiro("Id do livro: ");
        String nome = lerLinha("Novo nome: ");

        try {
            boolean ok = controller.atualizarLivro(id, nome);
            if (ok) {
                sucesso("Livro atualizado.");
            } else {
                erro("Livro nao encontrado.");
            }
        } catch (LivroNaoEncontradoException e) {
            erro(e.getMessage());
        }
    }

    private void excluirLivro() {
        subTitulo("Exclusao de Livro");
        int id = lerInteiro("Id do livro: ");
        boolean ok = controller.deletarLivro(id);
        if (ok) {
            sucesso("Livro removido.");
        } else {
            erro("Livro nao encontrado ou vinculado a emprestimos.");
        }
    }

    private void atualizarUsuario() {
        subTitulo("Atualizacao de Usuario");
        int id = lerInteiro("Id do usuario: ");
        String nome = lerLinha("Novo nome: ");
        boolean ok = controller.atualizarUsuario(id, nome);
        if (ok) {
            sucesso("Usuario atualizado.");
        } else {
            erro("Usuario nao encontrado.");
        }
    }

    private void excluirUsuario() {
        subTitulo("Exclusao de Usuario");
        int id = lerInteiro("Id do usuario: ");
        boolean ok = controller.deletarUsuario(id);
        if (ok) {
            sucesso("Usuario removido.");
        } else {
            erro("Usuario nao encontrado ou possui emprestimos em aberto.");
        }
    }

    private void listarEmprestimos() {
        subTitulo("Emprestimos");
        List<Emprestimo> emprestimos = controller.listarEmprestimos();
        if (emprestimos.isEmpty()) {
            info("Nenhum emprestimo cadastrado.");
            return;
        }
        exibirListaEmprestimos(emprestimos);
    }

    private void cadastrarEmprestimoManual() {
        subTitulo("Cadastro Manual de Emprestimo");
        int clienteId = lerInteiro("Id do cliente: ");
        int livroId = lerInteiro("Id do livro: ");
        Date retirada = lerData("Data de retirada (dd/MM/yyyy): ");
        Date devolucao = lerData("Data de devolucao (dd/MM/yyyy): ");
        boolean devolvido = lerBooleano("Ja devolvido? (s/n): ");

        try {
            boolean ok = controller.cadastrarEmprestimo(clienteId, livroId, retirada, devolucao, devolvido);
            if (ok) {
                sucesso("Emprestimo cadastrado.");
            } else {
                erro("Nao foi possivel cadastrar emprestimo.");
            }
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException
                 | LivroIndisponivelException | ClienteComMultasPendentesException e) {
            erro(e.getMessage());
        }
    }

    private void atualizarEmprestimo() {
        subTitulo("Atualizacao de Emprestimo");
        int id = lerInteiro("Id do emprestimo: ");
        int clienteId = lerInteiro("Novo id do cliente: ");
        int livroId = lerInteiro("Novo id do livro: ");
        Date retirada = lerData("Nova data de retirada (dd/MM/yyyy): ");
        Date devolucao = lerData("Nova data de devolucao (dd/MM/yyyy): ");
        boolean devolvido = lerBooleano("Devolvido? (s/n): ");

        try {
            boolean ok = controller.atualizarEmprestimo(id, clienteId, livroId, retirada, devolucao, devolvido);
            if (ok) {
                sucesso("Emprestimo atualizado.");
            } else {
                erro("Emprestimo nao encontrado.");
            }
        } catch (ClienteNaoEncontradoException | LivroNaoEncontradoException | LivroIndisponivelException e) {
            erro(e.getMessage());
        }
    }

    private void excluirEmprestimo() {
        subTitulo("Exclusao de Emprestimo");
        int id = lerInteiro("Id do emprestimo: ");
        boolean ok = controller.deletarEmprestimo(id);
        if (ok) {
            sucesso("Emprestimo removido.");
        } else {
            erro("Emprestimo nao encontrado.");
        }
    }

    private void exibirListaEmprestimos(List<Emprestimo> emprestimos) {
        linhaFina();
        imprimirLinha(
                "%-6s %-9s %-34s %-12s %-12s %-12s",
                "ID",
                "CLIENTE",
                "LIVRO",
                "RETIRADA",
                "DEVOLUCAO",
                "STATUS"
        );
        linhaFina();
        for (Emprestimo emprestimo : emprestimos) {
            String status = emprestimo.isDevolvido() ? "devolvido" : "em aberto";
            imprimirLinha(
                    "%-6d %-9d %-34s %-12s %-12s %-12s",
                    emprestimo.getId(),
                    emprestimo.getClienteId(),
                    limitar(emprestimo.getLivro().getNome(), 34),
                    formatarData(emprestimo.getDataRetirada()),
                    formatarData(emprestimo.getDataDevolucao()),
                    status
            );
        }
        linhaFina();
    }

    private void exibirListaMultas(List<Multa> multas, boolean mostrarUsuario) {
        linhaFina();
        if (mostrarUsuario) {
            imprimirLinha("%-6s %-10s %-16s %-12s", "ID", "CLIENTE", "VALOR", "STATUS");
        } else {
            imprimirLinha("%-6s %-16s %-12s", "ID", "VALOR", "STATUS");
        }
        linhaFina();
        for (Multa multa : multas) {
            String status = multa.isPaga() ? "paga" : "pendente";
            if (mostrarUsuario) {
                imprimirLinha(
                        "%-6d %-10d %-16s %-12s",
                        multa.getId(),
                        multa.getUsuarioId(),
                        formatarValor(multa.getValor()),
                        status
                );
            } else {
                imprimirLinha("%-6d %-16s %-12s", multa.getId(), formatarValor(multa.getValor()), status);
            }
        }
        linhaFina();
    }

    private void exibirMenu(String titulo, String... opcoes) {
        titulo(titulo);
        for (String opcao : opcoes) {
            println("  " + opcao);
        }
        linha();
    }

    private String formatarData(Date data) {
        return formatoData.format(data);
    }

    private String formatarValor(double valor) {
        return "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private String formatarTipo(TipoUsuario tipo) {
        if (tipo == TipoUsuario.ADMINISTRADOR) {
            return "Administrador";
        }
        return "Cliente";
    }

    private String capitalizar(String texto) {
        if (texto.isBlank()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private Date lerData(String prompt) {
        while (true) {
            String entrada = lerLinha(prompt);
            try {
                return formatoData.parse(entrada);
            } catch (ParseException e) {
                erro("Data invalida. Formato esperado: dd/MM/yyyy");
            }
        }
    }

    private String limitar(String texto, int tamanho) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= tamanho) {
            return texto;
        }
        if (tamanho <= 1) {
            return texto.substring(0, tamanho);
        }
        return texto.substring(0, tamanho - 1) + "~";
    }

    private double lerDecimalPositivo(String prompt) {
        while (true) {
            String valor = lerLinha(prompt).replace(",", ".");
            try {
                double numero = Double.parseDouble(valor);
                if (Double.isFinite(numero) && numero > 0.0) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // A mensagem unica abaixo cobre texto invalido e numeros fora da regra.
            }
            erro("Valor invalido. Digite um numero maior que zero.");
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
            erro("Valor invalido. Digite 's' ou 'n'.");
        }
    }

    private void executarAcao(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            erro(e.getMessage());
        } finally {
            aguardarEnter();
        }
    }

    private void sair() {
        info("Encerrando...");
        System.exit(0);
    }

    private void print(String str) {
        System.out.print(str);
    }

    private void println(String str) {
        System.out.println(str);
    }

    private void linha() {
        println("=".repeat(LARGURA));
    }

    private void linhaFina() {
        println("-".repeat(LARGURA));
    }

    private void titulo(String texto) {
        linha();
        println(centralizar(texto.toUpperCase(Locale.ROOT)));
        linha();
    }

    private void subTitulo(String texto) {
        vazio();
        println("[" + texto + "]");
        linhaFina();
    }

    private void vazio() {
        println("");
    }

    private void bemVindo() {
        titulo("Sistema de Biblioteca");
    }

    private void sucesso(String mensagem) {
        println("[OK] " + mensagem);
    }

    private void erro(String mensagem) {
        println("[ERRO] " + mensagem);
    }

    private void info(String mensagem) {
        println("[INFO] " + mensagem);
    }

    private void aguardarEnter() {
        vazio();
        print("Pressione Enter para continuar...");
        scan.nextLine();
    }

    private void imprimirLinha(String formato, Object... valores) {
        System.out.printf(Locale.forLanguageTag("pt-BR"), formato + "%n", valores);
    }

    private String centralizar(String texto) {
        if (texto.length() >= LARGURA) {
            return texto;
        }
        int esquerda = (LARGURA - texto.length()) / 2;
        return " ".repeat(esquerda) + texto;
    }

    private String lerLinha(String prompt) {
        print("> " + prompt);
        return scan.nextLine().trim();
    }

    private int lerInteiro(String prompt) {
        while (true) {
            String valor = lerLinha(prompt);
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                erro("Valor invalido. Tente novamente.");
            }
        }
    }
}
