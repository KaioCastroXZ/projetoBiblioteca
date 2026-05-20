package model;

import main.model.Cliente;
import main.model.Emprestimo;
import main.model.Livro;
import main.model.Multa;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void registrarMultaCriaMultaComIdSequencial() {
        Cliente cliente = new Cliente(1, "Ana");

        int id = cliente.registrarMulta(12.50);

        assertEquals(1, id);
        assertEquals(1, cliente.getMultas().size());
        assertEquals(12.50, cliente.getMultas().get(0).getValor());
    }

    @Test
    void getValorMultasPendentesSomaApenasMultasNaoPagas() {
        Cliente cliente = new Cliente(1, "Ana");
        int multaPaga = cliente.registrarMulta(10.00);
        cliente.registrarMulta(7.50);
        cliente.pagarMulta(multaPaga);

        double total = cliente.getValorMultasPendentes();

        assertEquals(7.50, total);
    }

    @Test
    void temMultasPendentesRetornaTrueQuandoHaMultaAberta() {
        Cliente cliente = new Cliente(1, "Ana");
        cliente.registrarMulta(10.00);

        assertTrue(cliente.temMultasPendentes());
    }

    @Test
    void pagarMultaMarcaMultaAbertaComoPaga() {
        Cliente cliente = new Cliente(1, "Ana");
        int idMulta = cliente.registrarMulta(10.00);

        boolean pagou = cliente.pagarMulta(idMulta);

        assertTrue(pagou);
        assertTrue(cliente.getMultas().get(0).isPaga());
    }

    @Test
    void pagarMultaRetornaFalseQuandoMultaNaoExiste() {
        Cliente cliente = new Cliente(1, "Ana");

        boolean pagou = cliente.pagarMulta(99);

        assertFalse(pagou);
    }

    @Test
    void pagarMultaRetornaFalseQuandoMultaJaFoiPaga() {
        Cliente cliente = new Cliente(1, "Ana");
        int idMulta = cliente.registrarMulta(10.00);
        cliente.pagarMulta(idMulta);

        boolean pagouNovamente = cliente.pagarMulta(idMulta);

        assertFalse(pagouNovamente);
    }

    @Test
    void registrarEmprestimoAdicionaAoHistorico() {
        Cliente cliente = new Cliente(1, "Ana");
        Emprestimo emprestimo = novoEmprestimo(1, 1, new Livro(1, "Livro"));

        cliente.registrarEmprestimo(emprestimo);

        assertEquals(emprestimo, cliente.getHistoricoEmprestimos().get(0));
    }

    @Test
    void buscarEmprestimoPorIdRetornaEmprestimoDoHistorico() {
        Cliente cliente = new Cliente(1, "Ana");
        Emprestimo emprestimo = novoEmprestimo(10, 1, new Livro(1, "Livro"));
        cliente.registrarEmprestimo(emprestimo);

        Emprestimo encontrado = cliente.buscarEmprestimoPorId(10);

        assertEquals(emprestimo, encontrado);
    }

    @Test
    void buscarEmprestimoPorIdRetornaNullQuandoNaoExiste() {
        Cliente cliente = new Cliente(1, "Ana");

        Emprestimo encontrado = cliente.buscarEmprestimoPorId(99);

        assertNull(encontrado);
    }

    @Test
    void getQuantidadeEmprestimosEmAbertoContaSomenteNaoDevolvidos() {
        Cliente cliente = new Cliente(1, "Ana");
        Emprestimo aberto = novoEmprestimo(1, 1, new Livro(1, "Livro A"));
        Emprestimo devolvido = novoEmprestimo(2, 1, new Livro(2, "Livro B"));
        devolvido.marcarDevolvido();
        cliente.registrarEmprestimo(aberto);
        cliente.registrarEmprestimo(devolvido);

        int quantidade = cliente.getQuantidadeEmprestimosEmAberto();

        assertEquals(1, quantidade);
    }

    @Test
    void livroMarcarEmprestadoDeixaIndisponivel() {
        Livro livro = new Livro(1, "Livro");

        livro.marcarEmprestado();

        assertFalse(livro.isDisponivel());
    }

    @Test
    void livroMarcarDevolvidoDeixaDisponivel() {
        Livro livro = new Livro(1, "Livro");
        livro.marcarEmprestado();

        livro.marcarDevolvido();

        assertTrue(livro.isDisponivel());
    }

    @Test
    void emprestimoNovoComecaNaoDevolvido() {
        Emprestimo emprestimo = novoEmprestimo(1, 1, new Livro(1, "Livro"));

        assertFalse(emprestimo.isDevolvido());
    }

    @Test
    void marcarDevolvidoAlteraStatusDoEmprestimo() {
        Emprestimo emprestimo = novoEmprestimo(1, 1, new Livro(1, "Livro"));

        emprestimo.marcarDevolvido();

        assertTrue(emprestimo.isDevolvido());
    }

    @Test
    void multaNovaComecaNaoPaga() {
        Multa multa = new Multa(1, 10.00);

        assertFalse(multa.isPaga());
    }

    @Test
    void pagarMultaAlteraStatusDaMulta() {
        Multa multa = new Multa(1, 10.00);

        multa.pagar();

        assertTrue(multa.isPaga());
    }

    private Emprestimo novoEmprestimo(int id, int clienteId, Livro livro) {
        return new Emprestimo(id, clienteId, livro, new Date(), new Date());
    }
}
