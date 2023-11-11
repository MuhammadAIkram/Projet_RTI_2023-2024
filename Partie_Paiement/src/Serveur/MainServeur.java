package Serveur;

import Serveur.Controller.ControllerServeur;
import Serveur.GUI.ServeurWindow;
import com.formdev.flatlaf.FlatLightLaf;

import java.io.IOException;

public class MainServeur {
    public static void main(String[] args) throws IOException {
        FlatLightLaf.setup();

        ServeurWindow window = new ServeurWindow();
        ControllerServeur controller = new ControllerServeur(window);
        window.setControleur(controller);

        window.setVisible(true);
    }
}
