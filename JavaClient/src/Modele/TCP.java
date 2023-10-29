package Modele;

import java.io.*;
import java.net.*;

public class TCP {
    int tailleMax;

    public TCP() {
        tailleMax = 10000;
    }

    public Socket ClientSocket(String IpServeur, int PortServeur) throws IOException {
        Socket csocket;

        // Création de la socket et connexion sur le serveur
        csocket = new Socket(IpServeur,PortServeur);
        System.out.println("Connexion établie.");

        // Caractéristiques de la socket
        System.out.println("--- Socket ---");
        System.out.println("Adresse IP locale : " +
                csocket.getLocalAddress().getHostAddress());
        System.out.println("Port local : " + csocket.getLocalPort());
        System.out.println("Adresse IP distante : " +
                csocket.getInetAddress().getHostAddress());
        System.out.println("Port distant : " + csocket.getPort());

        return csocket;
    }

    public int Send(Socket socket, String data) throws IOException {
        if (data.length() > tailleMax)
            return -1;

        // Convert the string to bytes
        byte[] dataBytes = data.getBytes();

        // Prepare the payload
        byte[] trame = new byte[dataBytes.length + 2];
        System.arraycopy(dataBytes, 0, trame, 0, dataBytes.length);
        trame[dataBytes.length] = '%';
        trame[dataBytes.length + 1] = ')';

        // Write to the socket
        socket.getOutputStream().write(trame);
        return dataBytes.length + 2;
    }

    public String Receive(Socket socket) throws IOException {
        boolean fini = false;
        StringBuilder receivedData = new StringBuilder();
        int nbLus;
        byte lu1, lu2;
        byte[] buffer = new byte[2];

        while (!fini) {
            if ((nbLus = socket.getInputStream().read(buffer, 0, 1)) == -1)
                return null;
            if (nbLus == 0) return receivedData.toString(); // Connection closed by the client

            lu1 = buffer[0];

            if (lu1 == '%') {
                if ((nbLus = socket.getInputStream().read(buffer, 0, 1)) == -1)
                    return null;

                if (nbLus == 0) return receivedData.toString(); // Connection closed by the client

                lu2 = buffer[0];

                if (lu2 == ')') fini = true;
                else {
                    receivedData.append((char) lu1);
                    receivedData.append((char) lu2);
                }
            } else {
                receivedData.append((char) lu1);
            }
        }

        return receivedData.toString();
    }
}
