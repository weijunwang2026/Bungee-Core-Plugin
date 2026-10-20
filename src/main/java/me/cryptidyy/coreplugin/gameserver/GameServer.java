package me.cryptidyy.coreplugin.gameserver;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public interface GameServer {

    //server status
    String getGameName();
    String getServerID();
    boolean isStarted();
    boolean isWaiting();
    int getOnlinePlayers();

    //server canjoin
    boolean canJoin(UUID uuid);

    boolean isPlayerConnected(UUID uuid);

    //queues_in_servers
    int getQueuedPlayers();

    Integer getOnlineAndQueuedPlayerCount();

    ServerInfo getServerInfo();
}
