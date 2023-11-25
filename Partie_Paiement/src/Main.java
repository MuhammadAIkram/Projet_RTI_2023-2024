import java.security.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        Provider prov[] = Security.getProviders();
        for (int i=0; i<prov.length; i++)
            System.out.println(prov[i].getName() + "/" + prov[i].getVersion());
    }
}