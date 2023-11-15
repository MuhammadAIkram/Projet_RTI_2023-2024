package Client;

import Client.Controller.ControllerClient;
import Client.GUI.LoginWindow;
import com.formdev.flatlaf.FlatLightLaf;

public class MainClient {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        LoginWindow window = new LoginWindow();
        ControllerClient controller = new ControllerClient(window);
        window.setControleur(controller);

        window.setVisible(true);
    }
}
