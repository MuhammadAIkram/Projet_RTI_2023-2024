package Client.Controller;

import Client.GUI.CarteVisa;
import Client.GUI.HomeWindow;
import Client.GUI.LoginWindow;
import Client.GUI.VisualiserFactureWindow;
import Modele.Facture;
import VESPAP.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

public class ControllerClient extends WindowAdapter implements ActionListener {
    private String login;
    private int idClient;
    private boolean logged;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private LoginWindow loginWindow;
    private HomeWindow homeWindow;
    private CarteVisa carteVisa;
    private VisualiserFactureWindow visualiserFactureWindow;
    private LinkedList<Facture> facturesAPayer;
    private LinkedList<Facture> facturesDejaPayer;

    public ControllerClient(LoginWindow loginWindow){
        try {
            this.loginWindow = loginWindow;

            InputStream input = new FileInputStream("./Fichiers/config.properties");

            Properties properties = new Properties();
            properties.load(input);

            int port = Integer.parseInt(properties.getProperty("PORT_PAIEMENT"));

            socket = new Socket("localhost", port);

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            logged = false;
            idClient = 0;

            carteVisa = new CarteVisa();
            visualiserFactureWindow = new VisualiserFactureWindow();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == loginWindow.getConnecterButton()) {
            onConnecter();
        }
        if(e.getSource() == homeWindow.getLogoutButton()){
            onLogout();
        }
        if(e.getSource() == homeWindow.getPayerButton()){
            onPayer();
        }
        if(e.getSource() == homeWindow.getVisualiserAPayerButton()){
            onVisualiserAPayer();
        }
        if(e.getSource() == homeWindow.getVisualiserDejaPayerButton()){
            onVisualiserDejaPayer();
        }
        if(e.getSource() == carteVisa.getValiderButton()){
            onValider();
        }
        if(e.getSource() == carteVisa.getCancelButton()){
            onCancel();
        }
        if(e.getSource() == visualiserFactureWindow.getFermerButton()){
            onFermer();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        onQuitter();
    }

    private void onQuitter() {
        int ret = JOptionPane.showConfirmDialog(null,"Êtes-vous certain de vouloir quitter ?");
        if (ret == JOptionPane.YES_OPTION)
        {
            if(logged)
            {
                onLogout();
            }

            if(!socket.isClosed() && socket != null) {
                try {
                    System.out.println("Fermeture succes");
                    socket.close();
                    oos.close();
                    ois.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            System.exit(0);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		login window
    //----------------------------------------------------------------------------------

    private void onConnecter() {
        System.out.println("button connecter cliquer");

        try {
            login = loginWindow.getTextFieldLogin().getText();
            String password = loginWindow.getTextFieldMDP().getText();

            if(login.length() == 0) throw new Exception("la connexion doit avoir au moins 1 caractère");
            if(password.length() == 0) throw new Exception("le mot de passe doit contenir au moins 1 caractère");

            RequeteLOGIN requete = new RequeteLOGIN(login,password);
            oos.writeObject(requete);
            ReponseLOGIN reponse = (ReponseLOGIN) ois.readObject();

            if (reponse.isValide()){
                idClient = reponse.getIdClient();

                JOptionPane.showMessageDialog(null, "Vous êtes connecté avec succès", "Login", JOptionPane.INFORMATION_MESSAGE);

                logged = true;

                loginWindow.setVisible(false);

                homeWindow = new HomeWindow();
                homeWindow.setControleur(this);

                getFactures(); //pour recuperer les factures

                homeWindow.setVisible(true);

                homeWindow.getLoginNomField().setText(login);
            }
            else {
                JOptionPane.showMessageDialog(null,"Erreur de login !","Erreur...",JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }

    }

    //----------------------------------------------------------------------------------
    //---------		home window
    //----------------------------------------------------------------------------------

    private void onLogout() {
        System.out.println("button logout cliquer");

        try {
            RequeteLOGOUT requete = new RequeteLOGOUT(login);
            oos.writeObject(requete);

            ReponseLOGOUT reponse = (ReponseLOGOUT) ois.readObject();

            if (reponse.isValide()){
                logged = false;

                homeWindow.setVisible(false);
                loginWindow.setVisible(true);
            }
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getFactures(){
        System.out.println("recuperation des factures");

        try{
            //recuperation des factures a paye
            RequeteGetFactures requete = new RequeteGetFactures(false, idClient);
            oos.writeObject(requete);

            ReponseGetFactures reponse = (ReponseGetFactures) ois.readObject();

            if (reponse.isValide()){
                facturesAPayer = reponse.getFactures();

                DefaultTableModel modelFactures = (DefaultTableModel) homeWindow.getJTableFactureAPayer().getModel();

                for (Facture facture:facturesAPayer) {

                    Vector ligne = new Vector();
                    ligne.add(facture.getIdFacture());
                    ligne.add(facture.getDateFacture());
                    ligne.add(facture.getMontant());

                    modelFactures.addRow(ligne);
                }
            }

            //recuperation des factures deja paye
            requete = new RequeteGetFactures(true, idClient);
            oos.writeObject(requete);

            reponse = (ReponseGetFactures) ois.readObject();

            if (reponse.isValide()){
                facturesDejaPayer = reponse.getFactures();

                DefaultTableModel modelFactures = (DefaultTableModel) homeWindow.getJTableFactureDejaPayer().getModel();

                for (Facture facture:facturesDejaPayer) {

                    Vector ligne = new Vector();
                    ligne.add(facture.getIdFacture());
                    ligne.add(facture.getDateFacture());
                    ligne.add(facture.getMontant());

                    modelFactures.addRow(ligne);
                }
            }
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPayer() {
        System.out.println("Button payer clicker");

        try {
            if(homeWindow.getJTableFactureAPayer().getSelectedRow() == -1)
                throw new Exception("veuillez sélectionner la facture à payer");

            carteVisa = new CarteVisa();
            carteVisa.setControleur(this);
            carteVisa.setVisible(true);

            homeWindow.setVisible(false);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onVisualiserDejaPayer() {
        System.out.println("Button VisualiserDejaPayer clicker");

        try {
            if(homeWindow.getJTableFactureDejaPayer().getSelectedRow() == -1)
                throw new Exception("veuillez sélectionner la facture à visualiser (dans la liste des factures deja payer)");

            Facture facture = facturesDejaPayer.get(homeWindow.getJTableFactureDejaPayer().getSelectedRow());

            RequeteCONSULT requete = new RequeteCONSULT(facture.getIdFacture());
            oos.writeObject(requete);

            ReponseCONSULT reponse = (ReponseCONSULT) ois.readObject();

            if (reponse.isValide()){
                visualiserFactureWindow = new VisualiserFactureWindow(facture.getIdFacture(), facture.getDateFacture(), facture.getMontant(), reponse.getArticles());
                visualiserFactureWindow.setControleur(this);
                visualiserFactureWindow.setVisible(true);

                homeWindow.setVisible(false);
            }
            else throw new Exception("Erreur avec consult");
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onVisualiserAPayer() {
        System.out.println("Button onVisualiserAPayer clicker");

        try {
            if(homeWindow.getJTableFactureAPayer().getSelectedRow() == -1)
                throw new Exception("veuillez sélectionner la facture à visualiser (dans la liste des factures a payer)");

            Facture facture = facturesAPayer.get(homeWindow.getJTableFactureAPayer().getSelectedRow());

            RequeteCONSULT requete = new RequeteCONSULT(facture.getIdFacture());
            oos.writeObject(requete);

            ReponseCONSULT reponse = (ReponseCONSULT) ois.readObject();

            if (reponse.isValide()){
                visualiserFactureWindow = new VisualiserFactureWindow(facture.getIdFacture(), facture.getDateFacture(), facture.getMontant(), reponse.getArticles());
                visualiserFactureWindow.setControleur(this);
                visualiserFactureWindow.setVisible(true);

                homeWindow.setVisible(false);
            }
            else throw new Exception("Erreur avec consult");
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		carte visa
    //----------------------------------------------------------------------------------

    private void onCancel() {
        carteVisa.setVisible(false);
        homeWindow.setVisible(true);
    }

    private void onValider() {
        try {
            if(carteVisa.getNomField().getText().equals("")) throw new Exception("veuillez entrer une valeur pour le nom");
            if(carteVisa.getNumeroField().getText().equals("")) throw new Exception("veuillez entrer une valeur pour le numero");
            if(carteVisa.getDateField().getText().equals("")) throw new Exception("veuillez entrer une valeur pour la date");
            if(carteVisa.getCVCField().getText().equals("")) throw new Exception("veuillez entrer une valeur pour le CVC");

            int idFact = facturesAPayer.get(homeWindow.getJTableFactureAPayer().getSelectedRow()).getIdFacture();
            String nom = carteVisa.getNomField().getText();
            String numero = carteVisa.getNumeroField().getText();

            RequetePayFacture requete = new RequetePayFacture(idFact, nom, numero);
            oos.writeObject(requete);

            ReponsePayFacture reponse = (ReponsePayFacture) ois.readObject();

            if (reponse.isValide()){
                if(!reponse.isEchec()){
                    JOptionPane.showMessageDialog(null, "Votre paiement a été traité avec succès", "Paiement", JOptionPane.INFORMATION_MESSAGE);

                    int index = homeWindow.getJTableFactureAPayer().getSelectedRow();

                    Facture facture = facturesAPayer.get(index);

                    facturesDejaPayer.add(facture);

                    DefaultTableModel modelFactures = (DefaultTableModel) homeWindow.getJTableFactureDejaPayer().getModel();

                    Vector ligne = new Vector();
                    ligne.add(facture.getIdFacture());
                    ligne.add(facture.getDateFacture());
                    ligne.add(facture.getMontant());

                    modelFactures.addRow(ligne);

                    DefaultTableModel modelFactures2 = (DefaultTableModel) homeWindow.getJTableFactureAPayer().getModel();

                    modelFactures2.removeRow(index);

                    facturesAPayer.remove(index);

                    carteVisa.setVisible(false);
                    homeWindow.setVisible(true);
                }
                else throw new Exception("Échec du paiement.");
            }
            else throw new Exception("numéro de carte invalide");
        } catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }

    }

    private void onFermer() {
        visualiserFactureWindow.setVisible(false);
        homeWindow.setVisible(true);
    }
}
