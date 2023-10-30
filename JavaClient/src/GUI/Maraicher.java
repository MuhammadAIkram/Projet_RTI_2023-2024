package GUI;

import Controller.Controller;
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
    private JTextField textFieldPrixTotal;
    private JPanel MainPanel;
    public JTable tableArticles;

    public Maraicher(){
        setSize(800,600);
        setTitle("Le Maraicher en ligne");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        tableArticles = new JTable();
        DefaultTableModel tableModelEmp = (DefaultTableModel) tableArticles.getModel();
        String[] nomsColonnes = { "Article", "Prix a l'unite", "Quantite"};
        tableModelEmp.setColumnIdentifiers(nomsColonnes);
        JscrollPanier.setViewportView(tableArticles);

        getTextFieldNomArticle().setEditable(false);
        getTextFieldPrixUnite().setEditable(false);
        getTextFieldStock().setEditable(false);

        getTextFieldPrixTotal().setEditable(false);
    }

    public void setControleur(Controller c)
    {
        buttonLogin.addActionListener(c);
        buttonLogout.addActionListener(c);
        buttonAvant.addActionListener(c);
        buttonSuivant.addActionListener(c);
        buttonAchat.addActionListener(c);
        buttonSupprimer.addActionListener(c);
        buttonVider.addActionListener(c);
        confirmerAchatButton.addActionListener(c);

        this.addWindowListener(c);
    }

    public JTextField getTextFieldNom() {
        return textFieldNom;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getButtonLogin() {
        return buttonLogin;
    }

    public JButton getButtonLogout() {
        return buttonLogout;
    }

    public JCheckBox getCheckBoxNvClient() {
        return checkBoxNvClient;
    }

    public JLabel getPhotoArticle() {
        return PhotoArticle;
    }

    public JTextField getTextFieldNomArticle() {
        return textFieldNomArticle;
    }

    public JTextField getTextFieldPrixUnite() {
        return textFieldPrixUnite;
    }

    public JTextField getTextFieldStock() {
        return textFieldStock;
    }

    public JSpinner getSpinnerQantite() {
        return spinnerQantite;
    }

    public JButton getButtonAvant() {
        return buttonAvant;
    }

    public JButton getButtonSuivant() {
        return buttonSuivant;
    }

    public JButton getButtonAchat() {
        return buttonAchat;
    }

    public JTextPane getTextPanePublicte() {
        return textPanePublicte;
    }

    public JScrollPane getJscrollPanier() {
        return JscrollPanier;
    }

    public JButton getButtonSupprimer() {
        return buttonSupprimer;
    }

    public JButton getButtonVider() {
        return buttonVider;
    }

    public JButton getConfirmerAchatButton() {
        return confirmerAchatButton;
    }

    public JTextField getTextFieldPrixTotal() {
        return textFieldPrixTotal;
    }

    public JTable getTableArticles() {
        return tableArticles;
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        Maraicher home = new Maraicher();
        home.setVisible(true);
    }
}
