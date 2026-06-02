package main.model;

public class Administrador extends Usuario {

    public Administrador(int id, String nome) {
        super(id, nome, TipoUsuario.ADMINISTRADOR);
    }
}
