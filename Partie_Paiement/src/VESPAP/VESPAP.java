package VESPAP;

import Beans.DataBaseBeanHandler;
import ServeurGeneriqueTCP.*;

import java.net.Socket;
import java.util.HashMap;

public class VESPAP implements Protocole
{
    private HashMap<String,Socket> clientsConnectes;
    private Logger logger;

    private DataBaseBeanHandler dataBaseBeanHandler;

    public VESPAP(Logger log)
    {
        logger = log;

        clientsConnectes = new HashMap<>();

        dataBaseBeanHandler = new DataBaseBeanHandler();
    }

    @Override
    public String getNom() {
        return "VESPAP";
    }

    @Override
    public synchronized Reponse TraiteRequete(Requete requete, Socket socket)
    {
        if (requete instanceof RequeteLOGIN) return TraiteRequeteLOGIN((RequeteLOGIN) requete, socket);
        if (requete instanceof RequeteLOGOUT) return TraiteRequeteLOGOUT((RequeteLOGOUT) requete);

        return null;
    }

    private synchronized ReponseLOGIN TraiteRequeteLOGIN(RequeteLOGIN requete, Socket socket)
    {
        logger.Trace("RequeteLOGIN reçue de " + requete.getLogin());

        if(clientsConnectes.containsKey(requete.getLogin()))
        {
            logger.Trace(requete.getLogin() + " --> erreur de login, Client deja logge");
            return new ReponseLOGIN(false);
        }

        int id = dataBaseBeanHandler.SelectLogin(requete.getLogin(),requete.getPassword());

        if(id != 0)
        {
            String ipPortClient = socket.getInetAddress().getHostAddress() + "/" + socket.getPort();
            logger.Trace(requete.getLogin() + " correctement loggé de " + ipPortClient);
            clientsConnectes.put(requete.getLogin(),socket);

            ReponseLOGIN reponse = new ReponseLOGIN(true);
            reponse.setIdClient(id);
            return reponse;
        }
        else
        {
            logger.Trace(requete.getLogin() + " --> erreur de login");
            return new ReponseLOGIN(false);
        }
    }

    private synchronized ReponseLOGOUT TraiteRequeteLOGOUT(RequeteLOGOUT requete)
    {
        logger.Trace("RequeteLOGOUT reçue de " + requete.getLogin());
        clientsConnectes.remove(requete.getLogin());
        logger.Trace(requete.getLogin() + " correctement déloggé");

        ReponseLOGOUT reponse = new ReponseLOGOUT(true);
        return reponse;
    }
    @Override
    public void CloseDatabase(){
        dataBaseBeanHandler.close();
    }
}
