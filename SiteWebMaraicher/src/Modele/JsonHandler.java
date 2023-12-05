package Modele;

import Beans.DataBaseBeanHandler;

import java.io.IOException;
import java.sql.SQLException;

public class JsonHandler {
    public static void CreateJson() throws SQLException, IOException {
        DataBaseBeanHandler dataBaseBeanHandler = new DataBaseBeanHandler();

        dataBaseBeanHandler.writeArticlesToJsonFile();

        dataBaseBeanHandler.close();
    }
}
