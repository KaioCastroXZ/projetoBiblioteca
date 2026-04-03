package main.controller;

import main.exception.ClienteComMultasPendentesException;
import main.exception.ClienteNaoEncontradoException;
import main.exception.LivroIndisponivelException;
import main.exception.LivroNaoEncontradoException;
import main.model.Cliente;
import main.model.Emprestimo;
import main.model.Livro;
import main.model.Multa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BibliotecaController {

    private final List<Livro> livros = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();

    public Livro cadastrarLivro(String nome) {
        Livro livro = new Livro(livros.size() + 1, nome);
        livros.add(livro);
        return livro;
    }

    public Cliente cadastrarCliente(String nome) {
        Cliente cliente = new Cliente(clientes.size() + 1, nome);
        clientes.add(cliente);
        return cliente;
    }

    public Emprestimo emprestarLivro(int clienteId, int livroId)
            throws ClienteNaoEncontradoException, LivroNaoEncontradoException,
            LivroIndisponivelException, ClienteComMultasPendentesException {
        Cliente cliente = buscarClientePorId(clienteId);
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
        return emprestimo;
    }

    public int registrarMulta(int clienteId, double valor) throws ClienteNaoEncontradoException {
        Cliente cliente = buscarClientePorId(clienteId);
        return cliente.registrarMulta(valor);
    }

    public boolean pagarMulta(int clienteId, int multaId) throws ClienteNaoEncontradoException {
        Cliente cliente = buscarClientePorId(clienteId);
        return cliente.pagarMulta(multaId);
    }

    public Cliente buscarClientePorId(int id) throws ClienteNaoEncontradoException {
        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }
        throw new ClienteNaoEncontradoException("Cliente nao encontrado.");
    }

    public Livro buscarLivroPorId(int id) throws LivroNaoEncontradoException {
        for (Livro livro : livros) {
            if (livro.getId() == id) {
                return livro;
            }
        }
        throw new LivroNaoEncontradoException("Livro nao encontrado no sistema.");
    }

    public List<Cliente> listarClientes() {
        return new ArrayList<>(clientes);
    }

    public List<Livro> listarLivros() {
        return new ArrayList<>(livros);
    }

    public List<Multa> listarMultasDoCliente(int clienteId) throws ClienteNaoEncontradoException {
        return new ArrayList<>(buscarClientePorId(clienteId).getMultas());
    }

    public List<Emprestimo> consultarHistorico(int clienteId) throws ClienteNaoEncontradoException {
        return new ArrayList<>(buscarClientePorId(clienteId).getHistoricoEmprestimos());
    }

    public List<Emprestimo> listarEmprestimosAbertos(int clienteId) throws ClienteNaoEncontradoException {
        List<Emprestimo> emprestimosAbertos = new ArrayList<>();
        for (Emprestimo emprestimo : buscarClientePorId(clienteId).getHistoricoEmprestimos()) {
            if (!emprestimo.isDevolvido()) {
                emprestimosAbertos.add(emprestimo);
            }
        }
        return emprestimosAbertos;
    }

    public boolean devolverLivro(int clienteId, int emprestimoId) throws ClienteNaoEncontradoException {
        Cliente cliente = buscarClientePorId(clienteId);
        Emprestimo emprestimo = cliente.buscarEmprestimoPorId(emprestimoId);

        if (emprestimo == null || emprestimo.isDevolvido()) {
            return false;
        }

        emprestimo.marcarDevolvido();
        emprestimo.getLivro().marcarDevolvido();
        return true;
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
}
