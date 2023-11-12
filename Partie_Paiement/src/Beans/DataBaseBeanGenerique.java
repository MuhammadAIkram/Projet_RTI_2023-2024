package Beans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DataBaseBeanGenerique {
    Connection connection;

    public DataBaseBeanGenerique() {
        try {
            // Chargement du driver
            Class leDriver = Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Obtention du driver OK...");

            InputStream input = new FileInputStream("./Fichiers/config.properties");

            Properties properties = new Properties();
            properties.load(input);

            String ipAdress = properties.getProperty("IP_Database");

            // Connexion a la BD
            connection = DriverManager.getConnection("jdbc:mysql://"+ipAdress+"/PourStudent","Student","PassStudent1_");

            System.out.println("Connexion à la BD PourStudent OK...");
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println("Erreur ClassNotFoundException: " + ex.getMessage());
        }
        catch (SQLException ex)
        {
            System.out.println("Erreur SQLException: " + ex.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            System.out.println("Connexion fermer avec DB!");
            connection.close();
        }
    }

    // Méthode pour exécuter une requête SELECT
    public ResultSet executeSelect(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    // Méthode pour exécuter une requête INSERT, UPDATE, DELETE
    public int executeIUD(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
}
