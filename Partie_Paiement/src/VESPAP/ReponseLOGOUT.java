package VESPAP;

import ServeurGeneriqueTCP.Reponse;

public class ReponseLOGOUT implements Reponse {
    private boolean valide;

    public ReponseLOGOUT(boolean v) {
        valide = v;
    }

    public boolean isValide() {
        return valide;
    }
}
