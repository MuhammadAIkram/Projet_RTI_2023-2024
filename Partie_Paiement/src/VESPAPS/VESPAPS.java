package VESPAPS;

import Beans.DataBaseBeanHandler;
import Modele.Facture;
import ServeurGeneriqueTCP.*;
import VESPAP.ReponseLOGOUT;
import VESPAP.RequeteLOGOUT;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.HashMap;
import java.util.LinkedList;
import MyCrypto.MyCrypto;

public class VESPAPS implements Protocole
{
    private HashMap<String,Socket> clientsConnectes;
    private Logger logger;
    private DataBaseBeanHandler dataBaseBeanHandler;

    private PublicKey clePubliqueClient;
    private SecretKey cleSession;

    public VESPAPS(Logger logger) {
        Security.addProvider(new BouncyCastleProvider());

        this.logger = logger;

        clientsConnectes = new HashMap<>();

        dataBaseBeanHandler = new DataBaseBeanHandler();

        try {
            clePubliqueClient = RecupereClePubliqueClient();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Génération d'une clé de session
        KeyGenerator cleGen;
        try {
            cleGen = KeyGenerator.getInstance("DES","BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        cleGen.init(new SecureRandom());
        cleSession = cleGen.generateKey();
        System.out.println("Génération d'une clé de session : " + cleSession);
    }

    @Override
    public String getNom() {
        return "VESPAPS";
    }

    @Override
    public synchronized Reponse TraiteRequete(Requete requete, Socket socket)
    {
        if (requete instanceof RequeteLOGIN_S) return TraiteRequeteLOGIN_S((RequeteLOGIN_S) requete, socket);
        if (requete instanceof RequeteLOGOUT) return TraiteRequeteLOGOUT((RequeteLOGOUT) requete);
        if (requete instanceof RequeteGetFactures_S) return TraiteRequeteGetFactures((RequeteGetFactures_S) requete);

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

        if(id != 0)
        {
            String ipPortClient = socket.getInetAddress().getHostAddress() + "/" + socket.getPort();
            logger.Trace(requete.getLogin() + " correctement loggé de " + ipPortClient);
            clientsConnectes.put(requete.getLogin(),socket);

            ReponseLOGIN_S reponse = new ReponseLOGIN_S(true);
            reponse.setIdClient(id);
            reponse.setCleSession(cleSession);
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

    private ReponseGetFactures_S TraiteRequeteGetFactures(RequeteGetFactures_S requete) {
        logger.Trace("RequeteGetFactures reçue");

        try {
            if(!requete.VerifySignature(clePubliqueClient)){
                logger.Trace("Mauvais signature !");
                return new ReponseGetFactures_S(false, null);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | IOException | SignatureException e) {
            throw new RuntimeException(e);
        }

        LinkedList<Facture> factures = dataBaseBeanHandler.selectFactures(requete.isPaye(), requete.getIdClient());

        // Constructon du vecteur de bytes du message clair
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            // Writing the linked list object to ObjectOutputStream
            oos.writeObject(factures);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] messageClair = baos.toByteArray();
        System.out.println("Construction du message à envoyer");

        byte[] messageCrypte;
        try {
            messageCrypte = MyCrypto.CryptSymDES(cleSession,messageClair);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Cryptage symétrique du message : " + new String(messageCrypte));

        return new ReponseGetFactures_S(true, messageCrypte);
    }

    public static PublicKey RecupereClePubliqueClient() throws IOException, ClassNotFoundException {
        // Désérialisation de la clé publique
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("./Fichiers/clePubliqueClient.ser"));
        PublicKey cle = (PublicKey) ois.readObject();
        ois.close();

        System.out.println("Cle publique recuperer");

        return cle;
    }

    @Override
    public void CloseDatabase() {
        dataBaseBeanHandler.close();
    }
}
