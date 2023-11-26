package VESPAPS;

import ServeurGeneriqueTCP.Reponse;

public class ReponseLOGIN_S implements Reponse {
    private boolean valide;
    private int idClient;

    ReponseLOGIN_S(boolean v) {
        valide = v;
    }

    public boolean isValide() {
        return valide;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }
}
