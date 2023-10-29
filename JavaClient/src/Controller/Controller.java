package Controller;

import GUI.IpPortWIndow;
import GUI.Maraicher;
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
    }

    @Override
    public void windowClosing(WindowEvent e) {
        onQuitter();
    }

    private void onQuitter() {
        int ret = JOptionPane.showConfirmDialog(null,"Êtes-vous certain de vouloir quitter ?");
        if (ret == JOptionPane.YES_OPTION)
        {
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

            PasLogger();
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

            Logger();
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    //----------------------------------------------------------------------------------
    //---------		Methodes utile
    //----------------------------------------------------------------------------------

    private String SendRec(String Requete) throws Exception {
        int taille = tcp.Send(csocket, Requete);

        if(taille == -1) throw new Exception("Erreur send!");

        String Reponse = tcp.Receive(csocket);

        if(Reponse == null) throw new Exception("Erreur Receive!");

        return Reponse;
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
