package VESPAP;

import ServeurGeneriqueTCP.*;

public class ReponseLOGIN implements Reponse
{
    private boolean valide;

    ReponseLOGIN(boolean v) {
        valide = v;
    }
    public boolean isValide() {
        return valide;
    }
}
