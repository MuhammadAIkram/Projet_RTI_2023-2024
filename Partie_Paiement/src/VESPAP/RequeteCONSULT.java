package VESPAP;

import ServeurGeneriqueTCP.Requete;

public class RequeteCONSULT implements Requete {
    private int idFacture;

    public RequeteCONSULT(int idFacture) {
        this.idFacture = idFacture;
    }

    public int getIdFacture() {
        return idFacture;
    }
}
