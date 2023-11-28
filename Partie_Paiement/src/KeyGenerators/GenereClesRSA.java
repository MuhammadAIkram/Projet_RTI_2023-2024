package KeyGenerators;

import java.io.*;
import java.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class GenereClesRSA {
    public static void main(String args[]) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());

        // Génération des clés
        KeyPairGenerator genCles = KeyPairGenerator.getInstance("RSA","BC");
        genCles.initialize(512,new SecureRandom()); // 512 par exemple
        KeyPair deuxCles = genCles.generateKeyPair();
        PublicKey clePublique = deuxCles.getPublic();
        PrivateKey clePrivee = deuxCles.getPrivate();
        System.out.println(" *** Cle publique generee = " + clePublique);
        System.out.println(" *** Cle privee generee = " + clePrivee);

        // Sérialisation des clés dans des fichiers différents
        ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream("./Fichiers/clePubliqueClient.ser"));
        oos1.writeObject(clePublique);
        oos1.close();
        System.out.println("Sérialisation de la clé publique dans le fichier clePubliqueClient.ser");

        ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream("./Fichiers/clePriveeClient.ser"));
        oos2.writeObject(clePrivee);
        oos2.close();
        System.out.println("Sérialisation de la clé privée dans le fichier clePriveeClient.ser");
    }
}
