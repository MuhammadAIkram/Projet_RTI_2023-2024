package Serveur.Controller;

import Serveur.GUI.ServeurWindow;
import ServeurGeneriqueTCP.*;
import VESPAP.VESPAP;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.module.Configuration;
import java.util.Properties;
import java.util.Vector;

public class ControllerServeur extends WindowAdapter implements Logger, ActionListener {
    ThreadServeur threadServeur;
    ServeurWindow serveurWindow;

    public ControllerServeur(ServeurWindow serveurWindow) {
        this.serveurWindow = serveurWindow;

        this.serveurWindow.getDemarrerButton().setEnabled(true);
        this.serveurWindow.getArreterButton().setEnabled(false);

        threadServeur = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == serveurWindow.getDemarrerButton()) {
            onDemarrer();
        }
        if(e.getSource() == serveurWindow.getArreterButton()) {
            onArreter();
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

    private void onDemarrer() {
        System.out.println("Button Demarrer clique");

        try {
            Properties properties = new Properties();

            final String CONFIG_FILE = ".\\Fichiers\\config.properties";

            InputStream input = new FileInputStream(CONFIG_FILE);

            properties.load(input);

            int port = Integer.parseInt(properties.getProperty("PORT_PAIEMENT"));
            int nbThread = Integer.parseInt(properties.getProperty("NB_Thread_Pool"));

            Protocole protocole = new VESPAP(this);

            threadServeur = new ThreadServeurPool(port,protocole,nbThread,this);

            videLogs();
            threadServeur.start();

            this.serveurWindow.getDemarrerButton().setEnabled(false);
            this.serveurWindow.getArreterButton().setEnabled(true);
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(null,"Erreur de Port et/ou taille Pool !","Erreur...",JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }

    }

    private void onArreter() {
        try {
            threadServeur.interrupt();
           // threadServeur.CloseSocket();

            this.serveurWindow.getDemarrerButton().setEnabled(true);
            this.serveurWindow.getArreterButton().setEnabled(false);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,e.getMessage(),"Erreur...",JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void Trace(String message) {
        DefaultTableModel modele = (DefaultTableModel) serveurWindow.getTableLog().getModel();
        Vector<String> ligne = new Vector<>();
        ligne.add(Thread.currentThread().getName());
        ligne.add(message);
        modele.insertRow(modele.getRowCount(),ligne);
    }

    private void videLogs()
    {
        DefaultTableModel modele = (DefaultTableModel) serveurWindow.getTableLog().getModel();
        modele.setRowCount(0);
    }
}
