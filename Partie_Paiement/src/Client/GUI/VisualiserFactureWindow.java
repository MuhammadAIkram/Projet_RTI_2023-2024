package Client.GUI;

import Client.Controller.ControllerClient;
import Client_S.Controller.ControllerClientS;
import Modele.Facture;
import Modele.Vente;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedList;
import java.util.Vector;

public class VisualiserFactureWindow extends JFrame{
    private JButton fermerButton;
    private JScrollPane JScrollVente;
    private JTextField idField;
    private JTextField dateField;
    private JTextField montantField;
    private JPanel MainPanel;

    private JTable JTableVentes;

    public JButton getFermerButton() {
        return fermerButton;
    }

    public VisualiserFactureWindow() {
    }

    public VisualiserFactureWindow(int idFacture, String dateFacture, float montant, LinkedList<Vente> articles){
        setSize(800,600);
        setTitle("Client Paiement");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JTableVentes = new JTable();
        DefaultTableModel tableModel = (DefaultTableModel) JTableVentes.getModel();
        String[] nomsColonnes = {"Article", "Prix Unitaire", "Quantite"};
        tableModel.setColumnIdentifiers(nomsColonnes);
        JScrollVente.setViewportView(JTableVentes);

        DefaultTableModel modelArticles = (DefaultTableModel) JTableVentes.getModel();

        for (Vente article:articles) {

            Vector ligne = new Vector();
            ligne.add(article.getNom());
            ligne.add(article.getPrixUnite());
            ligne.add(article.getQuantite());

            modelArticles.addRow(ligne);
        }

        idField.setText(String.valueOf(idFacture));
        dateField.setText(dateFacture);
        montantField.setText(String.valueOf(montant));
    }

    public void setControleur(ControllerClient c)
    {
        fermerButton.addActionListener(c);
    }

    public void setControleur(ControllerClientS c)
    {
        fermerButton.addActionListener(c);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        VisualiserFactureWindow window = new VisualiserFactureWindow(5, "5/5/2023", 50.2F, new LinkedList<>());
        window.setVisible(true);
    }
}
