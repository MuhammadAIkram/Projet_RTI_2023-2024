package VESPAP;

import Modele.Facture;
import ServeurGeneriqueTCP.Reponse;

import java.util.LinkedList;

public class ReponseGetFactures implements Reponse
{
    private boolean valide;
    private LinkedList<Facture> factures;
    ReponseGetFactures(boolean v, LinkedList<Facture> list) {
        valide = v;
        factures = list;
    }

    public boolean isValide() {
        return valide;
    }

    public LinkedList<Facture> getFactures() {
        return factures;
    }
}
