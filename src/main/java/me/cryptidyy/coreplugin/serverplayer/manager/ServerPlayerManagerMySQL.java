package me.cryptidyy.coreplugin.serverplayer.manager;

import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.connection.PlayerSQL;
import me.cryptidyy.coreplugin.serverplayer.data.PlayerDataSet;
import me.cryptidyy.coreplugin.serverplayer.sql.OnlineServerPlayerMySQL;
import me.cryptidyy.coreplugin.serverplayer.sql.ServerPlayerMySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.*;

public class ServerPlayerManagerMySQL extends ServerPlayerManager {

    private static PlayerSQL connection;
    public ServerPlayerManagerMySQL()
    {
        connection = new PlayerSQL();
    }
    //finds players from sql using player data
    public static PlayerSQL getConnection()
    {
        return connection;
    }

    @Override
    public ServerPlayer getPlayer(String playerName)
    {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        if (player == null)
            return getPlayerNotOnlineOnBungeeCord(getConnection().getPlayerData(playerName));
        else
            return getPlayer(player);
    }

    @Override
    public OnlineServerPlayer getPlayer(ProxiedPlayer player)
    {
        return new OnlineServerPlayerMySQL(getConnection().getPlayerID(player), player);
    }

    @Override
    public ServerPlayer getPlayer(UUID uuid)
    {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null)
            return getPlayer(player);
        return getPlayerNotOnlineOnBungeeCord(getConnection().getPlayerData(uuid));
    }

    @Override
    public ServerPlayer getPlayer(PlayerDataSet playerDataSet)
    {
        ProxiedPlayer player;
        if (playerDataSet.UUID != null)
            player = ProxyServer.getInstance().getPlayer(playerDataSet.UUID);
        else
            player = ProxyServer.getInstance().getPlayer(playerDataSet.NAME);
        if (player != null)
            return new OnlineServerPlayerMySQL(playerDataSet, player);
        return getPlayerNotOnlineOnBungeeCord(playerDataSet);
    }

    @Override
    public void joinGame(UUID uuid, String serverID)
    {
        getConnection().joinGame(uuid, serverID);
    }

    @Override
    public void removeLastGameServer(UUID uuid)
    {
        getConnection().removeLastGameServer(uuid);
    }

    @Override
    public void setLastGameServer(UUID uuid, String serverID)
    {
        getConnection().setLastGameServer(uuid ,serverID);
    }

    @Override
    public ServerInfo getLastGameServer(UUID uuid)
    {
        return getConnection().getLastGameServer(uuid);
    }

    @Override
    public void updatePlayerName(int id, String name)
    {
        getConnection().updatePlayerName(id, name);
    }

    @Override
    public UUID getUUID(int playerID) {
        return getConnection().getUUID(playerID);
    }

    @Override
    public void updateUUID(int playerID, UUID uuid) {
        getConnection().updateUUID(playerID, uuid);
    }

    @Override
    public void setOnline(int id, boolean isOnline) {
        getConnection().setOnline(id, isOnline);
    }

    @Override
    public void queuePlayer(UUID uuid, String serverID) {
        getConnection().queuePlayer(uuid, serverID);
    }

    @Override
    public void removePlayerFromQueue(UUID uuid, String serverID) {
        getConnection().removePlayerFromQueue(uuid, serverID);
    }

    @Override
    public boolean isInQueue(UUID uuid, String serverID) {
        return getConnection().isInQueue(uuid, serverID);
    }

    @Override
    public List<GameServer> getAllQueuedServers(UUID uuid) {
        return getConnection().getAllQueuedServers(uuid);
    }


    @Override
    public Set<OnlineServerPlayer> getAllOnlinePlayers()
    {
        return connection.getAllOnlinePlayers();
    }

    protected ServerPlayer getPlayerNotOnlineOnBungeeCord(PlayerDataSet pData) {
        return new ServerPlayerMySQL(pData);
    }
}
