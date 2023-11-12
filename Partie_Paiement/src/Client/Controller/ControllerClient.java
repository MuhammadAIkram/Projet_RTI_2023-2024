package Client.Controller;

import Client.GUI.HomeWindow;
import Client.GUI.LoginWindow;
import VESPAP.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class ControllerClient extends WindowAdapter implements ActionListener {
    private String login;
    private int idClient;
    private boolean logged;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private LoginWindow loginWindow;
    private HomeWindow homeWindow;

    public ControllerClient(LoginWindow loginWindow){
        try {
            this.loginWindow = loginWindow;

            Socket socket = new Socket("localhost", 50050);

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            logged = false;
            idClient = 0;
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
    }

    @Override
    public void windowClosing(WindowEvent e) {
        onQuitter();
    }

    private void onQuitter() {
        int ret = JOptionPane.showConfirmDialog(null,"Êtes-vous certain de vouloir quitter ?");
        if (ret == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

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
}
