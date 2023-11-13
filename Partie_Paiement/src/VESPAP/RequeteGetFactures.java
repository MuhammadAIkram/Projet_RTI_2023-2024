package VESPAP;

import ServeurGeneriqueTCP.Requete;

public class RequeteGetFactures implements Requete
{
    private boolean paye;
    private int idClient;

    public RequeteGetFactures(boolean paye, int idClient) {
        this.paye = paye;
        this.idClient = idClient;
    }

    public boolean isPaye() {
        return paye;
    }

    public int getIdClient() {
        return idClient;
    }
}
