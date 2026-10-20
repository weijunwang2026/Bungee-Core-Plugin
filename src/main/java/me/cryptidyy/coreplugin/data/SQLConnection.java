package me.cryptidyy.coreplugin.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private String host = "localhost";
    private String port = "3306"; // 3308
    private String database = "communitcraft";
    private String username = "root";
    private String password = "cryptidyy";

    private Connection connection;

    public boolean isConnected()
    {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException
    {
        if(isConnected())
        {
            return;
        }

        connection = DriverManager.getConnection("jdbc:mysql://" +
                host + ":" + port + "/" + database + "?useSSL=false", username, password);
    }

    public void disconnect()
    {
        if(isConnected())
        {
            try
            {
                connection.close();
            }
            catch(SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection()
    {
        return connection;
    }
}
