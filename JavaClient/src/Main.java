import Controller.Controller;
import GUI.IpPortWIndow;
import com.formdev.flatlaf.FlatLightLaf;

import java.io.*;
import java.net.*;
public class Main {
    public static void main(String[] args) throws IOException {
        FlatLightLaf.setup();

        IpPortWIndow window = new IpPortWIndow();
        Controller controller = new Controller(window);
        window.setControleur(controller);

        window.setVisible(true);
    }
}