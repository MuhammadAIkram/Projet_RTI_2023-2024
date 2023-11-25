package VESPAPS;

import Beans.DataBaseBeanHandler;
import ServeurGeneriqueTCP.*;

import java.net.Socket;
import java.util.HashMap;

public class VESPAPS implements Protocole
{
    private HashMap<String,Socket> clientsConnectes;
    private Logger logger;
    private DataBaseBeanHandler dataBaseBeanHandler;

    public VESPAPS(Logger logger) {
        this.logger = logger;

        clientsConnectes = new HashMap<>();

        dataBaseBeanHandler = new DataBaseBeanHandler();
    }

    @Override
    public String getNom() {
        return "VESPAPS";
    }

    @Override
    public Reponse TraiteRequete(Requete requete, Socket socket)
    {
        return null;
    }

    @Override
    public void CloseDatabase() {
        dataBaseBeanHandler.close();
    }
}
