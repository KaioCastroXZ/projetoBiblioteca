package main.controller;

import main.dao.EmprestimoDAO;
import main.dao.LivroDAO;
import main.dao.MultaDAO;
import main.dao.UsuarioDAO;
import main.dao.jdbc.JdbcEmprestimoDAO;
import main.dao.jdbc.JdbcLivroDAO;
import main.dao.jdbc.JdbcMultaDAO;
import main.dao.jdbc.JdbcUsuarioDAO;
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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BibliotecaController {

    private final LivroDAO livroDAO;
    private final UsuarioDAO usuarioDAO;
    private final EmprestimoDAO emprestimoDAO;
    private final MultaDAO multaDAO;

    public BibliotecaController() {
        this(new JdbcLivroDAO(), new JdbcUsuarioDAO(), new JdbcEmprestimoDAO(), new JdbcMultaDAO());
    }

    public BibliotecaController(
            LivroDAO livroDAO,
            UsuarioDAO usuarioDAO,
            EmprestimoDAO emprestimoDAO,
            MultaDAO multaDAO
    ) {
        this.livroDAO = livroDAO;
        this.usuarioDAO = usuarioDAO;
        this.emprestimoDAO = emprestimoDAO;
        this.multaDAO = multaDAO;
    }

    public Livro cadastrarLivro(String nome) {
        String nomeValidado = validarNome(nome, "Nome do livro");
        try {
            return livroDAO.inserir(new Livro(0, nomeValidado));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao cadastrar livro.", e);
        }
    }

    public Cliente cadastrarCliente(String nome) {
        String nomeValidado = validarNome(nome, "Nome do cliente");
        try {
            return usuarioDAO.inserir(new Cliente(0, nomeValidado));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao cadastrar usuario.", e);
        }
    }

    public Administrador cadastrarAdministrador(String nome) {
        String nomeValidado = validarNome(nome, "Nome do administrador");
        try {
            return usuarioDAO.inserirAdministrador(new Administrador(0, nomeValidado));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao cadastrar administrador.", e);
        }
    }

    public Usuario cadastrarUsuario(String nome, TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de usuario e obrigatorio.");
        }
        if (tipo == TipoUsuario.ADMINISTRADOR) {
            return cadastrarAdministrador(nome);
        }
        return cadastrarCliente(nome);
    }

    public Emprestimo emprestarLivro(int clienteId, int livroId)
            throws ClienteNaoEncontradoException, LivroNaoEncontradoException,
            LivroIndisponivelException, ClienteComMultasPendentesException {
        Cliente cliente = buscarClientePorId(clienteId);
        Livro livro = buscarLivroPorId(livroId);
        validarEmprestimo(cliente, livro);

        Date dataRetirada = new Date();
        Date dataDevolucao = calcularDataDevolucao(dataRetirada);
        Emprestimo emprestimo = new Emprestimo(0, cliente.getId(), livro, dataRetirada, dataDevolucao);

        try {
            Emprestimo salvo = emprestimoDAO.inserir(emprestimo);
            livro.marcarEmprestado();
            livroDAO.atualizar(livro);
            return salvo;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao registrar emprestimo.", e);
        }
    }

    public boolean cadastrarEmprestimo(int usuarioId, int livroId, Date retirada, Date devolucao, boolean devolvido)
            throws ClienteNaoEncontradoException, LivroNaoEncontradoException,
            LivroIndisponivelException, ClienteComMultasPendentesException {
        validarPeriodoEmprestimo(retirada, devolucao);
        Cliente usuario = buscarClientePorId(usuarioId);
        Livro livro = buscarLivroPorId(livroId);
        if (!devolvido) {
            validarEmprestimo(usuario, livro);
        }

        Emprestimo novo = new Emprestimo(0, usuario.getId(), livro, retirada, devolucao);
        novo.setDevolvido(devolvido);
        try {
            Emprestimo salvo = emprestimoDAO.inserir(novo);
            if (!devolvido) {
                livro.marcarEmprestado();
                livroDAO.atualizar(livro);
            }
            return salvo.getId() > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao cadastrar emprestimo.", e);
        }
    }

    public int registrarMulta(int clienteId, double valor) throws ClienteNaoEncontradoException {
        validarValorMulta(valor);
        buscarClientePorId(clienteId);
        try {
            return multaDAO.inserir(clienteId, valor).getId();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao registrar multa.", e);
        }
    }

    public boolean pagarMulta(int clienteId, int multaId) throws ClienteNaoEncontradoException {
        validarIdPositivo(multaId, "Id da multa");
        buscarClientePorId(clienteId);
        try {
            for (Multa multa : multaDAO.listarPorUsuario(clienteId)) {
                if (multa.getId() == multaId && !multa.isPaga()) {
                    multa.pagar();
                    return multaDAO.atualizar(multa);
                }
            }
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao pagar multa.", e);
        }
    }

    public Cliente buscarClientePorId(int id) throws ClienteNaoEncontradoException {
        validarIdPositivo(id, "Id do cliente");
        try {
            Optional<Cliente> cliente = usuarioDAO.buscarPorId(id);
            if (cliente.isPresent()) {
                return cliente.get();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao buscar usuario.", e);
        }
        throw new ClienteNaoEncontradoException("Cliente nao encontrado.");
    }

    public Optional<Usuario> buscarUsuarioPorId(int id) {
        validarIdPositivo(id, "Id do usuario");
        try {
            return usuarioDAO.buscarUsuarioPorId(id);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao buscar usuario.", e);
        }
    }

    public Livro buscarLivroPorId(int id) throws LivroNaoEncontradoException {
        validarIdPositivo(id, "Id do livro");
        try {
            Optional<Livro> livro = livroDAO.buscarPorId(id);
            if (livro.isPresent()) {
                return livro.get();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao buscar livro.", e);
        }
        throw new LivroNaoEncontradoException("Livro nao encontrado no sistema.");
    }

    public Emprestimo buscarEmprestimoPorId(int id) {
        validarIdPositivo(id, "Id do emprestimo");
        try {
            return emprestimoDAO.buscarPorId(id).orElse(null);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao buscar emprestimo.", e);
        }
    }

    public List<Cliente> listarClientes() {
        try {
            return usuarioDAO.listarTodos();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar usuarios.", e);
        }
    }

    public List<Usuario> listarUsuarios() {
        try {
            return usuarioDAO.listarUsuarios();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar usuarios.", e);
        }
    }

    public List<Livro> listarLivros() {
        try {
            return livroDAO.listarTodos();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar livros.", e);
        }
    }

    public List<Emprestimo> listarEmprestimos() {
        try {
            return emprestimoDAO.listarTodos();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar emprestimos.", e);
        }
    }

    public List<Multa> listarMultasDoCliente(int clienteId) throws ClienteNaoEncontradoException {
        buscarClientePorId(clienteId);
        try {
            return multaDAO.listarPorUsuario(clienteId);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar multas.", e);
        }
    }

    public List<Multa> listarTodasMultas() {
        try {
            return multaDAO.listarTodos();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar multas.", e);
        }
    }

    public List<Emprestimo> consultarHistorico(int clienteId) throws ClienteNaoEncontradoException {
        buscarClientePorId(clienteId);
        try {
            return emprestimoDAO.listarPorUsuario(clienteId);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao consultar historico.", e);
        }
    }

    public List<Emprestimo> listarEmprestimosAbertos(int clienteId) throws ClienteNaoEncontradoException {
        buscarClientePorId(clienteId);
        try {
            return emprestimoDAO.listarAbertosPorUsuario(clienteId);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao listar emprestimos abertos.", e);
        }
    }

    public boolean devolverLivro(int clienteId, int emprestimoId) throws ClienteNaoEncontradoException {
        buscarClientePorId(clienteId);
        try {
            Optional<Emprestimo> emprestimoOpt = emprestimoDAO.buscarPorId(emprestimoId);
            if (emprestimoOpt.isEmpty()) {
                return false;
            }

            Emprestimo emprestimo = emprestimoOpt.get();
            if (emprestimo.getClienteId() != clienteId || emprestimo.isDevolvido()) {
                return false;
            }

            emprestimo.setDevolvido(true);
            boolean atualizado = emprestimoDAO.atualizar(emprestimo);
            if (!atualizado) {
                return false;
            }

            Livro livro = emprestimo.getLivro();
            livro.marcarDevolvido();
            livroDAO.atualizar(livro);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao devolver livro.", e);
        }
    }

    public boolean atualizarLivro(int id, String novoNome) throws LivroNaoEncontradoException {
        String nomeValidado = validarNome(novoNome, "Nome do livro");
        Livro livro = buscarLivroPorId(id);
        livro.setNome(nomeValidado);
        try {
            return livroDAO.atualizar(livro);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao atualizar livro.", e);
        }
    }

    public boolean deletarLivro(int id) {
        validarIdPositivo(id, "Id do livro");
        try {
            return livroDAO.deletar(id);
        } catch (SQLException e) {
            if (isViolacaoChaveEstrangeira(e)) {
                return false;
            }
            throw new IllegalStateException("Erro ao remover livro.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao remover livro.", e);
        }
    }

    public boolean atualizarCliente(int id, String novoNome) throws ClienteNaoEncontradoException {
        String nomeValidado = validarNome(novoNome, "Nome do cliente");
        Cliente usuario = buscarClientePorId(id);
        usuario.setNome(nomeValidado);
        try {
            return usuarioDAO.atualizar(usuario);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao atualizar usuario.", e);
        }
    }

    public boolean atualizarUsuario(int id, String novoNome) {
        String nomeValidado = validarNome(novoNome, "Nome do usuario");
        validarIdPositivo(id, "Id do usuario");
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.buscarUsuarioPorId(id);
            if (usuarioOpt.isEmpty()) {
                return false;
            }

            Usuario usuario = usuarioOpt.get();
            usuario.setNome(nomeValidado);
            return usuarioDAO.atualizarUsuario(usuario);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao atualizar usuario.", e);
        }
    }

    public boolean deletarCliente(int id) {
        return deletarUsuario(id);
    }

    public boolean deletarUsuario(int id) {
        validarIdPositivo(id, "Id do usuario");
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.buscarUsuarioPorId(id);
            if (usuarioOpt.isEmpty()) {
                return false;
            }

            if (usuarioOpt.get().getTipo() == TipoUsuario.CLIENTE
                    && !emprestimoDAO.listarAbertosPorUsuario(id).isEmpty()) {
                return false;
            }

            return usuarioDAO.deletar(id);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao remover usuario.", e);
        }
    }

    public boolean atualizarEmprestimo(
            int emprestimoId,
            int usuarioId,
            int livroId,
            Date retirada,
            Date devolucao,
            boolean devolvido
    ) throws ClienteNaoEncontradoException, LivroNaoEncontradoException, LivroIndisponivelException {
        validarPeriodoEmprestimo(retirada, devolucao);
        Cliente usuario = buscarClientePorId(usuarioId);
        Livro livroNovo = buscarLivroPorId(livroId);
        Emprestimo atual = buscarEmprestimoPorId(emprestimoId);
        if (atual == null) {
            return false;
        }

        Livro livroAnterior = atual.getLivro();
        boolean antigoAberto = !atual.isDevolvido();
        boolean mudouLivro = livroAnterior.getId() != livroNovo.getId();
        if (!devolvido && (!antigoAberto || mudouLivro) && !livroNovo.isDisponivel()) {
            throw new LivroIndisponivelException("Livro solicitado nao esta disponivel.");
        }

        try {
            Emprestimo editado = new Emprestimo(emprestimoId, usuario.getId(), livroNovo, retirada, devolucao);
            editado.setDevolvido(devolvido);
            boolean atualizou = emprestimoDAO.atualizar(editado);
            if (!atualizou) {
                return false;
            }

            if (antigoAberto && mudouLivro) {
                livroAnterior.setDisponivel(true);
                if (!livroDAO.atualizar(livroAnterior)) {
                    return false;
                }
            }

            if (!devolvido) {
                livroNovo.marcarEmprestado();
                return livroDAO.atualizar(livroNovo);
            }

            if (antigoAberto && !mudouLivro) {
                livroNovo.marcarDevolvido();
                return livroDAO.atualizar(livroNovo);
            }

            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao atualizar emprestimo.", e);
        }
    }

    public boolean deletarEmprestimo(int emprestimoId) {
        validarIdPositivo(emprestimoId, "Id do emprestimo");
        try {
            Optional<Emprestimo> emprestimoOpt = emprestimoDAO.buscarPorId(emprestimoId);
            if (emprestimoOpt.isEmpty()) {
                return false;
            }
            Emprestimo emprestimo = emprestimoOpt.get();
            boolean removido = emprestimoDAO.deletar(emprestimoId);
            if (!removido) {
                return false;
            }
            if (!emprestimo.isDevolvido()) {
                Livro livro = emprestimo.getLivro();
                livro.setDisponivel(true);
                livroDAO.atualizar(livro);
            }
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao remover emprestimo.", e);
        }
    }

    public double calcularValorMultasPendentes(int clienteId) throws ClienteNaoEncontradoException {
        double total = 0.0;
        for (Multa multa : listarMultasDoCliente(clienteId)) {
            if (!multa.isPaga()) {
                total += multa.getValor();
            }
        }
        return total;
    }

    public int quantidadeEmprestimosEmAberto(int clienteId) throws ClienteNaoEncontradoException {
        return listarEmprestimosAbertos(clienteId).size();
    }

    private void validarEmprestimo(Cliente cliente, Livro livro)
            throws LivroIndisponivelException, ClienteComMultasPendentesException {
        if (!livro.isDisponivel()) {
            throw new LivroIndisponivelException("Livro solicitado nao esta disponivel.");
        }
        try {
            if (calcularValorMultasPendentes(cliente.getId()) > 0.0) {
                throw new ClienteComMultasPendentesException(
                        "Cliente possui multas pendentes e nao pode realizar emprestimo."
                );
            }
        } catch (ClienteNaoEncontradoException e) {
            throw new IllegalStateException("Erro ao validar emprestimo.", e);
        }
    }

    private String validarNome(String nome, String campo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " e obrigatorio.");
        }
        return nome.trim();
    }

    private void validarIdPositivo(int id, String campo) {
        if (id <= 0) {
            throw new IllegalArgumentException(campo + " deve ser positivo.");
        }
    }

    private void validarValorMulta(double valor) {
        if (!Double.isFinite(valor) || valor <= 0.0) {
            throw new IllegalArgumentException("Valor da multa deve ser maior que zero.");
        }
    }

    private void validarPeriodoEmprestimo(Date retirada, Date devolucao) {
        if (retirada == null || devolucao == null) {
            throw new IllegalArgumentException("Datas de retirada e devolucao sao obrigatorias.");
        }
        if (devolucao.before(retirada)) {
            throw new IllegalArgumentException("Data de devolucao nao pode ser anterior a data de retirada.");
        }
    }

    private boolean isViolacaoChaveEstrangeira(SQLException e) {
        return "23503".equals(e.getSQLState());
    }

    private Date calcularDataDevolucao(Date dataRetirada) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataRetirada);
        cal.add(Calendar.DAY_OF_MONTH, 14);
        return cal.getTime();
    }
}
