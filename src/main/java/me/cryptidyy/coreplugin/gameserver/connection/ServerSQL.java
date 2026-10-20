package me.cryptidyy.coreplugin.gameserver.connection;

import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.data.SQLConnection;
import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.gameserver.GameServerMySQL;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManager;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import me.cryptidyy.coreplugin.utils.Deactivated;
import me.cryptidyy.coreplugin.utils.Disabler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.*;
import java.util.*;

public class ServerSQL implements Deactivated {

    //the state of the general server
    //connects with spigot to see if a player can join that server

    private final String TABLE_PREFIX = "cc_";
    private SQLConnection sqlConnection = Main.getInstance().sqlConnection;
    public ServerSQL() throws SQLException, ClassNotFoundException {

        //create table if not exists
        importDatabase();
        Disabler.getInstance().registerDeactivated(this);
    }

    private void importDatabase() {
        Connection con = sqlConnection.getConnection();

        PreparedStatement prepStmt = null;
        try {
            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "server_status` ("
                    + "`server_id` VARCHAR(7) NOT NULL, "
                    + "is_started TINYINT(1) DEFAULT 0, "
                    + "is_waiting TINYINT(1) DEFAULT 0, "
                    + "total_player_num INT(8) DEFAULT 0, "
                    + "max_player_num INT(8) DEFAULT 12, "
                    + "PRIMARY KEY (`server_id`));");
            prepStmt.executeUpdate();

            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "server_type` ("
                    + "`server_id` VARCHAR(7) NOT NULL, "
                    + "game_name VARCHAR(45), "
                    + "PRIMARY KEY (`server_id`));");
            prepStmt.executeUpdate();
            //prepStmt.close();

            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "servers_can_join` ("
                    + "`player_id` INT(8) NOT NULL, "
                    + "server_id VARCHAR(7), "
                    + "can_join TINYINT(1) DEFAULT 0, "
                    + "reason VARCHAR(200), "
                    + "PRIMARY KEY (`player_id`, `server_id`));");
            prepStmt.executeUpdate();

            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "queues_in_servers` ("
                    + "`server_id` VARCHAR(7) NOT NULL, "
                    + "player_num INT(8) DEFAULT 0, "
                    + "PRIMARY KEY (`server_id`));");
            prepStmt.executeUpdate();
            //prepStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //get and set
    public boolean isStarted(String id) {
        boolean isStarted = false;

        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            rs = (stmt = con.createStatement()).executeQuery(
                    "SELECT is_started FROM " + TABLE_PREFIX + "server_status WHERE server_id = '" + id + "'");
            if (rs.next()) {
                isStarted = rs.getBoolean("is_started");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isStarted;
    }

    public boolean isWaiting(String id) {
        boolean isWaiting = false;

        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            rs = (stmt = con.createStatement()).executeQuery(
                    "SELECT is_waiting FROM " + TABLE_PREFIX + "server_status WHERE server_id = '" + id + "'");
            if (rs.next()) {
                isWaiting = rs.getBoolean("is_waiting");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isWaiting;
    }

    public int playersOnline(String id) {
        int playersOnline = 0;

        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            rs = (stmt = con.createStatement()).executeQuery(
                    "SELECT total_player_num FROM " + TABLE_PREFIX + "server_status WHERE server_id = '" + id + "'");
            if (rs.next()) {
                playersOnline = rs.getInt("total_player_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playersOnline;
    }

    public int playersInQueue(String id) {
        int playersInQueue = 0;

        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            rs = (stmt = con.createStatement()).executeQuery(
                    "SELECT player_num FROM " + TABLE_PREFIX + "queues_in_servers WHERE server_id = '" + id + "'");
            if (rs.next()) {
                playersInQueue = rs.getInt("player_num");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playersInQueue;
    }

    public void registerGameServer(String id, String gameName) {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = con.prepareStatement(
                    "INSERT INTO " + TABLE_PREFIX + "server_status (server_id) VALUES ('" + id + "')" +
                            "ON DUPLICATE KEY UPDATE server_id = '" + id + "'");
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            prepStmt = con.prepareStatement(
                    "INSERT INTO " + TABLE_PREFIX + "server_type (server_id, game_name) VALUES ('" + id + "','" + gameName + "')"
                            + "ON DUPLICATE KEY UPDATE server_id = '" + id + "'");
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            prepStmt = con.prepareStatement(
                    "INSERT INTO " + TABLE_PREFIX + "queues_in_servers (server_id) VALUES ('" + id + "')"
                            + "ON DUPLICATE KEY UPDATE server_id = '" + id + "'");
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<GameServer> findServersByStatus(String gameName)
    {
        Set<GameServer> availableServers = new HashSet<>();

        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            prepStmt = con.prepareStatement(
                    "SELECT * FROM " + TABLE_PREFIX + "server_type st INNER JOIN " + TABLE_PREFIX + "server_status ss " +
                            "ON st.server_id = ss.server_id " +
                            "WHERE game_name = '" + gameName + "' AND is_waiting = TRUE AND is_started = TRUE;");
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                availableServers.add(new GameServerMySQL(rs.getString("server_id"), gameName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableServers;
    }

    public GameServer getGameServer(String serverID)
    {
        Connection con = sqlConnection.getConnection();
        GameServer result = null;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT game_name FROM " + TABLE_PREFIX + "server_type WHERE (server_id = ?)");
            ps.setString(1, serverID);

            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                result = new GameServerMySQL(serverID, rs.getString("game_name"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public Set<GameServer> getAllOnlineGameServers()
    {
        Connection con = sqlConnection.getConnection();
        Set<GameServer> result = new HashSet<>();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT ss.server_id, st.game_name FROM "
                    + TABLE_PREFIX + "server_status ss INNER JOIN "
                    + TABLE_PREFIX + "server_type st WHERE(ss.server_id = st.server_id AND is_started = 1)");
            ResultSet rs = ps.executeQuery();

            while(rs.next())
            {
                result.add(new GameServerMySQL(rs.getString("server_id"), rs.getString("game_name")));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    public int getMaxPlayerNum(String serverID)
    {
        Connection con = sqlConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT max_player_num FROM " + TABLE_PREFIX + "server_status WHERE server_id = ?");
            ps.setString(1, serverID);
            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                return rs.getInt("max_player_num");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }


    //This filters the servers by player count
    public List<GameServer> findServersByPlayerCount(UUID joiner, String gameName)
    {
        List<GameServer> joinableServers = new ArrayList<>();
        PlayerParty playerParty = PartyManager.getInstance().getParty(joiner);
        int joinerSize = playerParty == null ? 1 : playerParty.getAllPlayers().size();

        Set<GameServer> availableServers = findServersByStatus(gameName);

        for(GameServer gameServer : availableServers)
        {
            int currentPlayers = playersOnline(gameServer.getServerID()) + playersInQueue(gameServer.getServerID()) + joinerSize;
            if(currentPlayers >= getMaxPlayerNum(gameServer.getServerID())) continue;

            joinableServers.add(gameServer);
        }

        return joinableServers;
    }


    public Boolean listenForAccpetedResponse(UUID joiner, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        Boolean response = null;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT scj.can_join FROM "
                    + TABLE_PREFIX + "player_info pi INNER JOIN "
                    + TABLE_PREFIX + "servers_can_join scj " +
                    "WHERE (pi.player_id = scj.player_id AND pi.uuid = ? AND scj.server_id = ?)");
            ps.setString(1, joiner.toString());
            ps.setString(2, serverID);

            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                response = rs.getBoolean("can_join");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if(response != null)
        {
            return response;
        }

        return false;
    }

    public String getRejectionReason(UUID joiner, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        String reason = null;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT scj.reason FROM "
                    + TABLE_PREFIX + "player_info pi INNER JOIN "
                    + TABLE_PREFIX + "servers_can_join scj " +
                    "WHERE (pi.player_id = scj.player_id AND pi.uuid = ? AND scj.server_id = ?)");
            ps.setString(1, joiner.toString());
            ps.setString(2, serverID);

            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                reason = rs.getString("reason");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return reason;
    }

    public boolean listenForConnectedResponse(UUID joiner, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        boolean response = false;

        try
        {
            PreparedStatement ps = con.prepareStatement("SELECT pq.is_connected FROM "
                    + TABLE_PREFIX + "player_info pi INNER JOIN "
                    + TABLE_PREFIX + "player_queues pq " +
                    "WHERE (pi.player_id = pq.player_id AND pq.server_id = ?)");

            ps.setString(1, serverID);

            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                response = rs.getBoolean("is_connected");
                ProxyServer.getInstance().getLogger().info(joiner + " connected to " + serverID + ": " + response);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return response;
    }
    public void updateQueuesInServers()
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement ps = null;
        try {
            for(GameServer server : GameServerManager.getInstance().getAllOnlineServers())
            {
                ps = con.prepareStatement("UPDATE " + TABLE_PREFIX + "queues_in_servers SET player_num = " +
                        "(SELECT COUNT(?) AS players_queued FROM " +
                        TABLE_PREFIX + "player_queues WHERE (cc_player_queues.server_id = ?)) " +
                        "WHERE (server_id = ?)");

                ps.setString(1, server.getServerID());
                ps.setString(2, server.getServerID());
                ps.setString(3, server.getServerID());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable()
    {
        //TODO: Reset all server status
        sqlConnection.disconnect();
    }
}
