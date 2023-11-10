package VESPAP;

import ServeurGeneriqueTCP.*;

public class RequeteLOGIN implements Requete
{
    private String login;
    private String password;
    public RequeteLOGIN(String l,String p) {
        login = l;
        password = p;
    }

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
}
