package VESPAPS;

import ServeurGeneriqueTCP.Reponse;

public class ReponseCONSULT_S implements Reponse {
    private boolean valide;
    private byte[] articles;

    public ReponseCONSULT_S(boolean v, byte[] list) {
        valide = v;
        articles = list;
    }

    public boolean isValide() {
        return valide;
    }

    public byte[] getArticles() {
        return articles;
    }
}
