package Client.GUI;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HomeWindow extends JFrame{
    private JPanel MainPanel;
    private JPanel PayerPanel;
    private JPanel DejaPayerPanel;
    private JButton LogoutButton;
    private JScrollPane JScrollAPayer;
    private JScrollPane JScrollADejaPayer;
    private JButton PayerButton;
    private JButton VisualiserAPayerButton;
    private JButton VisualiserDejaPayerButton;
    private JTextField LoginNomField;
    private JTable JTableFactureAPayer;
    private JTable JTableFactureDejaPayer;

    public HomeWindow(){
        setSize(800,600);
        setTitle("Client Paiement");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JTableFactureAPayer = new JTable();
        DefaultTableModel tableModel = (DefaultTableModel) JTableFactureAPayer.getModel();
        String[] nomsColonnes = { "Date", "Montant"};
        tableModel.setColumnIdentifiers(nomsColonnes);
        JScrollAPayer.setViewportView(JTableFactureAPayer);

        JTableFactureDejaPayer = new JTable();
        DefaultTableModel tableModel2 = (DefaultTableModel) JTableFactureDejaPayer.getModel();
        tableModel2.setColumnIdentifiers(nomsColonnes);
        JScrollADejaPayer.setViewportView(JTableFactureDejaPayer);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        HomeWindow home = new HomeWindow();
        home.setVisible(true);
    }
}