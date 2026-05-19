package main.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class EmprestimoTest {

    @Test
    void deveCriarEmprestimoComAtributosCorretos() {
        Livro livro = new Livro(1, "Clean Code");
        Date retirada = new Date();
        Date devolucao = new Date(System.currentTimeMillis() + 86400000L * 14);
        Emprestimo emprestimo = new Emprestimo(1, 2, livro, retirada, devolucao);

        assertEquals(1, emprestimo.getId());
        assertEquals(2, emprestimo.getClienteId());
        assertEquals(livro, emprestimo.getLivro());
        assertEquals(retirada, emprestimo.getDataRetirada());
        assertEquals(devolucao, emprestimo.getDataDevolucao());
    }

    @Test
    void novoEmprestimoNaoDeveEstarDevolvidoPorPadrao() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        assertFalse(emprestimo.isDevolvido());
    }

    @Test
    void marcarDevolvidoDeveAtualizarStatusParaVerdadeiro() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.marcarDevolvido();
        assertTrue(emprestimo.isDevolvido());
    }

    @Test
    void setDevolvidoVerdadeiroDeveAtualizarStatus() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.setDevolvido(true);
        assertTrue(emprestimo.isDevolvido());
    }

    @Test
    void setDevolvidoFalsoDeveManterStatusComoNaoDevolvido() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.setDevolvido(false);
        assertFalse(emprestimo.isDevolvido());
    }

    @Test
    void construtorSemClienteIdDeveUsarZeroComoClienteId() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, livro, new Date(), new Date());
        assertEquals(0, emprestimo.getClienteId());
    }

    @Test
    void setDataRetiradaDeveAtualizarDataDeRetirada() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        Date novaData = new Date(System.currentTimeMillis() + 86400000L);
        emprestimo.setDataRetirada(novaData);
        assertEquals(novaData, emprestimo.getDataRetirada());
    }

    @Test
    void setDataDevolucaoDeveAtualizarDataDeDevolucao() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        Date novaData = new Date(System.currentTimeMillis() + 86400000L * 14);
        emprestimo.setDataDevolucao(novaData);
        assertEquals(novaData, emprestimo.getDataDevolucao());
    }

    @Test
    void setLivroDeveAtualizarLivroDoEmprestimo() {
        Livro livro1 = new Livro(1, "Clean Code");
        Livro livro2 = new Livro(2, "Refactoring");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro1, new Date(), new Date());
        emprestimo.setLivro(livro2);
        assertEquals(livro2, emprestimo.getLivro());
    }

    @Test
    void setClienteIdDeveAtualizarClienteId() {
        Livro livro = new Livro(1, "Clean Code");
        Emprestimo emprestimo = new Emprestimo(1, 1, livro, new Date(), new Date());
        emprestimo.setClienteId(5);
        assertEquals(5, emprestimo.getClienteId());
    }
}
