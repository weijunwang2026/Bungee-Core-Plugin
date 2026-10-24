package me.cryptidyy.coreplugin.data;

import me.cryptidyy.coreplugin.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private final String host;
    private final String port; // 3308
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    // Reads the database login info from config file
    public SQLConnection(Main plugin) throws Exception
    {
        DataManager data = new DataManager(plugin, "mysql.yml");
        host = data.getConfig().getString("host");
        port = data.getConfig().getString("port");
        database = data.getConfig().getString("database");
        username = data.getConfig().getString("username");
        password = data.getConfig().getString("password");

        if (host == null || port == null || database == null || username == null || password == null)
        {
            throw new Exception();
        }
    }
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
