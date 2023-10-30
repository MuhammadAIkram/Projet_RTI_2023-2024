package Controller;

import GUI.IpPortWIndow;
import GUI.Maraicher;
import Modele.Article;
import Modele.TCP;

import javax.security.auth.callback.TextInputCallback;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Controller extends WindowAdapter implements ActionListener {
    private IpPortWIndow WindowPortIp;
    private Maraicher maraicherWindow;
    private Socket csocket;
    private boolean logged;
    private int numFacture;

    private Article articleCourant;

    private TCP tcp;

    public Controller(IpPortWIndow WindowPortIp)
    {
        this.WindowPortIp = WindowPortIp;

        tcp = new TCP();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == WindowPortIp.getButtonConnecter()) {
            onConnecter();
        }
        if(e.getSource() == maraicherWindow.getButtonLogout()) {
            onLogout();
        }
        if(e.getSource() == maraicherWindow.getButtonLogin()) {
            onLogin();
        }
        if(e.getSource() == maraicherWindow.getButtonAvant()) {
            onAvant();
        }
        if(e.getSource() == maraicherWindow.getButtonSuivant()) {
            onSuivante();
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
            if(logged) onLogout();

            if(csocket != null)
            {
                try
                {
                    csocket.close();
                }
                catch (IOException e){
                    System.out.println("Attention il y'a une probleme avec la fermeture");
                }
            }
            System.exit(0);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		Pour Connexion
    //----------------------------------------------------------------------------------

    private void onConnecter() {
        //System.out.println(WindowPortIp.getTextFieldPort().getText() + " " + WindowPortIp.getTextFieldIpAdresse().getText());

        try{
            String regex = "^[0-9.]+$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(WindowPortIp.getTextFieldIpAdresse().getText());

            if (!matcher.matches()) throw new Exception("l'adresse IP ne peut contenir que des chiffres et des points");

            regex = "^[0-9]+$";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(WindowPortIp.getTextFieldPort().getText());

            if (!matcher.matches()) throw new Exception("le port ne peut être qu'un numéro");

            String ipS = WindowPortIp.getTextFieldIpAdresse().getText();
            int PortS = Integer.parseInt(WindowPortIp.getTextFieldPort().getText());

            csocket = tcp.ClientSocket(ipS,PortS);

            JOptionPane.showMessageDialog(null, "Vous êtes connecté au serveur !", "Connexion", JOptionPane.INFORMATION_MESSAGE);

            maraicherWindow = new Maraicher();
            maraicherWindow.setControleur(this);
            maraicherWindow.setVisible(true);

            WindowPortIp.setVisible(false);

            PasLogger();
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		Pour Maraicher
    //----------------------------------------------------------------------------------

    private void onLogout() {
        String Requete = "LOGOUT";

        System.out.println(Requete);

        try {
            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("LOGOUT"))
            {
                if(tokens[1].equals("ok"))
                {
                    //doit ajouter verification du panier ici

                    JOptionPane.showMessageDialog(null, "BYE BYE ;)", "Logout", JOptionPane.INFORMATION_MESSAGE);

                    PasLogger();

                    logged = false;
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLogin(){
        int nvClient;

        if(maraicherWindow.getCheckBoxNvClient().isSelected()) nvClient = 1;
        else nvClient = 0;

        String Requete = "LOGIN#" + maraicherWindow.getTextFieldNom().getText() + "#" + maraicherWindow.getPasswordField().getText() + "#" + nvClient;

        System.out.println(Requete);

        try {
            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("LOGIN"))
            {
                if(tokens[1].equals("ok"))
                {
                    if(maraicherWindow.getCheckBoxNvClient().isSelected())
                        JOptionPane.showMessageDialog(null, "Vous avez été inscrit avec succès", "Login", JOptionPane.INFORMATION_MESSAGE);

                    JOptionPane.showMessageDialog(null, "Vous êtes connecté avec succès", "Login", JOptionPane.INFORMATION_MESSAGE);

                    Logger();

                    logged = true;

                    numFacture = Integer.parseInt(tokens[2]);

                    System.out.println("Numero de facture: " + numFacture);

                    ConsultArticle(1);
                }
                else
                    JOptionPane.showMessageDialog(null, tokens[2], "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAvant() {
        ConsultArticle(articleCourant.getId() - 1);
    }

    private void onSuivante(){
        ConsultArticle(articleCourant.getId() + 1);
    }

    private void ConsultArticle(int id)
    {
        String Requete = "CONSULT#" + id;

        System.out.println(Requete);

        try {
            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("CONSULT"))
            {
                if(!tokens[1].equals("-1"))
                {
                    String intitule = tokens[2];
                    int stock = Integer.parseInt(tokens[3]);
                    float prix = Float.parseFloat(tokens[4]);
                    String image = tokens[5];

                    setArticle(intitule, prix, stock , image);

                    articleCourant = new Article(Integer.parseInt(tokens[1]), intitule, prix, stock, image);
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		Methodes outil
    //----------------------------------------------------------------------------------

    private String SendRec(String Requete) throws Exception {
        int taille = tcp.Send(csocket, Requete);

        if(taille == -1) throw new Exception("Erreur send!");

        String Reponse = tcp.Receive(csocket);

        if(Reponse == null) throw new Exception("Erreur Receive!");

        return Reponse;
    }

    private void setArticle(String intitule, float prix, int stock, String image)
    {
        maraicherWindow.getTextFieldNomArticle().setText(intitule);
        maraicherWindow.getTextFieldStock().setText(String.valueOf(stock));
        maraicherWindow.getTextFieldPrixUnite().setText(String.valueOf(prix));

        String filepath = "./images/" + image;
        ImageIcon imageIcon = new ImageIcon(filepath);
        maraicherWindow.getPhotoArticle().setIcon(imageIcon);
    }

    //----------------------------------------------------------------------------------
    //---------		Les differents etats de logger
    //----------------------------------------------------------------------------------

    private void PasLogger()
    {
        maraicherWindow.getButtonLogout().setEnabled(false);
        maraicherWindow.getButtonAvant().setEnabled(false);
        maraicherWindow.getButtonSuivant().setEnabled(false);
        maraicherWindow.getButtonAchat().setEnabled(false);
        maraicherWindow.getButtonSupprimer().setEnabled(false);
        maraicherWindow.getButtonVider().setEnabled(false);
        maraicherWindow.getConfirmerAchatButton().setEnabled(false);

        maraicherWindow.getSpinnerQantite().setEnabled(false);

        maraicherWindow.getButtonLogin().setEnabled(true);
        maraicherWindow.getTextFieldNom().setEnabled(true);
        maraicherWindow.getPasswordField().setEnabled(true);
        maraicherWindow.getCheckBoxNvClient().setEnabled(true);

        maraicherWindow.getTextFieldNomArticle().setText(null);
        maraicherWindow.getTextFieldStock().setText(null);
        maraicherWindow.getTextFieldPrixUnite().setText(null);
        maraicherWindow.getPhotoArticle().setIcon(null);
    }

    private void Logger()
    {
        maraicherWindow.getButtonLogin().setEnabled(false);
        maraicherWindow.getTextFieldNom().setEnabled(false);
        maraicherWindow.getPasswordField().setEnabled(false);
        maraicherWindow.getCheckBoxNvClient().setEnabled(false);

        maraicherWindow.getButtonLogout().setEnabled(true);
        maraicherWindow.getButtonAvant().setEnabled(true);
        maraicherWindow.getButtonSuivant().setEnabled(true);
        maraicherWindow.getButtonAchat().setEnabled(true);
        maraicherWindow.getButtonSupprimer().setEnabled(true);
        maraicherWindow.getButtonVider().setEnabled(true);
        maraicherWindow.getConfirmerAchatButton().setEnabled(true);

        maraicherWindow.getSpinnerQantite().setEnabled(true);
    }
}
