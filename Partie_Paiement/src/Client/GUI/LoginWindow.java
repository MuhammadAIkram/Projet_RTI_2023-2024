package Client.GUI;

import Client.Controller.ControllerClient;
import Client_S.Controller.ControllerClientS;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame{
    private JPanel MainPanel;
    private JButton ConnecterButton;
    private JTextField textFieldLogin;
    private JTextField textFieldMDP;

    public JTextField getTextFieldLogin() {
        return textFieldLogin;
    }

    public JTextField getTextFieldMDP() {
        return textFieldMDP;
    }

    public JButton getConnecterButton() {
        return ConnecterButton;
    }

    public LoginWindow(){
        setSize(400,150);
        setTitle("Client Paiement");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocationRelativeTo(null);
    }

    public void setControleur(ControllerClient c)
    {
        ConnecterButton.addActionListener(c);

        this.addWindowListener(c);
    }

    public void setControleur(ControllerClientS c)
    {
        ConnecterButton.addActionListener(c);

        this.addWindowListener(c);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        LoginWindow window = new LoginWindow();
        window.setVisible(true);
    }
}
