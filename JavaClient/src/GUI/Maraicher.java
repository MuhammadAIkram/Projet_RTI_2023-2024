package GUI;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Maraicher extends JFrame{
    private JPanel panel1;
    private JPanel PanelLogin;
    private JPanel PanelVisualize;
    private JPanel Publicite;
    private JPanel PanierArticle;
    private JTextField textFieldNom;
    private JPasswordField passwordField;
    private JButton buttonLogin;
    private JButton buttonLogout;
    private JCheckBox checkBoxNvClient;
    private JLabel PhotoArticle;
    private JTextField textFieldNomArticle;
    private JTextField textFieldPrixUnite;
    private JTextField textFieldStock;
    private JSpinner spinnerQantite;
    private JButton buttonAvant;
    private JButton buttonSuivant;
    private JButton buttonAchat;
    private JTextPane textPanePublicte;
    private JScrollPane JscrollPanier;
    private JButton buttonSupprimer;
    private JButton buttonVider;
    private JButton confirmerAchatButton;
    private JTextField textField1;
    private JPanel MainPanel;
    public JTable tableArticles;

    public Maraicher(){
        setSize(800,600);
        setTitle("Le Maraicher en ligne");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        tableArticles = new JTable();
        DefaultTableModel tableModelEmp = (DefaultTableModel) tableArticles.getModel();
        String[] nomsColonnes = { "Article", "Prix a l'unite", "Quantite"};
        tableModelEmp.setColumnIdentifiers(nomsColonnes);
        JscrollPanier.setViewportView(tableArticles);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        Maraicher home = new Maraicher();
        home.setVisible(true);
    }
}
