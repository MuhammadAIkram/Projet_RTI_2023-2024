package VESPAP;

import ServeurGeneriqueTCP.Reponse;

public class ReponsePayFacture implements Reponse {
    private boolean valide;
    private boolean echec;

    ReponsePayFacture(boolean v, boolean e) {
        valide = v;
        echec = e;
    }

    public boolean isValide() {
        return valide;
    }

    public boolean isEchec() {
        return echec;
    }
}
