import Modele.JsonHandler;
import com.sun.net.httpserver.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.stream.Collectors;


public class HandlerFormulaire implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Lecture de la requete
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        System.out.print("HandlerFormulaire (methode " + requestMethod + ") = " + requestPath + " --> ");
        // Ecriture de la reponse
        if(requestPath.equals("/FormArticle")){
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            reader.close();

            // Convertion vers object json
            String jsonData = requestBody.toString();
            JsonReader jsonReader = Json.createReader(new java.io.StringReader(jsonData));
            JsonObject jsonObject = jsonReader.readObject();

            // acces au valeur du object json en utilisent ces cles
            float prix = Float.parseFloat(jsonObject.getString("prix"));
            int stock = Integer.parseInt(jsonObject.getString("stock"));
            int id = jsonObject.getInt("id");

            System.out.print("(id: " + id + " - prix: " + prix + " - stock: " + stock + ") || ");

            try {
                JsonHandler.UpdateDatabase(id, prix, stock);
                JsonHandler.CreateJson();
            } catch (SQLException e) {
                Erreur404(exchange);
            }

            String response = "Data updated successfully";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            System.out.println("OK");
        }
        else Erreur404(exchange);
    }

    private void Erreur404(HttpExchange exchange) throws IOException
    {
        String reponse = "Erreur form !!!";
        exchange.sendResponseHeaders(404, reponse.length());
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        OutputStream os = exchange.getResponseBody();
        os.write(reponse.getBytes());
        os.close();
        System.out.println("KO");
    }
}
