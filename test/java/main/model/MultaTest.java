package main.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultaTest {

    @Test
    void deveCriarMultaComIdEValorCorretos() {
        Multa multa = new Multa(1, 25.0);
        assertEquals(1, multa.getId());
        assertEquals(25.0, multa.getValor(), 0.001);
    }

    @Test
    void multaCriadaNaoDeveEstarPagaPorPadrao() {
        Multa multa = new Multa(1, 25.0);
        assertFalse(multa.isPaga());
    }

    @Test
    void pagarDeveMarcarMultaComoPaga() {
        Multa multa = new Multa(1, 25.0);
        multa.pagar();
        assertTrue(multa.isPaga());
    }

    @Test
    void setValorDeveAtualizarValorDaMulta() {
        Multa multa = new Multa(1, 25.0);
        multa.setValor(50.0);
        assertEquals(50.0, multa.getValor(), 0.001);
    }

    @Test
    void setIdDeveAtualizarIdDaMulta() {
        Multa multa = new Multa(1, 25.0);
        multa.setId(99);
        assertEquals(99, multa.getId());
    }
}
