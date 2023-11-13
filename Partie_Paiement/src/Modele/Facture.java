package Modele;

import java.io.Serializable;

public class Facture implements Serializable {
    private int idFacture;
    private String dateFacture;
    private Float montant;

    public Facture(int idFacture, String dateFacture, Float montant) {
        this.idFacture = idFacture;
        this.dateFacture = dateFacture;
        this.montant = montant;
    }

    public int getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(int idFacture) {
        this.idFacture = idFacture;
    }

    public String getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(String dateFacture) {
        this.dateFacture = dateFacture;
    }

    public Float getMontant() {
        return montant;
    }

    public void setMontant(Float montant) {
        this.montant = montant;
    }
}
