package Serveur.Controller;

import Serveur.GUI.ServeurWindow;
import ServeurGeneriqueTCP.*;
import VESPAP.VESPAP;
import VESPAPS.VESPAPS;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;
import java.util.Vector;

public class ControllerServeur extends WindowAdapter implements Logger, ActionListener {
    ThreadServeur threadServeur;
    ServeurWindow serveurWindow;
    Protocole protocole;

    boolean ServeurStart;

    public ControllerServeur(ServeurWindow serveurWindow) {
        this.serveurWindow = serveurWindow;

        this.serveurWindow.getDemarrerButton().setEnabled(true);
        this.serveurWindow.getArreterButton().setEnabled(false);

        threadServeur = null;
        ServeurStart = false;
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
            if(ServeurStart)
            {
                protocole.CloseDatabase();
                threadServeur.interrupt();
            }

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

            int port;

            if(this.serveurWindow.getCheckBoxServeurType().isSelected()){
                protocole = new VESPAPS(this);
                port = Integer.parseInt(properties.getProperty("PORT_PAIEMENT_SECURE"));
                threadServeur = new ThreadServeurDemande(port,protocole,this);
            }
            else {
                protocole = new VESPAP(this);
                port = Integer.parseInt(properties.getProperty("PORT_PAIEMENT"));
                int nbThread = Integer.parseInt(properties.getProperty("NB_Thread_Pool"));

                threadServeur = new ThreadServeurPool(port,protocole,nbThread,this);
            }

            videLogs();
            threadServeur.start();

            this.serveurWindow.getDemarrerButton().setEnabled(false);
            this.serveurWindow.getArreterButton().setEnabled(true);

            ServeurStart = true;
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

            this.serveurWindow.getDemarrerButton().setEnabled(true);
            this.serveurWindow.getArreterButton().setEnabled(false);

            protocole.CloseDatabase();

            ServeurStart = false;
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
