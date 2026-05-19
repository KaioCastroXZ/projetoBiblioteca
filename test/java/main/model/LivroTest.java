package main.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LivroTest {

    @Test
    void deveCriarLivroComIdENomeCorretos() {
        Livro livro = new Livro(1, "Clean Code");
        assertEquals(1, livro.getId());
        assertEquals("Clean Code", livro.getNome());
    }

    @Test
    void livroCriadoDeveEstarDisponivelPorPadrao() {
        Livro livro = new Livro(1, "Clean Code");
        assertTrue(livro.isDisponivel());
    }

    @Test
    void marcarEmprestadoDeveSetarDisponibilidadeComoFalso() {
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        assertFalse(livro.isDisponivel());
    }

    @Test
    void marcarDevolvidoDeveSetarDisponibilidadeComoVerdadeiro() {
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        livro.marcarDevolvido();
        assertTrue(livro.isDisponivel());
    }

    @Test
    void setNomeDeveAtualizarNome() {
        Livro livro = new Livro(1, "Clean Code");
        livro.setNome("Refactoring");
        assertEquals("Refactoring", livro.getNome());
    }

    @Test
    void setDisponivelFalsoDeveMarcarIndisponivel() {
        Livro livro = new Livro(1, "Clean Code");
        livro.setDisponivel(false);
        assertFalse(livro.isDisponivel());
    }

    @Test
    void setDisponivelVerdadeiroDeveMarcarDisponivel() {
        Livro livro = new Livro(1, "Clean Code");
        livro.marcarEmprestado();
        livro.setDisponivel(true);
        assertTrue(livro.isDisponivel());
    }

    @Test
    void setIdDeveAtualizarId() {
        Livro livro = new Livro(1, "Clean Code");
        livro.setId(42);
        assertEquals(42, livro.getId());
    }
}
