package Client_S.Controller;

import Client.GUI.CarteVisa;
import Client.GUI.HomeWindow;
import Client.GUI.LoginWindow;
import Client.GUI.VisualiserFactureWindow;
import Modele.Facture;
import Modele.Vente;
import VESPAP.ReponseLOGOUT;
import VESPAP.RequeteLOGOUT;
import VESPAPS.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import MyCrypto.MyCrypto;

public class ControllerClientS extends WindowAdapter implements ActionListener {

    private String login;
    private int idClient;
    private boolean logged;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private LoginWindow loginWindow;
    private HomeWindow homeWindow;
    private CarteVisa carteVisa;
    private VisualiserFactureWindow visualiserFactureWindow;
    private LinkedList<Facture> facturesAPayer;
    private LinkedList<Facture> facturesDejaPayer;
    private PrivateKey clePriveeClient;
    private SecretKey cleSession;

    public ControllerClientS(LoginWindow loginWindow){
        try {
            this.loginWindow = loginWindow;

            InputStream input = new FileInputStream("./Fichiers/config.properties");

            Properties properties = new Properties();
            properties.load(input);

            int port = Integer.parseInt(properties.getProperty("PORT_PAIEMENT_SECURE"));

            socket = new Socket("localhost", port);

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            logged = false;
            idClient = 0;

            carteVisa = new CarteVisa();
            visualiserFactureWindow = new VisualiserFactureWindow();

            clePriveeClient = RecupereClePriveeClient();
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
        try {
            login = loginWindow.getTextFieldLogin().getText();
            String password = loginWindow.getTextFieldMDP().getText();

            if(login.length() == 0) throw new Exception("la connexion doit avoir au moins 1 caractère");
            if(password.length() == 0) throw new Exception("le mot de passe doit contenir au moins 1 caractère");

            RequeteLOGIN_S requete = new RequeteLOGIN_S(login,password);
            oos.writeObject(requete);
            ReponseLOGIN_S reponse = (ReponseLOGIN_S) ois.readObject();

            if (reponse.isValide()){
                idClient = reponse.getIdClient();

                byte[] cleSessionDecryptee;
                System.out.println("Clé session cryptée reçue = " + new String(reponse.getCleSession()));
                cleSessionDecryptee = MyCrypto.DecryptAsymRSA(clePriveeClient,reponse.getCleSession());
                cleSession = new SecretKeySpec(cleSessionDecryptee,"DES");
                System.out.println("Decryptage asymétrique de la clé de session...");

                System.out.println("Génération d'une clé de session : " + cleSession);

                JOptionPane.showMessageDialog(null, "Vous êtes connecté avec succès", "Login", JOptionPane.INFORMATION_MESSAGE);

                logged = true;

                loginWindow.setVisible(false);

                homeWindow = new HomeWindow();
                homeWindow.setControleur(this);

                getFactures();

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

    private void onVisualiserDejaPayer() {
        System.out.println("Button VisualiserDejaPayer clicker");

        try {
            if(homeWindow.getJTableFactureDejaPayer().getSelectedRow() == -1)
                throw new Exception("veuillez sélectionner la facture à visualiser (dans la liste des factures deja payer)");

            Facture facture = facturesDejaPayer.get(homeWindow.getJTableFactureDejaPayer().getSelectedRow());

            RequeteCONSULT_S requete = new RequeteCONSULT_S(facture.getIdFacture(), clePriveeClient);
            oos.writeObject(requete);

            ReponseCONSULT_S reponse = (ReponseCONSULT_S) ois.readObject();

            if (reponse.isValide()){
                // Décryptage symétrique du message
                byte[] messageDecrypte;
                System.out.println("Message reçu = " + new String(reponse.getArticles()));
                messageDecrypte = MyCrypto.DecryptSymDES(cleSession,reponse.getArticles());
                System.out.println("Decryptage symétrique du message...");

                // Récupération des données claires
                ByteArrayInputStream bais = new ByteArrayInputStream(messageDecrypte);
                ObjectInputStream objectOut = new ObjectInputStream(bais);
                LinkedList<Vente> list = (LinkedList<Vente>) objectOut.readObject();

                visualiserFactureWindow = new VisualiserFactureWindow(facture.getIdFacture(), facture.getDateFacture(), facture.getMontant(), list);
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

            RequeteCONSULT_S requete = new RequeteCONSULT_S(facture.getIdFacture(), clePriveeClient);
            oos.writeObject(requete);

            ReponseCONSULT_S reponse = (ReponseCONSULT_S) ois.readObject();

            if (reponse.isValide()){
                // Décryptage symétrique du message
                byte[] messageDecrypte;
                System.out.println("Message reçu = " + new String(reponse.getArticles()));
                messageDecrypte = MyCrypto.DecryptSymDES(cleSession,reponse.getArticles());
                System.out.println("Decryptage symétrique du message...");

                // Récupération des données claires
                ByteArrayInputStream bais = new ByteArrayInputStream(messageDecrypte);
                ObjectInputStream objectOut = new ObjectInputStream(bais);
                LinkedList<Vente> list = (LinkedList<Vente>) objectOut.readObject();

                visualiserFactureWindow = new VisualiserFactureWindow(facture.getIdFacture(), facture.getDateFacture(), facture.getMontant(), list);
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

            // Constructon du vecteur de bytes du message clair
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(idFact);
            dos.writeUTF(nom);
            dos.writeUTF(numero);
            byte[] messageClair = baos.toByteArray();
            System.out.println("Construction du message à envoyer");

            // Cryptage symétrique du message
            byte[] messageCrypte;
            messageCrypte = MyCrypto.CryptSymDES(cleSession,messageClair);
            System.out.println("Cryptage symétrique du message : " + new String(messageCrypte));

            RequetePayFacture_S requete = new RequetePayFacture_S(messageCrypte);
            oos.writeObject(requete);

            ReponsePayFacture_S reponse = (ReponsePayFacture_S) ois.readObject();

            if(!reponse.VerifyAuthenticity(cleSession))
                throw new Exception("il y avait un problème d'authenticité !");

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

    //----------------------------------------------------------------------------------
    //---------		home window
    //----------------------------------------------------------------------------------

    private void onLogout() {
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

    //----------------------------------------------------------------------------------
    //---------		autres
    //----------------------------------------------------------------------------------

    public static PrivateKey RecupereClePriveeClient() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        // Désérialisation de la clé privée du serveur
//        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./Fichiers/clePriveeClient.ser"));
//        PrivateKey cle = (PrivateKey) ois.readObject();
//        ois.close();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("./Fichiers/KeystoreClient.jks"),"PassKeystore".toCharArray());

        PrivateKey cle = (PrivateKey) ks.getKey("keymaraicherclient","passclient".toCharArray());

        System.out.println("Cle privee recuperer");

        return cle;
    }

    private void getFactures(){
        System.out.println("recuperation des factures");

        try{
            //recuperation des factures a paye
            RequeteGetFactures_S requete = new RequeteGetFactures_S(false, idClient,clePriveeClient);
            oos.writeObject(requete);

            ReponseGetFactures_S reponse = (ReponseGetFactures_S) ois.readObject();

            if (reponse.isValide()){
                // Décryptage symétrique du message
                byte[] messageDecrypte;
                System.out.println("Message reçu = " + new String(reponse.getFactures()));
                messageDecrypte = MyCrypto.DecryptSymDES(cleSession,reponse.getFactures());
                System.out.println("Decryptage symétrique du message...");

                // Récupération des données claires
                ByteArrayInputStream bais = new ByteArrayInputStream(messageDecrypte);
                ObjectInputStream objectOut = new ObjectInputStream(bais);
                LinkedList<Facture> list = (LinkedList<Facture>) objectOut.readObject();

                facturesAPayer = list;

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
            requete = new RequeteGetFactures_S(true, idClient,clePriveeClient);
            oos.writeObject(requete);

            reponse = (ReponseGetFactures_S) ois.readObject();

            if (reponse.isValide()){
                // Décryptage symétrique du message
                byte[] messageDecrypte;
                System.out.println("Message reçu = " + new String(reponse.getFactures()));
                messageDecrypte = MyCrypto.DecryptSymDES(cleSession,reponse.getFactures());
                System.out.println("Decryptage symétrique du message...");

                // Récupération des données claires
                ByteArrayInputStream bais = new ByteArrayInputStream(messageDecrypte);
                ObjectInputStream objectOut = new ObjectInputStream(bais);
                LinkedList<Facture> list = (LinkedList<Facture>) objectOut.readObject();

                facturesDejaPayer = list;

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

    private void onFermer() {
        visualiserFactureWindow.setVisible(false);
        homeWindow.setVisible(true);
    }
}
