package Modele;

import java.io.Serializable;

public class Article implements Serializable {
    private int id;
    private String nom;
    private int quantite;
    private float PrixUnite;
    private String image;

    public Article(int id, String nom, int quantite, Float PrixUnite, String image) {
        this.id = id;
        this.nom = nom;
        this.quantite = quantite;
        this.PrixUnite = PrixUnite;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getImage() {
        return image;
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
