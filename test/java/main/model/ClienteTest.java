package main.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deveCriarClienteComIdENomeCorretos() {
        Cliente cliente = new Cliente(1, "João");
        assertEquals(1, cliente.getId());
        assertEquals("João", cliente.getNome());
    }

    @Test
    void setNomeDeveAtualizarNomeDoCliente() {
        Cliente cliente = new Cliente(1, "João");
        cliente.setNome("Maria");
        assertEquals("Maria", cliente.getNome());
    }

    @Test
    void setIdDeveAtualizarIdDoCliente() {
        Cliente cliente = new Cliente(1, "João");
        cliente.setId(99);
        assertEquals(99, cliente.getId());
    }

    @Test
    void registrarMultaDeveAdicionarMultaNaLista() {
        Cliente cliente = new Cliente(1, "João");
        cliente.registrarMulta(30.0);
        assertEquals(1, cliente.getMultas().size());
    }

    @Test
    void registrarMultaDeveRetornarIdSequencial() {
        Cliente cliente = new Cliente(1, "João");
        int id1 = cliente.registrarMulta(10.0);
        int id2 = cliente.registrarMulta(20.0);
        assertEquals(1, id1);
        assertEquals(2, id2);
    }

    @Test
    void getValorMultasPendentesDeveRetornarSomaDeTodasNaoPagas() {
        Cliente cliente = new Cliente(1, "João");
        cliente.registrarMulta(30.0);
        cliente.registrarMulta(20.0);
        assertEquals(50.0, cliente.getValorMultasPendentes(), 0.001);
    }

    @Test
    void getValorMultasPendentesDeveIgnorarMultasJaPagas() {
        Cliente cliente = new Cliente(1, "João");
        int idMulta = cliente.registrarMulta(30.0);
        cliente.registrarMulta(20.0);
        cliente.pagarMulta(idMulta);
        assertEquals(20.0, cliente.getValorMultasPendentes(), 0.001);
    }

    @Test
    void getValorMultasPendentesDeveRetornarZeroSemMultas() {
        Cliente cliente = new Cliente(1, "João");
        assertEquals(0.0, cliente.getValorMultasPendentes(), 0.001);
    }

    @Test
    void temMultasPendentesDeveRetornarTrueComMultasNaoPagas() {
        Cliente cliente = new Cliente(1, "João");
        cliente.registrarMulta(30.0);
        assertTrue(cliente.temMultasPendentes());
    }

    @Test
    void temMultasPendentesDeveRetornarFalseSemMultas() {
        Cliente cliente = new Cliente(1, "João");
        assertFalse(cliente.temMultasPendentes());
    }

    @Test
    void temMultasPendentesDeveRetornarFalseComTodasMultasPagas() {
        Cliente cliente = new Cliente(1, "João");
        int id = cliente.registrarMulta(30.0);
        cliente.pagarMulta(id);
        assertFalse(cliente.temMultasPendentes());
    }

    @Test
    void pagarMultaDeveRetornarTrueEMarcarMultaComoPaga() {
        Cliente cliente = new Cliente(1, "João");
        int idMulta = cliente.registrarMulta(30.0);
        boolean resultado = cliente.pagarMulta(idMulta);
        assertTrue(resultado);
        assertFalse(cliente.temMultasPendentes());
    }

    @Test
    void pagarMultaDeveRetornarFalseQuandoIdNaoExiste() {
        Cliente cliente = new Cliente(1, "João");
        boolean resultado = cliente.pagarMulta(99);
        assertFalse(resultado);
    }

    @Test
    void pagarMultaDeveRetornarFalseQuandoMultaJaEstaPaga() {
        Cliente cliente = new Cliente(1, "João");
        int idMulta = cliente.registrarMulta(30.0);
        cliente.pagarMulta(idMulta);
        boolean resultado = cliente.pagarMulta(idMulta);
        assertFalse(resultado);
    }

    @Test
    void registrarEmprestimoDeveAdicionarAoHistorico() {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        cliente.registrarEmprestimo(emprestimo);
        assertEquals(1, cliente.getHistoricoEmprestimos().size());
    }

    @Test
    void buscarEmprestimoPorIdDeveRetornarEmprestimoCorreto() {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(5, 1, livro, new Date(), new Date());
        cliente.registrarEmprestimo(emprestimo);
        assertEquals(emprestimo, cliente.buscarEmprestimoPorId(5));
    }

    @Test
    void buscarEmprestimoPorIdDeveRetornarNullQuandoNaoEncontrado() {
        Cliente cliente = new Cliente(1, "João");
        assertNull(cliente.buscarEmprestimoPorId(99));
    }

    @Test
    void getQuantidadeEmprestimosEmAbertoDeveContarApenasNaoDevolvidos() {
        Cliente cliente = new Cliente(1, "João");
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo aberto = new Emprestimo(1, 1, livro, new Date(), new Date());
        Emprestimo fechado = new Emprestimo(2, 1, livro, new Date(), new Date());
        fechado.setDevolvido(true);
        cliente.registrarEmprestimo(aberto);
        cliente.registrarEmprestimo(fechado);
        assertEquals(1, cliente.getQuantidadeEmprestimosEmAberto());
    }

    @Test
    void getQuantidadeEmprestimosEmAbertoDeveRetornarZeroSemEmprestimos() {
        Cliente cliente = new Cliente(1, "João");
        assertEquals(0, cliente.getQuantidadeEmprestimosEmAberto());
    }
}
