package main.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void clienteNaoEncontradoExceptionDeveConterMensagem() {
        ClienteNaoEncontradoException ex = new ClienteNaoEncontradoException("Cliente não encontrado");
        assertEquals("Cliente não encontrado", ex.getMessage());
    }

    @Test
    void clienteNaoEncontradoExceptionDeveSerSubclasseDeException() {
        ClienteNaoEncontradoException ex = new ClienteNaoEncontradoException("msg");
        assertInstanceOf(Exception.class, ex);
    }

    @Test
    void livroNaoEncontradoExceptionDeveConterMensagem() {
        LivroNaoEncontradoException ex = new LivroNaoEncontradoException("Livro não encontrado");
        assertEquals("Livro não encontrado", ex.getMessage());
    }

    @Test
    void livroNaoEncontradoExceptionDeveSerSubclasseDeException() {
        LivroNaoEncontradoException ex = new LivroNaoEncontradoException("msg");
        assertInstanceOf(Exception.class, ex);
    }

    @Test
    void livroIndisponivelExceptionDeveConterMensagem() {
        LivroIndisponivelException ex = new LivroIndisponivelException("Livro indisponível");
        assertEquals("Livro indisponível", ex.getMessage());
    }

    @Test
    void livroIndisponivelExceptionDeveSerSubclasseDeException() {
        LivroIndisponivelException ex = new LivroIndisponivelException("msg");
        assertInstanceOf(Exception.class, ex);
    }

    @Test
    void clienteComMultasPendentesExceptionDeveConterMensagem() {
        ClienteComMultasPendentesException ex = new ClienteComMultasPendentesException("Multas pendentes");
        assertEquals("Multas pendentes", ex.getMessage());
    }

    @Test
    void clienteComMultasPendentesExceptionDeveSerSubclasseDeException() {
        ClienteComMultasPendentesException ex = new ClienteComMultasPendentesException("msg");
        assertInstanceOf(Exception.class, ex);
    }
}
