package Beans;

import Modele.Article;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.json.*;
import javax.json.stream.JsonGenerator;

public class DataBaseBeanHandler {
    private DataBaseBeanGenerique beanGenerique;

    public DataBaseBeanHandler() {
        beanGenerique = new DataBaseBeanGenerique();
    }

    public synchronized LinkedList<Article> getAllArticles(){
        try {
            String query = "SELECT * FROM articles";

            ResultSet resultSet = beanGenerique.executeSelect(query);

            LinkedList<Article> articles = new LinkedList<>();

            while (resultSet.next()){
                int idArticle = resultSet.getInt("id");
                String nom = resultSet.getString("intitule");
                Float PrixU = resultSet.getFloat("prix");
                int stock = resultSet.getInt("stock");
                String image = resultSet.getString("image");

                articles.add(new Article(idArticle, nom, stock, PrixU, image));
            }

            return articles;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeArticlesToJsonFile() throws SQLException, IOException {
        List<Article> articles = getAllArticles();
        JsonArray jsonArray = Json.createArrayBuilder().build();

        for (Article article : articles) {
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("id", article.getId())
                    .add("intitule", article.getNom())
                    .add("prix", article.getPrixUnite())
                    .add("stock", article.getQuantite())
                    .add("image", article.getImage())
                    .build();
            jsonArray = Json.createArrayBuilder(jsonArray).add(jsonObject).build();
        }

        JsonWriterFactory writerFactory = Json.createWriterFactory(
                java.util.Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, "true")
        );

        try (FileWriter fileWriter = new FileWriter("./Fichiers/articles.json")) {
            JsonWriter jsonWriter = writerFactory.createWriter(fileWriter);
            jsonWriter.writeArray(jsonArray);
        }
    }

    public synchronized void updateData(int id, float prix, int stock) throws SQLException {
        String query = "UPDATE articles SET prix = " + prix + ", stock = "+ stock +" WHERE id = " + id;
        beanGenerique.executeIUD(query);

        System.out.println("Mise a jour succes!!!!");
    }

    public void close() {
        try {
            beanGenerique.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
