package main;

import main.controller.BibliotecaController;
import main.view.SistemaView;

public class Main {
    public static void main(String[] args) {
        BibliotecaController controller = new BibliotecaController();
        SistemaView view = new SistemaView(controller);
        view.iniciar();
    }
}
