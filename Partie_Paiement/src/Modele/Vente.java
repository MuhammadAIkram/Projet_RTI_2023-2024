package Modele;

import java.io.Serializable;

public class Vente implements Serializable {
    private String nom;
    private int quantite;
    private float PrixUnite;

    public Vente(String nom, int quantite, Float PrixUnite) {
        this.nom = nom;
        this.quantite = quantite;
        this.PrixUnite = PrixUnite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public float getPrixUnite() {
        return PrixUnite;
    }

    public void setPrixUnite(float prixUnite) {
        PrixUnite = prixUnite;
    }
}
