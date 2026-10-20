package me.cryptidyy.coreplugin.serverplayer.connection;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.data.SQLConnection;
import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManager;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.data.PlayerDataSet;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import me.cryptidyy.coreplugin.spigotcommands.CommandSQL;
import me.cryptidyy.coreplugin.utils.Deactivated;
import me.cryptidyy.coreplugin.utils.Disabler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerSQL implements Deactivated {

    //gets and sets player data on sql

    private final String TABLE_PREFIX = "cc_";
    private SQLConnection sqlConnection = Main.getInstance().sqlConnection;

    private ScheduledTask checkConnectionTask;

    private CommandSQL cmdSQL = new CommandSQL();

    public PlayerSQL()
    {
        //create table if not exists
        importDatabase();
        Disabler.getInstance().registerDeactivated(this);
    }

    //create if not exists
    private void importDatabase()
    {
        Connection con = sqlConnection.getConnection();

        PreparedStatement prepStmt = null;
        try
        {
            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "player_info` ("
                    + "`player_id` INT(8) NOT NULL AUTO_INCREMENT, "
                    + "`uuid` CHAR(38) NOT NULL, "
                    + "`name` VARCHAR(16) NOT NULL, "
                    + "is_online TINYINT(1), "
                    + "last_server_id VARCHAR(7), "
                    + "current_server_id VARCHAR(7), "
                    + "PRIMARY KEY (`player_id`));");
            prepStmt.executeUpdate();
            //prepStmt.close();

            prepStmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_PREFIX + "player_queues` ("
                    + "`player_id` INT(8) NOT NULL AUTO_INCREMENT, "
                    + "server_id VARCHAR(7), "
                    + "is_connected TINYINT(1), "
                    + "PRIMARY KEY (`player_id`));");
            prepStmt.executeUpdate();
            //prepStmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public PlayerDataSet getPlayerDataSet(UUID uuid) {
        return null;
    }

    public int firstJoin(ProxiedPlayer pPlayer)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        int id = 0;

        try
        {
            prepStmt = con.prepareStatement(
                    "insert into " + TABLE_PREFIX + "player_info (uuid, name, is_online) values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            prepStmt.setString(1, pPlayer.getUniqueId().toString());
            prepStmt.setString(2, pPlayer.getName());
            prepStmt.setBoolean(3, true);
            prepStmt.executeUpdate();
            ResultSet rs = prepStmt.getGeneratedKeys();
            if (rs.next())
            {
                id = rs.getInt(1);
                //rs.close();
                close(con, prepStmt);
            }
            //else
                //rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            close(con, prepStmt);
        }
        return id;
    }

    public void updatePlayerName(int id, String newName)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        try
        {
            prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "player_info SET name=? WHERE player_id='" + id + "' LIMIT 1");
            prepStmt.setString(1, newName);
            prepStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            close(con, prepStmt);
        }
    }

    public void updateUUID(int id, UUID newUUID)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = con.prepareStatement("UPDATE " + TABLE_PREFIX + "player_info SET uuid='"
                    + newUUID + "' WHERE player_id='" + id + "' LIMIT 1");
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(con, prepStmt);
        }
    }

    public UUID getUUID(int playerID)
    {
        UUID uuid;
        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            rs = (stmt = con.createStatement()).executeQuery("SELECT uuid, name FROM " + TABLE_PREFIX
                    + "player_info WHERE player_id='" + playerID + "' LIMIT 1");
            if (rs.next())
            {
                uuid = UUID.fromString(rs.getString("uuid"));
                return uuid;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            close(con, rs, stmt);
        }
        return null;
    }

    public String getName(int playerID)
    {
        String playerName;
        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            rs = (stmt = con.createStatement()).executeQuery("SELECT name, uuid FROM " + TABLE_PREFIX
                    + "player_info WHERE player_id='" + playerID + "' LIMIT 1");
            if (rs.next())
            {
                playerName = rs.getString("name");
                return playerName;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            close(con, rs, stmt);
        }
        return "";
    }

    public ServerInfo getLastGameServer(UUID uuid)
    {
        ServerInfo result = null;
        Connection con = sqlConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT last_server_id FROM " + TABLE_PREFIX + "player_info WHERE (player_id = ?)");
            ps.setInt(1, getPlayerData(uuid).ID);

            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                result = ProxyServer.getInstance().getServerInfo(rs.getString("last_server_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public ServerInfo getCurrentGameServer(int playerID)
    {
        return null;
    }

    public int getPlayerID(String pPlayerName) {
        return getPlayerData(pPlayerName).ID;
    }

    public int getPlayerID(ProxiedPlayer pPlayer)
    {
        return getPlayerID(pPlayer.getName());
    }

    public PlayerDataSet getPlayerData(String pPlayerName) {
        Integer playerID;
        Connection con = sqlConnection.getConnection();
        ResultSet rs = null;
        PreparedStatement prepStmt = null;

        try {
            prepStmt = con.prepareStatement("select player_id, uuid from " + TABLE_PREFIX
                    + "player_info WHERE name=? LIMIT 1");
            prepStmt.setString(1, pPlayerName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                playerID = rs.getInt("player_id");
                return new PlayerDataSet(pPlayerName, playerID, uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(con, rs, prepStmt);
        }
        return new PlayerDataSet(pPlayerName, -1, null);
    }

    public PlayerDataSet getPlayerData(UUID pUUID) {
        Integer playerID;
        Connection con = sqlConnection.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            rs = (stmt = con.createStatement()).executeQuery("select player_id, name from " + TABLE_PREFIX
                    + "player_info WHERE uuid='" + pUUID + "' LIMIT 1");
            if (rs.next()) {
                playerID = rs.getInt("player_id");
                String playerName = rs.getString("name");
                return new PlayerDataSet(playerName, playerID, pUUID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(con, rs, stmt);
        }
        return new PlayerDataSet(null, -1, pUUID);
    }

    public void setOnline(int id, boolean isOnline)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = con.prepareStatement(
                    "UPDATE " + TABLE_PREFIX + "player_info SET is_online=? WHERE player_id='" + id + "' LIMIT 1");
            prepStmt.setBoolean(1, isOnline);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(con, prepStmt);
        }
    }

    private void updateQueuesInServers(UUID joinerID, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("UPDATE " + TABLE_PREFIX + "queues_in_servers SET player_num = " +
                    "(SELECT COUNT(?) AS players_queued FROM " +
                    TABLE_PREFIX + "player_queues WHERE (cc_player_queues.server_id = ?)) " +
                    "WHERE (server_id = ?)");

            ps.setString(1, serverID);
            ps.setString(2, serverID);
            ps.setString(3, serverID);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<OnlineServerPlayer> getAllOnlinePlayers()
    {
        Set<OnlineServerPlayer> result = new HashSet<>();
        Connection con = sqlConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + TABLE_PREFIX + "player_info");
            ResultSet rs = ps.executeQuery();

            while(rs.next())
            {
                ServerPlayer sPlayer= ServerPlayerManager.getInstance().getPlayer(rs.getString("uuid"));
                if(!sPlayer.isOnline()) continue;

                result.add((OnlineServerPlayer) sPlayer);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public List<GameServer> getAllQueuedServers(UUID uuid)
    {
        List<GameServer> result = new ArrayList<>();
        Connection con = sqlConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT server_id FROM " + TABLE_PREFIX + "player_queues WHERE (player_id = ?)");
            ps.setInt(1, getPlayerData(uuid).ID);

            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                GameServer server = GameServerManager.getInstance().getGameServer(rs.getString("server_id"));

                result.add(server);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    public void queuePlayer(UUID joinerID, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO " + TABLE_PREFIX + "player_queues (`player_id`, `server_id`, `is_connected`) VALUES (?, ?, ?)");
            ps.setInt(1, getPlayerData(joinerID).ID);
            ps.setString(2, serverID);
            ps.setBoolean(3, false);

            ps.executeUpdate();

            ps = con.prepareStatement("INSERT INTO " + TABLE_PREFIX + "servers_can_join (`player_id`, `server_id`) VALUES (?, ?)");
            ps.setInt(1, getPlayerData(joinerID).ID);
            ps.setString(2, serverID);

            ps.executeUpdate();

            updateQueuesInServers(joinerID, serverID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayerFromQueue(UUID joinerID, String serverID)
    {
        Connection con = sqlConnection.getConnection();
        PlayerDataSet playerData = getPlayerData(joinerID);
        if(playerData.ID == -1)
        {
            ProxyServer.getInstance().getLogger().info("Cannot get player ID for " + joinerID);
        }

        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM " + TABLE_PREFIX + "player_queues WHERE (player_id = ? AND server_id = ?)");
            ps.setInt(1, getPlayerData(joinerID).ID);
            ps.setString(2, serverID);

            ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM " + TABLE_PREFIX + "servers_can_join WHERE (player_id = ? AND server_id = ?)");
            ps.setInt(1, getPlayerData(joinerID).ID);
            ps.setString(2, serverID);

            ps.executeUpdate();

            updateQueuesInServers(joinerID, serverID);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isInQueue(UUID uuid, String serverID)
    {
        Connection con = sqlConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT EXISTS(SELECT * FROM " + TABLE_PREFIX + "player_queues WHERE (player_id = ? AND server_id = ?))");
            ps.setInt(1, getPlayerData(uuid).ID);
            ps.setString(2, serverID);

            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return rs.getBoolean(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public void joinGame(UUID joinerID, String serverID)
    {
        final boolean[] isConnectionSuccessful = {false};
        GameServer serverToJoin = GameServerManager.getInstance().getGameServer(serverID);
        if(serverToJoin == null) return;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(joinerID);
        player.connect(serverToJoin.getServerInfo());

        //Check if player is connected, don't remove queue until player is (repeated)
        long timeStarted = System.currentTimeMillis();

        checkConnectionTask = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
            //ProxyServer.getInstance().getLogger().info(serverToJoin.isPlayerConnected(joinerID) + "");

            //Player has successfully connected to the game server, end of queueing
            if(serverToJoin.isPlayerConnected(joinerID))
            {
                isConnectionSuccessful[0] = true;
                removePlayerFromQueue(joinerID, serverID);
                cmdSQL.removeCommand(player.getUniqueId());
                checkConnectionTask.cancel();

                ProxyServer.getInstance().getLogger().info("Detected that player has connected to game server, ending the checkConnectionTask");
            }

            //Connection timed out, end of queueing
            else if(!isConnectionSuccessful[0] && System.currentTimeMillis() - timeStarted > 8000)
            {
                player.sendMessages(ChatColor.RED + "Connection to " + serverToJoin.getServerID() + " is taking longer than expected...");
                removePlayerFromQueue(joinerID, serverID);
                cmdSQL.removeCommand(player.getUniqueId());
                checkConnectionTask.cancel();

            }
        }, 3000, 200, TimeUnit.MILLISECONDS);

    }

    public void setLastGameServer(UUID uuid, String name)
    {
        Connection con = sqlConnection.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("UPDATE `cc_player_info` SET `last_server_id` = ? WHERE (`player_id` = ?)");
            ps.setString(1, name);
            ps.setInt(2, getPlayerData(uuid).ID);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //start a timer that will remove the server after x seconds
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {

            removeLastGameServer(uuid);

        }, 120, TimeUnit.SECONDS);

    }

    public void removeLastGameServer(UUID uuid)
    {
        Connection con = sqlConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE `cc_player_info` SET `last_server_id` = NULL WHERE (`player_id` = ?)");
            ps.setInt(1, getPlayerData(uuid).ID);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCurrentGameServer(int playerID, String name)
    {

    }

    protected void close(Connection con, ResultSet rs, Statement stmt) {
//        try {
//            if (con != null)
//                con.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        close(rs, stmt);
    }

    protected void close(Connection con, ResultSet rs, Statement stmt, PreparedStatement prepStmt) {
//        try {
//            if (con != null)
//                con.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        close(rs, stmt, prepStmt);
    }

    protected void close(Connection con, PreparedStatement pPrepStmt) {
//        try {
//            if (con != null)
//                con.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        close(pPrepStmt);
    }

    protected void close(PreparedStatement pPrepStmt) {
//        try {
//            if (pPrepStmt != null)
//                pPrepStmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    protected void close(ResultSet rs, Statement stmt) {
//        try {
//            if (rs != null)
//                rs.close();
//            if (stmt != null)
//                stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    protected void close(ResultSet rs, Statement stmt, PreparedStatement prepStmt) {
//        try {
//            if (rs != null)
//                rs.close();
//            if (stmt != null)
//                stmt.close();
//            if (prepStmt != null)
//                prepStmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onDisable()
    {
//        getAllOnlinePlayers().stream().forEach(player -> {
//            removeLastGameServer(player.getUUID());
//        });

        //Remove last server
        sqlConnection.disconnect();
    }
}
