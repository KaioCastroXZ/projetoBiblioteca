package main;

import main.controller.BibliotecaController;
import main.database.DatabaseInitializer;
import main.view.SistemaView;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.inicializar();
        BibliotecaController controller = new BibliotecaController();
        SistemaView view = new SistemaView(controller);
        view.iniciar();
    }
}
