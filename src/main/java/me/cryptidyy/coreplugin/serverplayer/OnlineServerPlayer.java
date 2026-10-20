package me.cryptidyy.coreplugin.serverplayer;

import me.cryptidyy.coreplugin.gameserver.GameServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Set;

public interface OnlineServerPlayer extends ServerPlayer {

    void createEntry();

    ProxiedPlayer getProxiedPlayer();

    void connect(ServerInfo info);

    ServerInfo getServer();

    void joinGame(String gameName);

    void quitGame();

    void rejoinLastGame();

    void queuePlayer(String serverID);

    void removeFromQueue(String serverID);

    boolean isInQueue(String serverID);

    List<GameServer> getQueuedServers();

    boolean isOnline();

    //update player if name or uuid changed, set online
    void update();

    void disconnect();
}
