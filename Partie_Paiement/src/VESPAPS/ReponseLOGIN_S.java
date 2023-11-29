package VESPAPS;

import ServeurGeneriqueTCP.Reponse;

import javax.crypto.SecretKey;

public class ReponseLOGIN_S implements Reponse {
    private boolean valide;
    private int idClient;
    private byte[] cleSession;

    ReponseLOGIN_S(boolean v) {
        valide = v;
    }

    public boolean isValide() {
        return valide;
    }

    public int getIdClient() {
        return idClient;
    }

    public byte[] getCleSession() {
        return cleSession;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public void setCleSession(byte[] cleSession) {
        this.cleSession = cleSession;
    }
}
