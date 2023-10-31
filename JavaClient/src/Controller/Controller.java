package Controller;

import GUI.IpPortWIndow;
import GUI.Maraicher;
import Modele.Article;
import Modele.TCP;

import javax.security.auth.callback.TextInputCallback;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Controller extends WindowAdapter implements ActionListener {
    private IpPortWIndow WindowPortIp;
    private Maraicher maraicherWindow;
    private Socket csocket;
    private boolean logged;
    private int numFacture;
    private int nbArticles;
    private float totalCaddie;

    private Article articleCourant;
    private LinkedList<Article> Caddie;

    private TCP tcp;

    public Controller(IpPortWIndow WindowPortIp)
    {
        this.WindowPortIp = WindowPortIp;

        tcp = new TCP();

        numFacture = 0;
        nbArticles = 0;
        totalCaddie = 0.0F;

        Caddie = new LinkedList<>();
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
        if(e.getSource() == maraicherWindow.getButtonAchat()) {
            onAchat();
        }
        if(e.getSource() == maraicherWindow.getButtonSupprimer()) {
            onSupprimer();
        }
        if(e.getSource() == maraicherWindow.getButtonVider()) {
            onVider();
        }
        if(e.getSource() == maraicherWindow.getConfirmerAchatButton()) {
            onConfirme();
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
                    if(nbArticles > 0)
                        VidePanier();

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

                    getCaddie();

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

    private void onAchat() {
        try {
            int stockSpin = (int) maraicherWindow.getSpinnerQantite().getValue();

            if(stockSpin <= 0)
                throw new Exception("Veuillez sélectionner une valeur supérieure à 0");

            int i = 0;

            for (Article art: Caddie) {
                if(art.getId() == articleCourant.getId())
                {
                    break;
                }

                i++;
            }

            if(Caddie.size() == i) i = 10;

            System.out.println(i);

            if(nbArticles == 10 && i == 10)
                throw new Exception("votre panier est plein, merci d'acheter les articles ou de supprimer un article du panier !");

            String Requete = "ACHAT#" + articleCourant.getId() + "#" + stockSpin;

            System.out.println(Requete);

            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("ACHAT"))
            {
                if(!tokens[1].equals("-1"))
                {
                    if(tokens[2].equals("0")) throw new Exception("stock insuffisant!");
                    else
                    {
                        articleCourant.setStock(articleCourant.getStock() - stockSpin);

                        maraicherWindow.getTextFieldStock().setText(String.valueOf(articleCourant.getStock()));

                        if(i == 10)
                        {
                            Caddie.add(new Article(articleCourant.getId(), articleCourant.getIntitule(), articleCourant.getPrix(), stockSpin, articleCourant.getImage()));

                            totalCaddie += (stockSpin*articleCourant.getPrix());

                            maraicherWindow.getTextFieldPrixTotal().setText(String.valueOf(totalCaddie));

                            ajouteArticleTablePanier(articleCourant.getIntitule(), articleCourant.getPrix(), stockSpin);

                            Requete = "UPDATE_CAD#" + numFacture + "#0#0#" + totalCaddie + "#" + articleCourant.getId() + "#" + stockSpin;

                            System.out.println(Requete);

                            Reponse = SendRec(Requete);

                            System.out.println(Reponse);

                            nbArticles++;
                        }
                        else
                        {
                            Caddie.get(i).setStock(Caddie.get(i).getStock() + stockSpin);

                            totalCaddie = 0.0F;

                            for (Article art: Caddie) {
                                totalCaddie += (art.getPrix()*art.getStock());
                            }

                            maraicherWindow.getTextFieldPrixTotal().setText(String.valueOf(totalCaddie));

                            DefaultTableModel modelArticles = (DefaultTableModel) maraicherWindow.getTableArticles().getModel();

                            modelArticles.setValueAt(Caddie.get(i).getStock(), i, 2);

                            Requete = "UPDATE_CAD#" + numFacture + "#0#0#" + totalCaddie + "#" + articleCourant.getId() + "#" + stockSpin;

                            System.out.println(Requete);

                            Reponse = SendRec(Requete);

                            System.out.println(Reponse);
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Achat", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSupprimer() {
        try {
            int ind = maraicherWindow.getTableArticles().getSelectedRow();

            if(ind == -1) throw new Exception("veuillez choisir une article pour supprimer");

            String Requete = "CANCEL#" + Caddie.get(ind).getId() + "#" + Caddie.get(ind).getStock();

            System.out.println(Requete);

            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("CANCEL"))
            {
                int newID = Integer.parseInt(tokens[1]);

                if(newID != -1)
                {
                    if(articleCourant.getId() == newID)
                    {
                        articleCourant.setStock(Integer.parseInt(tokens[2]));

                        maraicherWindow.getTextFieldStock().setText(String.valueOf(articleCourant.getStock()));
                    }

                    Caddie.remove(ind);
                    nbArticles--;

                    DefaultTableModel modelArticles = (DefaultTableModel) maraicherWindow.getTableArticles().getModel();

                    modelArticles.removeRow(ind);

                    totalCaddie = 0.0F;

                    for (Article art: Caddie) {
                        totalCaddie += (art.getPrix()*art.getStock());
                    }

                    maraicherWindow.getTextFieldPrixTotal().setText(String.valueOf(totalCaddie));

                    Requete = "UPDATE_CAD#" + numFacture + "#1#" + totalCaddie + "#" + articleCourant.getId();

                    System.out.println(Requete);

                    Reponse = SendRec(Requete);

                    System.out.println(Reponse);
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Supprimer", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onVider() {
        VidePanier();

        String Requete = "DELETE_CAD#" + numFacture;

        System.out.println(Requete);
        try {
            String Reponse = SendRec(Requete);

            System.out.println(Reponse);
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Vider", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onConfirme() {
        try {
            if(nbArticles <= 0)
                throw new Exception("Vous n'avez rien dans votre panier !");

            String Requete = "CONFIRMER#" + numFacture + "#" + maraicherWindow.getTextFieldNom().getText();

            System.out.println(Requete);

            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("CONFIRMER"))
            {
                if(tokens[1].equals("-1"))
                    throw new Exception("Un problème est survenu lors de l'obtention de votre numéro de reçu, mais votre commande a été confirmée !");
                else
                {
                    totalCaddie = 0.0F;

                    maraicherWindow.getTextFieldPrixTotal().setText(null);

                    DefaultTableModel modelArticles = (DefaultTableModel) maraicherWindow.getTableArticles().getModel();

                    int rowCount = modelArticles.getRowCount();

                    for (int i = rowCount - 1; i >= 0; i--) {
                        modelArticles.removeRow(i);
                    }

                    Caddie = new LinkedList<>();

                    nbArticles = 0;

                    numFacture = Integer.parseInt(tokens[1]);

                    JOptionPane.showMessageDialog(null, "Achat confirmer!", "Payer", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Payer", JOptionPane.ERROR_MESSAGE);
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

    private void VidePanier()
    {
        try {
            String Requete = "CANCEL_ALL#" + nbArticles;

            for (Article art:Caddie) {
                Requete += "#" + art.getId() + "&" + art.getStock();
            }

            System.out.println(Requete);

            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            String[] tokens;

            tokens = Reponse.split("#");

            if(tokens[0].equals("CANCEL_ALL"))
            {
                if(tokens[1].equals("ko")) throw new Exception("Vous n'avez rien dans votre panier !");

                totalCaddie = 0.0F;

                maraicherWindow.getTextFieldPrixTotal().setText(null);

                DefaultTableModel modelArticles = (DefaultTableModel) maraicherWindow.getTableArticles().getModel();

                int rowCount = modelArticles.getRowCount();

                for (int i = rowCount - 1; i >= 0; i--) {
                    modelArticles.removeRow(i);
                }

                for (Article art: Caddie) {
                    if(art.getId() == articleCourant.getId())
                    {
                        articleCourant.setStock(articleCourant.getStock()+art.getStock());
                        maraicherWindow.getTextFieldStock().setText(String.valueOf(articleCourant.getStock()));
                        break;
                    }
                }

                Caddie = new LinkedList<>();

                nbArticles = 0;
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Vider", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getCaddie()
    {
        try {
            String Requete = "CADDIE#" + numFacture;

            System.out.println(Requete);

            String Reponse = SendRec(Requete);

            System.out.println(Reponse);

            StringTokenizer tokenizer = new StringTokenizer(Reponse, "#");
            String ptr = tokenizer.nextToken();

            if (ptr.equals("CADDIE")) {
                nbArticles = Integer.parseInt(tokenizer.nextToken());

                if (nbArticles > 0) {
                    int i = 0;

                    while (i < nbArticles) {
                        StringTokenizer itemTokenizer = new StringTokenizer(tokenizer.nextToken(), "$");

                        int id = Integer.parseInt(itemTokenizer.nextToken());
                        String intitule = itemTokenizer.nextToken();
                        int stock = Integer.parseInt(itemTokenizer.nextToken());
                        float prix = Float.parseFloat(itemTokenizer.nextToken());

                        Article art = new Article(id, intitule, prix, stock, "");

                        Caddie.add(art);

                        ajouteArticleTablePanier(intitule, prix, stock);

                        totalCaddie += (stock * prix);

                        i++;
                    }

                    maraicherWindow.getTextFieldPrixTotal().setText(String.valueOf(totalCaddie));
                }
            }
        }
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Vider", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouteArticleTablePanier(String intitule, float prix, int stock)
    {
        DefaultTableModel modelArticles = (DefaultTableModel) maraicherWindow.getTableArticles().getModel();

        Vector ligne = new Vector();
        ligne.add(intitule);
        ligne.add(prix);
        ligne.add(stock);

        modelArticles.addRow(ligne);
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
