package me.cryptidyy.coreplugin.gameserver;

import me.cryptidyy.coreplugin.gameserver.manager.GameServerManagerMySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class GameServerMySQL implements GameServer {

    private String serverID;
    private String gameName;

    public GameServerMySQL(String serverID, String gameName)
    {
        this.serverID = serverID;
        this.gameName = gameName;
    }

    @Override
    public String getGameName() {
        return gameName;
    }

    @Override
    public String getServerID() {
        return serverID;
    }

    @Override
    public boolean isStarted() {
        return GameServerManagerMySQL.getConnection().isStarted(serverID);
    }

    @Override
    public boolean isWaiting() {
        return GameServerManagerMySQL.getConnection().isWaiting(serverID);
    }

    @Override
    public int getOnlinePlayers() {
        return GameServerManagerMySQL.getConnection().playersOnline(serverID);
    }

    @Override
    public boolean canJoin(UUID uuid) {
        return false;
    }

    @Override
    public boolean isPlayerConnected(UUID uuid) {
        return GameServerManagerMySQL.getConnection().listenForConnectedResponse(uuid, serverID);
    }

    @Override
    public int getQueuedPlayers() {
        return GameServerManagerMySQL.getConnection().playersInQueue(serverID);
    }

    @Override
    public Integer getOnlineAndQueuedPlayerCount() {
        return getOnlinePlayers() + getQueuedPlayers();
    }

    @Override
    public ServerInfo getServerInfo() {
        return ProxyServer.getInstance().getServerInfo(serverID);
    }
}
