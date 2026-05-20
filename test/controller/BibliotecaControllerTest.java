package controller;

import main.controller.BibliotecaController;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void cadastrarLivroInsereLivroNoDao() throws Exception {
        Livro salvo = new Livro(1, "Clean Code");
        when(livroDAO.inserir(any(Livro.class))).thenReturn(salvo);

        Livro resultado = controller.cadastrarLivro("Clean Code");

        assertEquals(salvo, resultado);
    }

    @Test
    void cadastrarClienteInsereClienteNoDao() throws Exception {
        Cliente salvo = new Cliente(1, "Ana");
        when(usuarioDAO.inserir(any(Cliente.class))).thenReturn(salvo);

        Cliente resultado = controller.cadastrarCliente("Ana");

        assertEquals(salvo, resultado);
    }

    @Test
    void buscarClientePorIdRetornaClienteEncontrado() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));

        Cliente resultado = controller.buscarClientePorId(1);

        assertEquals(cliente, resultado);
    }

    @Test
    void buscarClientePorIdLancaExcecaoQuandoNaoExiste() throws Exception {
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> controller.buscarClientePorId(1));
    }

    @Test
    void buscarLivroPorIdRetornaLivroEncontrado() throws Exception {
        Livro livro = new Livro(1, "Clean Code");
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.of(livro));

        Livro resultado = controller.buscarLivroPorId(1);

        assertEquals(livro, resultado);
    }

    @Test
    void buscarLivroPorIdLancaExcecaoQuandoNaoExiste() throws Exception {
        when(livroDAO.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> controller.buscarLivroPorId(1));
    }

    @Test
    void emprestarLivroRegistraEmprestimoEIndisponibilizaLivro() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livro = new Livro(2, "Clean Code");
        Emprestimo salvo = new Emprestimo(5, cliente.getId(), livro, new Date(), new Date());
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of());
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenReturn(salvo);

        Emprestimo resultado = controller.emprestarLivro(1, 2);

        assertEquals(salvo, resultado);
        assertFalse(livro.isDisponivel());
        verify(livroDAO).atualizar(livro);
    }

    @Test
    void emprestarLivroCalculaDevolucaoParaQuatorzeDiasDepoisDaRetirada() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livro = new Livro(2, "Clean Code");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of());
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        controller.emprestarLivro(1, 2);

        ArgumentCaptor<Emprestimo> captor = ArgumentCaptor.forClass(Emprestimo.class);
        verify(emprestimoDAO).inserir(captor.capture());
        Emprestimo enviado = captor.getValue();
        long dias = TimeUnit.MILLISECONDS.toDays(
                enviado.getDataDevolucao().getTime() - enviado.getDataRetirada().getTime()
        );
        assertEquals(14, dias);
    }

    @Test
    void emprestarLivroLancaExcecaoQuandoLivroIndisponivel() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livro = new Livro(2, "Clean Code");
        livro.marcarEmprestado();
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));

        assertThrows(LivroIndisponivelException.class, () -> controller.emprestarLivro(1, 2));
        verify(emprestimoDAO, never()).inserir(any(Emprestimo.class));
    }

    @Test
    void emprestarLivroLancaExcecaoQuandoClienteTemMultaPendente() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livro = new Livro(2, "Clean Code");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(new Multa(7, 10.00)));

        assertThrows(ClienteComMultasPendentesException.class, () -> controller.emprestarLivro(1, 2));
        verify(emprestimoDAO, never()).inserir(any(Emprestimo.class));
    }

    @Test
    void cadastrarEmprestimoDevolvidoNaoIndisponibilizaLivro() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livro = new Livro(2, "Clean Code");
        Date retirada = new Date();
        Date devolucao = new Date();
        Emprestimo salvo = new Emprestimo(9, cliente.getId(), livro, retirada, devolucao);
        salvo.setDevolvido(true);
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));
        when(emprestimoDAO.inserir(any(Emprestimo.class))).thenReturn(salvo);

        boolean cadastrado = controller.cadastrarEmprestimo(1, 2, retirada, devolucao, true);

        assertTrue(cadastrado);
        assertTrue(livro.isDisponivel());
        verify(livroDAO, never()).atualizar(livro);
    }

    @Test
    void registrarMultaRetornaIdDaMultaCriada() throws Exception {
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(multaDAO.inserir(1, 15.00)).thenReturn(new Multa(3, 15.00));

        int id = controller.registrarMulta(1, 15.00);

        assertEquals(3, id);
    }

    @Test
    void pagarMultaPagaMultaAberta() throws Exception {
        Multa multa = new Multa(3, 15.00);
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multa));
        when(multaDAO.atualizar(multa)).thenReturn(true);

        boolean pagou = controller.pagarMulta(1, 3);

        assertTrue(pagou);
        assertTrue(multa.isPaga());
    }

    @Test
    void pagarMultaRetornaFalseQuandoMultaNaoEstaAberta() throws Exception {
        Multa multa = new Multa(3, 15.00);
        multa.pagar();
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(multa));

        boolean pagou = controller.pagarMulta(1, 3);

        assertFalse(pagou);
    }

    @Test
    void buscarEmprestimoPorIdRetornaNullQuandoNaoExiste() throws Exception {
        when(emprestimoDAO.buscarPorId(1)).thenReturn(Optional.empty());

        Emprestimo resultado = controller.buscarEmprestimoPorId(1);

        assertNull(resultado);
    }

    @Test
    void listarClientesRetornaListaDoDao() throws Exception {
        List<Cliente> clientes = List.of(new Cliente(1, "Ana"));
        when(usuarioDAO.listarTodos()).thenReturn(clientes);

        assertEquals(clientes, controller.listarClientes());
    }

    @Test
    void listarLivrosRetornaListaDoDao() throws Exception {
        List<Livro> livros = List.of(new Livro(1, "Clean Code"));
        when(livroDAO.listarTodos()).thenReturn(livros);

        assertEquals(livros, controller.listarLivros());
    }

    @Test
    void listarEmprestimosRetornaListaDoDao() throws Exception {
        List<Emprestimo> emprestimos = List.of(novoEmprestimo(1, 1, new Livro(1, "Livro")));
        when(emprestimoDAO.listarTodos()).thenReturn(emprestimos);

        assertEquals(emprestimos, controller.listarEmprestimos());
    }

    @Test
    void listarMultasDoClienteRetornaMultasDoDao() throws Exception {
        List<Multa> multas = List.of(new Multa(1, 10.00));
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(multaDAO.listarPorUsuario(1)).thenReturn(multas);

        assertEquals(multas, controller.listarMultasDoCliente(1));
    }

    @Test
    void consultarHistoricoRetornaEmprestimosDoCliente() throws Exception {
        List<Emprestimo> historico = List.of(novoEmprestimo(1, 1, new Livro(1, "Livro")));
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(emprestimoDAO.listarPorUsuario(1)).thenReturn(historico);

        assertEquals(historico, controller.consultarHistorico(1));
    }

    @Test
    void listarEmprestimosAbertosRetornaEmprestimosAbertosDoCliente() throws Exception {
        List<Emprestimo> abertos = List.of(novoEmprestimo(1, 1, new Livro(1, "Livro")));
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(emprestimoDAO.listarAbertosPorUsuario(1)).thenReturn(abertos);

        assertEquals(abertos, controller.listarEmprestimosAbertos(1));
    }

    @Test
    void devolverLivroMarcaEmprestimoComoDevolvidoELiberaLivro() throws Exception {
        Livro livro = new Livro(2, "Clean Code");
        livro.marcarEmprestado();
        Emprestimo emprestimo = novoEmprestimo(5, 1, livro);
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(emprestimoDAO.buscarPorId(5)).thenReturn(Optional.of(emprestimo));
        when(emprestimoDAO.atualizar(emprestimo)).thenReturn(true);

        boolean devolvido = controller.devolverLivro(1, 5);

        assertTrue(devolvido);
        assertTrue(emprestimo.isDevolvido());
        assertTrue(livro.isDisponivel());
    }

    @Test
    void devolverLivroRetornaFalseQuandoEmprestimoPertenceAOutroCliente() throws Exception {
        Emprestimo emprestimo = novoEmprestimo(5, 2, new Livro(2, "Clean Code"));
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(emprestimoDAO.buscarPorId(5)).thenReturn(Optional.of(emprestimo));

        boolean devolvido = controller.devolverLivro(1, 5);

        assertFalse(devolvido);
    }

    @Test
    void atualizarLivroAlteraNomeDoLivro() throws Exception {
        Livro livro = new Livro(2, "Nome antigo");
        when(livroDAO.buscarPorId(2)).thenReturn(Optional.of(livro));
        when(livroDAO.atualizar(livro)).thenReturn(true);

        boolean atualizado = controller.atualizarLivro(2, "Nome novo");

        assertTrue(atualizado);
        assertEquals("Nome novo", livro.getNome());
    }

    @Test
    void deletarLivroRetornaResultadoDoDao() throws Exception {
        when(livroDAO.deletar(2)).thenReturn(true);

        assertTrue(controller.deletarLivro(2));
    }

    @Test
    void atualizarClienteAlteraNomeDoCliente() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(usuarioDAO.atualizar(cliente)).thenReturn(true);

        boolean atualizado = controller.atualizarCliente(1, "Maria");

        assertTrue(atualizado);
        assertEquals("Maria", cliente.getNome());
    }

    @Test
    void deletarClienteRetornaResultadoDoDao() throws Exception {
        when(usuarioDAO.deletar(1)).thenReturn(true);

        assertTrue(controller.deletarCliente(1));
    }

    @Test
    void atualizarEmprestimoLiberaLivroAnteriorQuandoLivroMuda() throws Exception {
        Cliente cliente = new Cliente(1, "Ana");
        Livro livroAnterior = new Livro(2, "Antigo");
        livroAnterior.marcarEmprestado();
        Livro livroNovo = new Livro(3, "Novo");
        Emprestimo atual = novoEmprestimo(5, 1, livroAnterior);
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));
        when(livroDAO.buscarPorId(3)).thenReturn(Optional.of(livroNovo));
        when(emprestimoDAO.buscarPorId(5)).thenReturn(Optional.of(atual));
        when(emprestimoDAO.atualizar(any(Emprestimo.class))).thenReturn(true);
        when(livroDAO.atualizar(any(Livro.class))).thenReturn(true);

        boolean atualizado = controller.atualizarEmprestimo(5, 1, 3, new Date(), new Date(), false);

        assertTrue(atualizado);
        assertTrue(livroAnterior.isDisponivel());
        assertFalse(livroNovo.isDisponivel());
    }

    @Test
    void deletarEmprestimoAbertoLiberaLivro() throws Exception {
        Livro livro = new Livro(2, "Clean Code");
        livro.marcarEmprestado();
        Emprestimo emprestimo = novoEmprestimo(5, 1, livro);
        when(emprestimoDAO.buscarPorId(5)).thenReturn(Optional.of(emprestimo));
        when(emprestimoDAO.deletar(5)).thenReturn(true);

        boolean deletado = controller.deletarEmprestimo(5);

        assertTrue(deletado);
        assertTrue(livro.isDisponivel());
    }

    @Test
    void calcularValorMultasPendentesSomaMultasNaoPagas() throws Exception {
        Multa paga = new Multa(1, 10.00);
        paga.pagar();
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(multaDAO.listarPorUsuario(1)).thenReturn(List.of(paga, new Multa(2, 7.50)));

        double total = controller.calcularValorMultasPendentes(1);

        assertEquals(7.50, total);
    }

    @Test
    void quantidadeEmprestimosEmAbertoRetornaTamanhoDaListaDeAbertos() throws Exception {
        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(new Cliente(1, "Ana")));
        when(emprestimoDAO.listarAbertosPorUsuario(1)).thenReturn(List.of(
                novoEmprestimo(1, 1, new Livro(1, "A")),
                novoEmprestimo(2, 1, new Livro(2, "B"))
        ));

        int quantidade = controller.quantidadeEmprestimosEmAberto(1);

        assertEquals(2, quantidade);
    }

    @Test
    void erroDoDaoEConvertidoEmIllegalStateException() throws Exception {
        when(livroDAO.listarTodos()).thenThrow(new SQLException("falha"));

        assertThrows(IllegalStateException.class, () -> controller.listarLivros());
    }

    private Emprestimo novoEmprestimo(int id, int clienteId, Livro livro) {
        return new Emprestimo(id, clienteId, livro, new Date(), new Date());
    }
}
