package VESPAP;

import ServeurGeneriqueTCP.Requete;

public class RequetePayFacture implements Requete
{
    private int idFacture;
    private String nomCarte;
    private String numeroCarte;

    public RequetePayFacture(int idFacture, String nomCarte, String numeroCarte) {
        this.idFacture = idFacture;
        this.nomCarte = nomCarte;
        this.numeroCarte = numeroCarte;
    }

    public int getIdFacture() {
        return idFacture;
    }

    public String getNomCarte() {
        return nomCarte;
    }

    public String getNumeroCarte() {
        return numeroCarte;
    }
}
