package VESPAPS;

import ServeurGeneriqueTCP.Requete;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.util.Date;

public class RequeteLOGIN_S implements Requete {
    private String login;
    private long temps;
    private double alea;
    private byte[] digest; // digest envoyé

    public RequeteLOGIN_S(String login,String password) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        Security.addProvider(new BouncyCastleProvider());

        this.login = login;

        // Construction du sel
        this.temps = new Date().getTime();
        this.alea = Math.random();

        // Construction du digest salé
        MessageDigest md = MessageDigest.getInstance("SHA-1","BC");
        md.update(login.getBytes());
        md.update(password.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(temps);
        dos.writeDouble(alea);
        md.update(baos.toByteArray());
        digest = md.digest();
    }

    public String getLogin() { return login; }

    public boolean VerifyPassword(String password) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        // Construction du digest local
        MessageDigest md = MessageDigest.getInstance("SHA-1","BC");
        md.update(login.getBytes());
        md.update(password.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(temps);
        dos.writeDouble(alea);
        md.update(baos.toByteArray());
        byte[] digestLocal = md.digest();

        // Comparaison digest reçu et digest local
        return MessageDigest.isEqual(digest,digestLocal);
    }

}
