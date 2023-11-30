import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("./Fichiers/KeystoreServeur.jks"), "PassKeystore".toCharArray());
        Enumeration<String> en = ks.aliases();
        ArrayList<String> vec = new ArrayList<>();
        while (en.hasMoreElements()) vec.add(en.nextElement());

        for (String alias:vec)
        {
            if (ks.isKeyEntry(alias)) System.out.println("[keyEntry] --> " + alias);
            if (ks.isCertificateEntry(alias))
            {
                System.out.println("[trustedCertificateEntry] --> " + alias);

                X509Certificate certif = (X509Certificate)ks.getCertificate(alias);
                System.out.println("\tType de certificat : " + certif.getType());
                System.out.println("\tNom du propriétaire du certificat : " +
                        certif.getSubjectDN().getName());
                PublicKey clePublique = certif.getPublicKey();
                System.out.println("\tCle publique recuperee = " +
                        clePublique.toString());
            }
        }
    }
}