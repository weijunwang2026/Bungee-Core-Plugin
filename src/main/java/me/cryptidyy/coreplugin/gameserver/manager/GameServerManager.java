package me.cryptidyy.coreplugin.gameserver.manager;

import me.cryptidyy.coreplugin.data.DataManager;
import me.cryptidyy.coreplugin.gameserver.GameServer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class GameServerManager {

    private static GameServerManager instance;

    public GameServerManager()
    {
        instance = this;
    }

    public static GameServerManager getInstance()
    {
        return instance;
    }

    public abstract Set<GameServer> findAvailableServers(String gameName);

    public abstract GameServer getGameServer(String serverID);

    public abstract GameServer findJoinableServer(String gameName, UUID uuid, long timeStarted);

    public abstract boolean canJoinServerNow(UUID uuid, String serverID);

    public abstract void resetPlayerQueues(UUID uuid);

    public abstract void removeAllOtherQueues(UUID uuid, GameServer serverToKeep);

    public abstract void registerGameServers(DataManager data);

    public abstract Set<GameServer> getAllServers();

    public abstract Set<GameServer> getAllOnlineServers();

    public abstract void updateQueueCountInServers();

}
