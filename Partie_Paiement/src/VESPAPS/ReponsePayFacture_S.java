package VESPAPS;

import ServeurGeneriqueTCP.Reponse;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class ReponsePayFacture_S implements Reponse {
    private boolean valide;
    private boolean echec;
    private byte[] hmac; // hmac envoyé

    public ReponsePayFacture_S(boolean v, boolean e, SecretKey cleSession) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IOException {
        valide = v;
        echec = e;

        // Construction du HMAC
        Mac hm = Mac.getInstance("HMAC-MD5","BC");
        hm.init(cleSession);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBoolean(v);
        dos.writeBoolean(e);
        hm.update(baos.toByteArray());
        hmac = hm.doFinal();
    }

    public boolean isValide() {
        return valide;
    }

    public boolean isEchec() {
        return echec;
    }

    public boolean VerifyAuthenticity(SecretKey cleSession) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        // Construction du HMAC local
        Mac hm = Mac.getInstance("HMAC-MD5","BC");
        hm.init(cleSession);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBoolean(valide);
        dos.writeBoolean(echec);
        hm.update(baos.toByteArray());
        byte[] hmacLocal = hm.doFinal();

        // Comparaison HMAC reçu et HMAC local
        return MessageDigest.isEqual(hmac,hmacLocal);
    }
}
