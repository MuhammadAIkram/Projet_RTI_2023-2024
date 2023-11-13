package Beans;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseBeanHandler {
    private DataBaseBeanGenerique beanGenerique;

    public DataBaseBeanHandler() {
        beanGenerique = new DataBaseBeanGenerique();
    }

    public int SelectLogin(String login, String MDP){
        try {
            String query = "SELECT * FROM clients WHERE login = '" + login + "'";

            ResultSet resultSet = beanGenerique.executeSelect(query);

            int id = 0;

            if(resultSet.next()){
                //System.out.println("id = " + resultSet.getInt("id"));
                //System.out.println("login = " + resultSet.getString("login"));
                //System.out.println("password = " + resultSet.getString("password"));

                if(MDP.equals(resultSet.getString("password")))
                    id = resultSet.getInt("id");
            }

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            beanGenerique.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
