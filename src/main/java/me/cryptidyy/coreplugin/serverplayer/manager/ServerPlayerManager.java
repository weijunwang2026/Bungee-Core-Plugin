package me.cryptidyy.coreplugin.serverplayer.manager;

import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.data.PlayerDataSet;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class ServerPlayerManager {

    private static ServerPlayerManager instance;

    public ServerPlayerManager() {
        instance = this;
    }

    public static ServerPlayerManager getInstance() {
        return instance;
    }

    /**
     * @param pPlayer The name of the player
     * @return Returns either a {@link ServerPlayer} if the player is offline/does not exist
     * (check with {@link ServerPlayer#doesExist()}) or an {@link OnlineServerPlayer} if the player is online
     */
    public abstract ServerPlayer getPlayer(String pPlayer);

    public abstract OnlineServerPlayer getPlayer(ProxiedPlayer pPlayer);

    /**
     * @param pPlayer The UUID of the player
     * @return Returns either a {@link ServerPlayer} if the player is offline/does not exist
     * (check with {@link ServerPlayer#doesExist()}) or an {@link OnlineServerPlayer} if the player is online
     */
    public abstract ServerPlayer getPlayer(UUID pPlayer);

    /**
     * @param playerDataSet The {@link PlayerDataSet}. Please make sure that the data set is complete
     * @return The {@link ServerPlayer} linked to this dataset
     */
    public abstract ServerPlayer getPlayer(PlayerDataSet playerDataSet);

    public abstract void joinGame(UUID uuid, String serverID);

    public abstract void removeLastGameServer(UUID uuid);

    public abstract void setLastGameServer(UUID uuid, String serverID);

    public abstract ServerInfo getLastGameServer(UUID uuid);

    public abstract void updatePlayerName(int id, String name);

    public abstract UUID getUUID(int playerID);

    public abstract void updateUUID(int playerID, UUID uuid);

    public abstract void setOnline(int id, boolean isOnline);

    public abstract void queuePlayer(UUID uuid, String serverID);

    public abstract void removePlayerFromQueue(UUID uuid, String serverID);

    public abstract boolean isInQueue(UUID uuid, String serverID);

    public abstract List<GameServer> getAllQueuedServers(UUID uuid);

    public abstract Set<OnlineServerPlayer> getAllOnlinePlayers();
}
