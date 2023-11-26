package VESPAPS;

import Beans.DataBaseBeanHandler;
import ServeurGeneriqueTCP.*;
import VESPAP.ReponseLOGOUT;
import VESPAP.RequeteLOGOUT;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.HashMap;

public class VESPAPS implements Protocole
{
    private HashMap<String,Socket> clientsConnectes;
    private Logger logger;
    private DataBaseBeanHandler dataBaseBeanHandler;

    public VESPAPS(Logger logger) {
        Security.addProvider(new BouncyCastleProvider());

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
        if (requete instanceof RequeteLOGIN_S) return TraiteRequeteLOGIN_S((RequeteLOGIN_S) requete, socket);
        if (requete instanceof RequeteLOGOUT) return TraiteRequeteLOGOUT((RequeteLOGOUT) requete);

        return null;
    }

    private synchronized ReponseLOGIN_S TraiteRequeteLOGIN_S(RequeteLOGIN_S requete, Socket socket) {
        logger.Trace("RequeteLOGIN reçue de " + requete.getLogin());

        if(clientsConnectes.containsKey(requete.getLogin()))
        {
            logger.Trace(requete.getLogin() + " --> erreur de login, Client deja logge");
            return new ReponseLOGIN_S(false);
        }

        String MDP = dataBaseBeanHandler.RecupereMotDePasse(requete.getLogin());

        if (MDP == null)
        {
            logger.Trace(requete.getLogin() + " --> Client inconnu !");
            return new ReponseLOGIN_S(false);
        }

        try {
            if(!requete.VerifyPassword(MDP)){
                logger.Trace(requete.getLogin() + " --> Mauvais mot de passe !");
                return new ReponseLOGIN_S(false);
            }
        }
        catch (NoSuchAlgorithmException | IOException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

        int id = dataBaseBeanHandler.SelectLogin(requete.getLogin(),MDP);

        System.out.println("yo");

        if(id != 0)
        {
            String ipPortClient = socket.getInetAddress().getHostAddress() + "/" + socket.getPort();
            logger.Trace(requete.getLogin() + " correctement loggé de " + ipPortClient);
            clientsConnectes.put(requete.getLogin(),socket);

            ReponseLOGIN_S reponse = new ReponseLOGIN_S(true);
            reponse.setIdClient(id);
            return reponse;
        }
        else
        {
            logger.Trace(requete.getLogin() + " --> erreur de login");
            return new ReponseLOGIN_S(false);
        }
    }

    private synchronized ReponseLOGOUT TraiteRequeteLOGOUT(RequeteLOGOUT requete) {
        logger.Trace("RequeteLOGOUT reçue de " + requete.getLogin());
        clientsConnectes.remove(requete.getLogin());
        logger.Trace(requete.getLogin() + " correctement déloggé");

        ReponseLOGOUT reponse = new ReponseLOGOUT(true);
        return reponse;
    }

    @Override
    public void CloseDatabase() {
        dataBaseBeanHandler.close();
    }
}
