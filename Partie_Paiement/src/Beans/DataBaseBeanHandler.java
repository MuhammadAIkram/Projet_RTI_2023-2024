package Beans;

import Modele.Facture;
import Modele.Vente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class DataBaseBeanHandler {
    private DataBaseBeanGenerique beanGenerique;

    public DataBaseBeanHandler() {
        beanGenerique = new DataBaseBeanGenerique();
    }

    public int SelectLogin(String login, String MDP){
        try {
            String query = "SELECT * FROM clients WHERE login = '" + login + "'";

            ResultSet resultSet = beanGenerique.executeSelect(query);

            int id = 0;

            if(resultSet.next()){
                //System.out.println("id = " + resultSet.getInt("id"));
                //System.out.println("login = " + resultSet.getString("login"));
                //System.out.println("password = " + resultSet.getString("password"));

                if(MDP.equals(resultSet.getString("password")))
                    id = resultSet.getInt("id");
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<Facture> selectFactures(boolean paye, int idClient){
        try {
            int p;

            if(paye) p = 1;
            else p = 0;

            String query = "SELECT * FROM factures WHERE paye = " + p + " AND idClient = " + idClient + " AND dateFacture IS NOT NULL";

            ResultSet resultSet = beanGenerique.executeSelect(query);

            LinkedList<Facture> factures = new LinkedList<>();

            while (resultSet.next()){
                int idFacture = Integer.parseInt(resultSet.getString("idFacture"));
                String dateFacture = resultSet.getString("dateFacture");
                Float montant = Float.valueOf(resultSet.getString("montant"));

                Facture facture = new Facture(idFacture, dateFacture, montant);

                factures.add(facture);
            }

            return factures;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int EffectuerPaiement(int idFacture){
        try {
            String query = "UPDATE factures SET paye = 1 where idFacture = " + idFacture;
            int rowsAffected = beanGenerique.executeIUD(query);
            if (rowsAffected > 0) {
                System.out.println("Paiement effectué avec succès.");
                return 1;
            } else {
                System.out.println("Échec du paiement.");
                return 0;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkedList<Vente> selectVentes(int idFacture){
        try {
            String query = "SELECT * FROM ventes WHERE idFacture = " + idFacture;

            ResultSet resultSet = beanGenerique.executeSelect(query);

            LinkedList<Vente> articles = new LinkedList<>();

            while (resultSet.next()){
                int idArticle = resultSet.getInt("idArticle");
                int quantite = resultSet.getInt("quantite");
                String nom = null;
                Float PrixU = null;

                query = "SELECT * FROM articles WHERE id = " + idArticle;

                ResultSet resultSet2 = beanGenerique.executeSelect(query);

                if(resultSet2.next()){
                    nom = resultSet2.getString("intitule");
                    PrixU = resultSet2.getFloat("prix");
                }

                articles.add(new Vente(nom, quantite, PrixU));
            }

            return articles;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            beanGenerique.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
