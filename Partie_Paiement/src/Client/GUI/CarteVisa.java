package Client.GUI;

import Client.Controller.ControllerClient;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class CarteVisa extends JFrame{
    private JPanel MainPanel;
    private JButton ValiderButton;
    private JLabel ImageVisa;
    private JTextField NomField;
    private JTextField NumeroField;
    private JTextField dateField;
    private JTextField CVCField;
    private JButton cancelButton;

    public JButton getValiderButton() {
        return ValiderButton;
    }

    public JLabel getImageVisa() {
        return ImageVisa;
    }

    public JTextField getNomField() {
        return NomField;
    }

    public JTextField getNumeroField() {
        return NumeroField;
    }

    public JTextField getDateField() {
        return dateField;
    }

    public JTextField getCVCField() {
        return CVCField;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public CarteVisa(){
        setSize(400,300);
        setTitle("Client Paiement");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        String filepath = "./images/Visa.png";
        ImageIcon imageIcon = new ImageIcon(filepath);
        ImageVisa.setIcon(imageIcon);
    }

    public void setControleur(ControllerClient c)
    {
        ValiderButton.addActionListener(c);
        cancelButton.addActionListener(c);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        CarteVisa carte = new CarteVisa();
        carte.setVisible(true);
    }
}
