package Client_S;

import Client.GUI.LoginWindow;
import Client_S.Controller.ControllerClientS;
import com.formdev.flatlaf.FlatLightLaf;

public class MainClientS {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        LoginWindow window = new LoginWindow();
        ControllerClientS controller = new ControllerClientS(window);
        window.setControleur(controller);

        window.setVisible(true);
    }
}
