package me.cryptidyy.coreplugin.spigotcommands;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.data.SQLConnection;
import me.cryptidyy.coreplugin.utils.Deactivated;
import me.cryptidyy.coreplugin.utils.Disabler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandSQL implements Deactivated {

    private final String TABLE_PREFIX = "cc_";
    private SQLConnection sqlConnection = Main.getInstance().sqlConnection;

    public CommandSQL()
    {
        importDatabase();
        Disabler.getInstance().registerDeactivated(this);
    }

    //uuid (String), cmd (String), is_executing (Boolean)
    private void importDatabase()
    {
        Connection con = sqlConnection.getConnection();

        PreparedStatement prepStmt;
        try {
            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "commands` ("
                    + "`uuid` VARCHAR(200) NOT NULL, "
                    + "cmd VARCHAR(200), "
                    + "is_executing TINYINT(1) DEFAULT 0, "
                    + "PRIMARY KEY (`uuid`));");
            prepStmt.executeUpdate();
            //prepStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Listener gets all commands in the table
    public Map<UUID, String> getCommands()
    {
        Connection con = sqlConnection.getConnection();

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Map<UUID, String> commandsMap = new HashMap<>();

        try
        {
            rs = con.createStatement().executeQuery("SELECT * FROM " + TABLE_PREFIX + "commands;");
            while(rs.next())
            {
                commandsMap.put(UUID.fromString(rs.getString("uuid")), rs.getString("cmd"));
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return commandsMap;
    }

    //the listener only executes the command that isn't already executing
    public boolean isExecuting(UUID uuid)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs;

        try
        {
            prepStmt = con.prepareStatement("SELECT is_executing FROM " + TABLE_PREFIX + "commands WHERE uuid = ?");
            prepStmt.setString(1, uuid.toString());
            rs = prepStmt.executeQuery();

            if(rs.next())
            {
                return rs.getBoolean("is_executing");
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    //Command is set to executing when the listener recieves the command
    public void setExecuting(UUID uuid, boolean isExecuting)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;

        try
        {
            prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "commands SET is_executing = ? WHERE uuid = ?");
            prepStmt.setBoolean(1, isExecuting);
            prepStmt.setString(2, uuid.toString());
            prepStmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Removes the command after it is FINISHED executing
    //Player cannot execute the command again while it still exists
    public void removeCommand(UUID uuid)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;

        try
        {
            prepStmt = con.prepareStatement("DELETE FROM " + TABLE_PREFIX + "commands WHERE uuid = '" + uuid + "'");
            prepStmt.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable()
    {
        sqlConnection.disconnect();
    }
}
