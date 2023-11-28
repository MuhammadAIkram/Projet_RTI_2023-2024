package VESPAPS;

import Modele.Facture;
import ServeurGeneriqueTCP.Reponse;

import java.util.LinkedList;

public class ReponseGetFactures_S implements Reponse {
    private boolean valide;
    private byte[] factures;
    public ReponseGetFactures_S(boolean v, byte[] list) {
        valide = v;
        factures = list;
    }

    public boolean isValide() {
        return valide;
    }

    public byte[] getFactures() {
        return factures;
    }
}
