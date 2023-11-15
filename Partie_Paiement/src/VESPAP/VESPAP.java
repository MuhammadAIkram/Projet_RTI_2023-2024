package VESPAP;

import Beans.DataBaseBeanHandler;
import Modele.Facture;
import Modele.Vente;
import ServeurGeneriqueTCP.*;

import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

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
        if (requete instanceof RequeteGetFactures) return TraiteRequeteGetFactures((RequeteGetFactures) requete);
        if (requete instanceof RequetePayFacture) return TraiteRequetePayFacture((RequetePayFacture) requete);
        if (requete instanceof RequeteCONSULT) return TraiteRequeteCONSULT((RequeteCONSULT) requete);

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

    private synchronized ReponseGetFactures TraiteRequeteGetFactures(RequeteGetFactures requete) {
        logger.Trace("RequeteGetFactures reçue");

        LinkedList<Facture> factures = dataBaseBeanHandler.selectFactures(requete.isPaye(), requete.getIdClient());

        ReponseGetFactures reponse = new ReponseGetFactures(true, factures);

        return reponse;
    }

    private synchronized ReponsePayFacture TraiteRequetePayFacture(RequetePayFacture requete) {
        logger.Trace("RequetePayFacture reçue");

        String visaRegex = "^4[0-9]{15}$";
        ReponsePayFacture reponse;
        if (requete.getNumeroCarte().matches(visaRegex)) {
            //System.out.println("Valid Visa card number");

            int veri = dataBaseBeanHandler.EffectuerPaiement(requete.getIdFacture());

            if(veri == 1){
                logger.Trace("Paiement Reussi");
                reponse = new ReponsePayFacture(true, false);
            }
            else{
                logger.Trace("Paiement Echec");
                reponse = new ReponsePayFacture(true, true);
            }

        } else {
            //System.out.println("Invalid Visa card number");
            logger.Trace("Carte Invalid!");
            reponse = new ReponsePayFacture(false, true);
        }

        return reponse;
    }

    private synchronized ReponseCONSULT TraiteRequeteCONSULT(RequeteCONSULT requete) {
        logger.Trace("RequeteCONSULT reçue");

        LinkedList<Vente> articles = dataBaseBeanHandler.selectVentes(requete.getIdFacture());

        ReponseCONSULT reponse = new ReponseCONSULT(true, articles);

        return reponse;
    }

    @Override
    public void CloseDatabase(){
        dataBaseBeanHandler.close();
    }
}
