import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        HttpServer serveur = null;
        try
        {
            serveur = HttpServer.create(new InetSocketAddress(8080),0);
            serveur.createContext("/",new HandlerHtml());
            serveur.createContext("/css",new HandlerCss());
            serveur.createContext("/images",new HandlerImages());
            serveur.createContext("/FormArticle",new HandlerFormulaire());
            System.out.println("Demarrage du serveur HTTP...");
            serveur.start();
        }
        catch (IOException e)
        {
            System.out.println("Erreur: " + e.getMessage());
        }
    }
}