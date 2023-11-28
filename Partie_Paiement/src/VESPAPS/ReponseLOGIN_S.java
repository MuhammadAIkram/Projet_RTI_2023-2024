package VESPAPS;

import ServeurGeneriqueTCP.Reponse;

import javax.crypto.SecretKey;

public class ReponseLOGIN_S implements Reponse {
    private boolean valide;
    private int idClient;
    private SecretKey cleSession;

    ReponseLOGIN_S(boolean v) {
        valide = v;
    }

    public boolean isValide() {
        return valide;
    }

    public int getIdClient() {
        return idClient;
    }

    public SecretKey getCleSession() {
        return cleSession;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public void setCleSession(SecretKey cleSession) {
        this.cleSession = cleSession;
    }
}
