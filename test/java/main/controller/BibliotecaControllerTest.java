package main.controller;

import main.dao.EmprestimoDAO;
import main.dao.LivroDAO;
import main.dao.MultaDAO;
import main.dao.UsuarioDAO;
import main.exception.ClienteComMultasPendentesException;
import main.exception.ClienteNaoEncontradoException;
import main.exception.LivroIndisponivelException;
import main.exception.LivroNaoEncontradoException;
import main.model.Cliente;
import main.model.Emprestimo;
import main.model.Livro;
import main.model.Multa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BibliotecaControllerTest {

    @Mock
    private LivroDAO livroDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private EmprestimoDAO emprestimoDAO;

    @Mock
    private MultaDAO multaDAO;

    private BibliotecaController controller;

    @BeforeEach
    void setUp() {
        controller = new BibliotecaController(livroDAO, usuarioDAO, emprestimoDAO, multaDAO);
    }

    // ── cadastrarLivro ──────────────────────────────────────────────────────

    @Test
    void cadastrarLivroDeveRetornarLivroSalvoNoDAO() throws SQLException {
        Livro esperado = new Livro(1, "Clean Code");
        when(livroDAO.inserir(any(Livro.class))).thenReturn(esperado);

        Livro resultado = controller.cadastrarLivro("Clean Code");

        assertEquals(esperado, resultado);
    }

    @Test
    void cadastrarLivroDeveLancarIllegalStateExceptionQuandoDAOFalha() throws SQLException {
        when(livroDAO.inserir(any(Livro.class))).thenThrow(new SQLException("Erro de banco"));

        assertThrows(IllegalStateException.class, () -> controller.cadastrarLivro("Clean Code"));
    }

    // ── cadastrarCliente ────────────────────────────────────────────────────

    @Test
    void cadastrarClienteDeveRetornarClienteSalvoNoDAO() throws SQLException {
        Cliente esperado = new Cliente(1, "João");
        when(usuarioDAO.inserir(any(Cliente.class))).thenReturn(esperado);

        Cliente resultado = controller.cadastrarCliente("João");

        assertEquals(esperado, resultado);
    }

    @Test
    void cadastrarClienteDeveLancarIllegalStateExceptionQuandoDAOFalha() throws SQLException {
        when(usuarioDAO.inserir(any(Cliente.class))).thenThrow(new SQLException("Erro"));

        assertThrows(IllegalStateException.class, () -> controller.cadastrarCliente("João"));
    }

    // ── buscarClientePorId ──────────────────────────────────────────────────

    @Test
    void buscarClientePorIdDeveRetornarClienteQuandoEncontrado() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));

        Cliente resultado = controller.buscarClientePorId(1);

        assertEquals(cliente, resultado);
    }

    @Test
    void buscarClientePorIdDeveLancarClienteNaoEncontradoExceptionQuandoAusenteNoDAO() throws SQLException {
        when(usuarioDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.buscarClientePorId(99));
    }

    @Test
    void buscarClientePorIdDeveLancarIllegalStateExceptionQuandoDAOLancaSQLException() throws SQLException {
        when(usuarioDAO.buscarPorId(1)).thenThrow(new SQLException("Erro"));

        assertThrows(IllegalStateException.class, () -> controller.buscarClientePorId(1));
    }

    // ── buscarLivroPorId ────────────────────────────────────────────────────

    @Test
    void buscarLivroPorIdDeveRetornarLivroQuandoEncontrado() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));

        Livro resultado = controller.buscarLivroPorId(1);

        assertEquals(livro, resultado);
    }

    @Test
    void buscarLivroPorIdDeveLancarLivroNaoEncontradoExceptionQuandoAusenteNoDAO() throws SQLException {
        when(livroDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> controller.buscarLivroPorId(99));
    }

    // ── emprestarLivro ──────────────────────────────────────────────────────

    @Test
    void emprestarLivroDeveRetornarEmprestimoQuandoClienteELivroValidos() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo esperado = new Emprestimo(1, 1, livro, new Date(), new Date());

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(Collections.emptyList());
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenReturn(esperado);

        Emprestimo resultado = controller.emprestarLivro(1, 1);

        assertEquals(esperado, resultado);
    }

    @Test
    void emprestarLivroDeveMarcaLivroComoIndisponivelAposEmprestimo() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(Collections.emptyList());
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenAnswer(i -> i.getArgument(0));

        controller.emprestarLivro(1, 1);

        assertFalse(livro.isDisponivel());
        verify(livroDAO).atualizar(livro);
    }

    @Test
    void emprestarLivroDeveCalcularDataDevolucaoComQuatorzeDias() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(Collections.emptyList());

        ArgumentCaptor<Emprestimo> captor = ArgumentCaptor.forClass(Emprestimo.class);
        when(emprestimoDAO.inserir(captor.capture())).thenAnswer(i -> i.getArgument(0));

        controller.emprestarLivro(1, 1);

        Emprestimo capturado = captor.getValue();
        long diffMs = capturado.getDataDevolucao().getTime() - capturado.getDataRetirada().getTime();
        long diffDias = diffMs / (1000L * 60 * 60 * 24);
        assertEquals(14, diffDias);
    }

    @Test
    void emprestarLivroDeveLancarLivroIndisponivelExceptionQuandoLivroJaEmprestado() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));

        assertThrows(LivroIndisponivelException.class, () -> controller.emprestarLivro(1, 1));
    }

    @Test
    void emprestarLivroDeveLancarClienteComMultasPendentesExceptionQuandoClienteTemMulta() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Multa multaPendente = new Multa(1, 50.0);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multaPendente));

        assertThrows(ClienteComMultasPendentesException.class, () -> controller.emprestarLivro(1, 1));
    }

    @Test
    void emprestarLivroDeveLancarClienteNaoEncontradoExceptionQuandoClienteInexistente() throws Exception {
        when(usuarioDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.emprestarLivro(99, 1));
    }

    @Test
    void emprestarLivroDeveLancarLivroNaoEncontradoExceptionQuandoLivroInexistente() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> controller.emprestarLivro(1, 99));
    }

    // ── devolverLivro ───────────────────────────────────────────────────────

    @Test
    void devolverLivroDeveRetornarTrueEMarcarEmprestimoComoDevolvido() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));
        when(emprestimoDAO.atualizar(any(Emprestimo.class))).thenReturn(true);

        boolean resultado = controller.devolverLivro(1, 1);

        assertTrue(resultado);
        assertTrue(emprestimo.isDevolvido());
        assertTrue(livro.isDisponivel());
        verify(livroDAO).atualizar(livro);
    }

    @Test
    void devolverLivroDeveRetornarFalseQuandoEmprestimoNaoEncontrado() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        boolean resultado = controller.devolverLivro(1, 99);

        assertFalse(resultado);
    }

    @Test
    void devolverLivroDeveRetornarFalseQuandoEmprestimoJaFoiDevolvido() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.setDevolvido(true);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));

        boolean resultado = controller.devolverLivro(1, 1);

        assertFalse(resultado);
    }

    @Test
    void devolverLivroDeveRetornarFalseQuandoClienteNaoCorrespondente() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 2, livro, new Date(), new Date());

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));

        boolean resultado = controller.devolverLivro(1, 1);

        assertFalse(resultado);
    }

    // ── registrarMulta ──────────────────────────────────────────────────────

    @Test
    void registrarMultaDeveRetornarIdDaMultaCriada() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Multa multa = new Multa(5, 30.0);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.inserir(1, 30.0)).thenReturn(multa);

        int resultado = controller.registrarMulta(1, 30.0);

        assertEquals(5, resultado);
    }

    @Test
    void registrarMultaDeveLancarClienteNaoEncontradoExceptionSeClienteInexistente() throws SQLException {
        when(usuarioDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.registrarMulta(99, 30.0));
    }

    // ── pagarMulta ──────────────────────────────────────────────────────────

    @Test
    void pagarMultaDeveRetornarTrueEMarcarMultaComoPaga() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Multa multa = new Multa(1, 30.0);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multa));
        when(multaDAO.atualizar(multa)).thenReturn(true);

        boolean resultado = controller.pagarMulta(1, 1);

        assertTrue(resultado);
        assertTrue(multa.isPaga());
    }

    @Test
    void pagarMultaDeveRetornarFalseQuandoMultaNaoEncontrada() throws Exception {
        Cliente cliente = new Cliente(1, "João");

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(Collections.emptyList());

        boolean resultado = controller.pagarMulta(1, 99);

        assertFalse(resultado);
    }

    @Test
    void pagarMultaDeveRetornarFalseQuandoMultaJaEstaPaga() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Multa multa = new Multa(1, 30.0);
        multa.pagar();

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multa));

        boolean resultado = controller.pagarMulta(1, 1);

        assertFalse(resultado);
    }

    // ── listarClientes ──────────────────────────────────────────────────────

    @Test
    void listarClientesDeveRetornarListaCompletaDoDAO() throws Exception {
        List<Cliente> clientes = List.of(new Cliente(1, "João"), new Cliente(2, "Maria"));
        when(usuarioDAO.listarTodos()).thenReturn(clientes);

        List<Cliente> resultado = controller.listarClientes();

        assertEquals(2, resultado.size());
    }

    // ── listarLivros ────────────────────────────────────────────────────────

    @Test
    void listarLivrosDeveRetornarListaCompletaDoDAO() throws Exception {
        List<Livro> livros = List.of(new Livro(1, "Clean Code"), new Livro(2, "Refactoring"));
        when(livroDAO.listarTodos()).thenReturn(livros);

        List<Livro> resultado = controller.listarLivros();

        assertEquals(2, resultado.size());
    }

    // ── listarEmprestimos ───────────────────────────────────────────────────

    @Test
    void listarEmprestimosDeveRetornarListaCompletaDoDAO() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        List<Emprestimo> lista = List.of(new Emprestimo(1, 1, livro, new Date(), new Date()));
        when(emprestimoDAO.listarTodos()).thenReturn(lista);

        List<Emprestimo> resultado = controller.listarEmprestimos();

        assertEquals(1, resultado.size());
    }

    // ── calcularValorMultasPendentes ────────────────────────────────────────

    @Test
    void calcularValorMultasPendentesDeveRetornarSomaDasMultasNaoPagas() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Multa m1 = new Multa(1, 30.0);
        Multa m2 = new Multa(2, 20.0);
        Multa m3 = new Multa(3, 10.0);
        m3.pagar();

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(m1, m2, m3));

        double resultado = controller.calcularValorMultasPendentes(1);

        assertEquals(50.0, resultado, 0.001);
    }

    @Test
    void calcularValorMultasPendentesDeveRetornarZeroQuandoTodasPagas() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Multa multa = new Multa(1, 30.0);
        multa.pagar();

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multa));

        double resultado = controller.calcularValorMultasPendentes(1);

        assertEquals(0.0, resultado, 0.001);
    }

    // ── atualizarLivro ──────────────────────────────────────────────────────

    @Test
    void atualizarLivroDeveAtualizarNomeERetornarTrue() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(livroDAO.atualizar(livro)).thenReturn(true);

        boolean resultado = controller.atualizarLivro(1, "Refactoring");

        assertTrue(resultado);
        assertEquals("Refactoring", livro.getNome());
    }

    @Test
    void atualizarLivroDeveLancarLivroNaoEncontradoExceptionSeNaoExistir() throws Exception {
        when(livroDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> controller.atualizarLivro(99, "Novo Nome"));
    }

    // ── deletarLivro ────────────────────────────────────────────────────────

    @Test
    void deletarLivroDeveRetornarTrueEChamarDeletarNoDAO() throws Exception {
        when(livroDAO.deletar(1)).thenReturn(true);

        boolean resultado = controller.deletarLivro(1);

        assertTrue(resultado);
        verify(livroDAO).deletar(1);
    }

    @Test
    void deletarLivroDeveRetornarFalseQuandoLivroNaoExisteNoDAO() throws Exception {
        when(livroDAO.deletar(99)).thenReturn(false);

        boolean resultado = controller.deletarLivro(99);

        assertFalse(resultado);
    }

    // ── atualizarCliente ────────────────────────────────────────────────────

    @Test
    void atualizarClienteDeveAtualizarNomeERetornarTrue() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(usuarioDAO.atualizar(cliente)).thenReturn(true);

        boolean resultado = controller.atualizarCliente(1, "Maria");

        assertTrue(resultado);
        assertEquals("Maria", cliente.getNome());
    }

    @Test
    void atualizarClienteDeveLancarClienteNaoEncontradoExceptionSeNaoExistir() throws Exception {
        when(usuarioDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.atualizarCliente(99, "Maria"));
    }

    // ── deletarCliente ──────────────────────────────────────────────────────

    @Test
    void deletarClienteDeveRetornarTrueEChamarDeletarNoDAO() throws Exception {
        when(usuarioDAO.deletar(1)).thenReturn(true);

        boolean resultado = controller.deletarCliente(1);

        assertTrue(resultado);
        verify(usuarioDAO).deletar(1);
    }

    // ── consultarHistorico ──────────────────────────────────────────────────

    @Test
    void consultarHistoricoDeveRetornarTodosEmprestimosDoCliente() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        List<Emprestimo> historico = List.of(new Emprestimo(1, 1, livro, new Date(), new Date()));

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.listarPorUsuario(1)).thenReturn(historico);

        List<Emprestimo> resultado = controller.consultarHistorico(1);

        assertEquals(1, resultado.size());
        verify(emprestimoDAO).listarPorUsuario(1);
    }

    // ── listarEmprestimosAbertos ────────────────────────────────────────────

    @Test
    void listarEmprestimosAbertosDeveRetornarApenasEmprestimosNaoDevolvidos() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        List<Emprestimo> abertos = List.of(new Emprestimo(1, 1, livro, new Date(), new Date()));

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.listarAbertosPorUsuario(1)).thenReturn(abertos);

        List<Emprestimo> resultado = controller.listarEmprestimosAbertos(1);

        assertEquals(1, resultado.size());
        verify(emprestimoDAO).listarAbertosPorUsuario(1);
    }

    // ── quantidadeEmprestimosEmAberto ───────────────────────────────────────

    @Test
    void quantidadeEmprestimosEmAbertoDeveRetornarContadorCorreto() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        List<Emprestimo> abertos = List.of(
                new Emprestimo(1, 1, livro, new Date(), new Date()),
                new Emprestimo(2, 1, livro, new Date(), new Date())
        );

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(emprestimoDAO.listarAbertosPorUsuario(1)).thenReturn(abertos);

        int resultado = controller.quantidadeEmprestimosEmAberto(1);

        assertEquals(2, resultado);
    }

    // ── deletarEmprestimo ───────────────────────────────────────────────────

    @Test
    void deletarEmprestimoDeveLibrarLivroQuandoEmprestimoEstaAberto() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());

        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));
        when(emprestimoDAO.deletar(1)).thenReturn(true);

        boolean resultado = controller.deletarEmprestimo(1);

        assertTrue(resultado);
        assertTrue(livro.isDisponivel());
        verify(livroDAO).atualizar(livro);
    }

    @Test
    void deletarEmprestimoNaoDeveAlterarLivroQuandoEmprestimoJaDevolvido() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.setDevolvido(true);

        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));
        when(emprestimoDAO.deletar(1)).thenReturn(true);

        boolean resultado = controller.deletarEmprestimo(1);

        assertTrue(resultado);
        verify(livroDAO, never()).atualizar(any());
    }

    @Test
    void deletarEmprestimoDeveRetornarFalseQuandoEmprestimoNaoEncontrado() throws Exception {
        when(emprestimoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        boolean resultado = controller.deletarEmprestimo(99);

        assertFalse(resultado);
    }

    // ── listarMultasDoCliente ───────────────────────────────────────────────

    @Test
    void listarMultasDoClienteDeveRetornarMultasDoDAO() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        List<Multa> multas = List.of(new Multa(1, 30.0), new Multa(2, 20.0));

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPorUsuario(1)).thenReturn(multas);

        List<Multa> resultado = controller.listarMultasDoCliente(1);

        assertEquals(2, resultado.size());
    }

    @Test
    void listarMultasDoClienteDeveLancarClienteNaoEncontradoExceptionSeClienteInexistente() throws SQLException {
        when(usuarioDAO.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.listarMultasDoCliente(99));
    }

    // ── buscarEmprestimoPorId ───────────────────────────────────────────────

    @Test
    void buscarEmprestimoPorIdDeveRetornarEmprestimoQuandoEncontrado() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.of(emprestimo));

        Emprestimo resultado = controller.buscarEmprestimoPorId(1);

        assertEquals(emprestimo, resultado);
    }

    @Test
    void buscarEmprestimoPorIdDeveRetornarNullQuandoNaoEncontrado() throws Exception {
        when(emprestimoDAO.buscarPorId(99)).thenReturn(Optional.empty());

        Emprestimo resultado = controller.buscarEmprestimoPorId(99);

        assertNull(resultado);
    }

    // ── cadastrarEmprestimo (manual) ────────────────────────────────────────

    @Test
    void cadastrarEmprestimoDeveRetornarTrueQuandoSalvoComSucesso() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo salvo = new Emprestimo(1, 1, livro, new Date(), new Date());
        salvo.setDevolvido(true);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenReturn(salvo);

        boolean resultado = controller.cadastrarEmprestimo(1, 1, new Date(), new Date(), true);

        assertTrue(resultado);
    }

    @Test
    void cadastrarEmprestimoAbertoDeveLancarExcecaoQuandoLivroIndisponivel() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));

        assertThrows(LivroIndisponivelException.class,
                () -> controller.cadastrarEmprestimo(1, 1, new Date(), new Date(), false));
    }

    @Test
    void cadastrarEmprestimoJaDevolvidoNaoDeveValidarDisponibilidadeDoLivro() throws Exception {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        Emprestimo salvo = new Emprestimo(1, 1, livro, new Date(), new Date());
        salvo.setDevolvido(true);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenReturn(salvo);

        assertDoesNotThrow(() -> controller.cadastrarEmprestimo(1, 1, new Date(), new Date(), true));
    }
}
