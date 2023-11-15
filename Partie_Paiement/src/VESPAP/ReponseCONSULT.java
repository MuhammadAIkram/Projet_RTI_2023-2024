package VESPAP;

import Modele.Facture;
import Modele.Vente;
import ServeurGeneriqueTCP.Reponse;

import java.util.LinkedList;

public class ReponseCONSULT implements Reponse {
    private boolean valide;
    private LinkedList<Vente> articles;
    ReponseCONSULT(boolean v, LinkedList<Vente> list) {
        valide = v;
        articles = list;
    }

    public boolean isValide() {
        return valide;
    }

    public LinkedList<Vente> getArticles() {
        return articles;
    }
}
