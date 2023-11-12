package VESPAP;

import ServeurGeneriqueTCP.*;

public class ReponseLOGIN implements Reponse
{
    private boolean valide;
    private int idClient;

    ReponseLOGIN(boolean v) {
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
