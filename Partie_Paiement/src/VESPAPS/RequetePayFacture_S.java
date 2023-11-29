package VESPAPS;

import ServeurGeneriqueTCP.Requete;

public class RequetePayFacture_S implements Requete {
    private byte[] data;

    public RequetePayFacture_S(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
