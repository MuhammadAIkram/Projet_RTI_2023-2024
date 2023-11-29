package VESPAPS;

import ServeurGeneriqueTCP.Requete;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.*;

public class RequeteCONSULT_S implements Requete {
    private int idFacture;
    private byte[] signature; // signature envoyée

    public RequeteCONSULT_S(int idFacture, PrivateKey clePriveeClient) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IOException, SignatureException {
        this.idFacture = idFacture;

        // Construction de la signature
        Signature s = Signature.getInstance("SHA1withRSA","BC");
        s.initSign(clePriveeClient);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idFacture);
        s.update(baos.toByteArray());
        signature = s.sign();
    }

    public int getIdFacture() {
        return idFacture;
    }

    public boolean VerifySignature(PublicKey clePubliqueClient) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignatureException {
        // Construction de l'objet Signature
        Signature s = Signature.getInstance("SHA1withRSA","BC");
        s.initVerify(clePubliqueClient);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idFacture);
        s.update(baos.toByteArray());

        // Vérification de la signature reçue
        return s.verify(signature);
    }
}
