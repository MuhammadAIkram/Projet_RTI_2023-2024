package VESPAPS;

import ServeurGeneriqueTCP.Requete;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.*;

public class RequeteGetFactures_S implements Requete {
    private boolean paye;
    private int idClient;
    private byte[] signature; // signature envoyée

    public RequeteGetFactures_S(boolean paye, int idClient, PrivateKey clePriveeClient) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, IOException {
        //Security.addProvider(new BouncyCastleProvider());

        this.paye = paye;
        this.idClient = idClient;

        // Construction de la signature
        Signature s = Signature.getInstance("SHA1withRSA","BC");
        s.initSign(clePriveeClient);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBoolean(paye);
        dos.writeInt(idClient);
        s.update(baos.toByteArray());
        signature = s.sign();
    }

    public boolean isPaye() {
        return paye;
    }

    public int getIdClient() {
        return idClient;
    }

    public boolean VerifySignature(PublicKey clePubliqueClient) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, IOException, SignatureException {
        // Construction de l'objet Signature
        Signature s = Signature.getInstance("SHA1withRSA","BC");
        s.initVerify(clePubliqueClient);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBoolean(paye);
        dos.writeInt(idClient);
        s.update(baos.toByteArray());

        // Vérification de la signature reçue
        return s.verify(signature);
    }
}
