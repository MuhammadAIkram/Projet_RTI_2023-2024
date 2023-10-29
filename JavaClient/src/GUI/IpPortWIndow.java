package GUI;

import Controller.Controller;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class IpPortWIndow extends JFrame{
    private JPanel MainPanel;
    private JButton buttonConnecter;
    private JTextField textFieldIpAdresse;
    private JTextField textFieldPort;

    public IpPortWIndow(){
        setSize(400,150);
        setTitle("Le Maraicher en ligne");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocationRelativeTo(null);
    }

    public void setControleur(Controller c)
    {
        buttonConnecter.addActionListener(c);

        this.addWindowListener(c);
    }

    public JButton getButtonConnecter() {
        return buttonConnecter;
    }

    public JTextField getTextFieldIpAdresse() {
        return textFieldIpAdresse;
    }

    public JTextField getTextFieldPort() {
        return textFieldPort;
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        IpPortWIndow window = new IpPortWIndow();
        window.setVisible(true);
    }
}
