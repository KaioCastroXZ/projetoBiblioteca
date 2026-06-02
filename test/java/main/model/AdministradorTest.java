package main.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorTest {

    @Test
    void deveCriarAdministradorComTipoCorreto() {
        Administrador administrador = new Administrador(1, "Ana");

        assertEquals(1, administrador.getId());
        assertEquals("Ana", administrador.getNome());
        assertEquals(TipoUsuario.ADMINISTRADOR, administrador.getTipo());
    }

    @Test
    void setNomeDeveAtualizarNomeDoAdministrador() {
        Administrador administrador = new Administrador(1, "Ana");

        administrador.setNome("Renata");

        assertEquals("Renata", administrador.getNome());
    }
}
