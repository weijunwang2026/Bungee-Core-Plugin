package me.cryptidyy.coreplugin.gameserver.manager;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.data.DataManager;
import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.gameserver.GameServerMySQL;
import me.cryptidyy.coreplugin.gameserver.connection.ServerSQL;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameServerManagerMySQL extends GameServerManager {
    private static ServerSQL connection;

    private Map<UUID, List<String>> rejectedServers = new HashMap<>();

    private Map<UUID, GameServer> currentQueuedServers = new HashMap<>();

    public GameServerManagerMySQL()
    {
        try
        {
            connection = new ServerSQL();
        }
        catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    //find game servers from sql

    public static ServerSQL getConnection()
    {
        return connection;
    }

    @Override
    public Set<GameServer> findAvailableServers(String gameName)
    {
        return connection.findServersByStatus(gameName);
    }

    @Override
    public GameServer getGameServer(String serverID)
    {
        return connection.getGameServer(serverID);
    }

    //Put player in queue for each server one at a time (server_can_join, player_queues, queues_in_servers)
    //Wait for response (can_join != null)
    //If accepted (can_join == true), connect player
    //if successfully connected (is_connected == true), remove player from queue
    //Otherwise, still remove from queue but queue another server
    //This is ran repeatedly by the OnlineServerPlayer class
    @Override
    public GameServer findJoinableServer(String gameName, UUID uuid, long timeStarted)
    {
        GameServer joinableServer = null;
        long timePassedPerServer = System.currentTimeMillis() - timeStarted;
        if(rejectedServers.containsKey(uuid))
            timePassedPerServer -= 5000 * rejectedServers.get(uuid).size();

        List<GameServer> potentiallyJoinableServers = connection.findServersByPlayerCount(uuid, gameName);

        if(potentiallyJoinableServers.size() == 0)
        {
            //create a scheduler that fetches response and joins player
            //ProxyServer.getInstance().getPlayer(uuid).sendMessage("No server for you to join!");
            return null;
        }

        List<String> rejected = rejectedServers.containsKey(uuid) ? rejectedServers.get(uuid) : new ArrayList<>();

        //Filter servers by the most players already joined, and that it has not failed the queue
        Optional<GameServer> currentGameServerOptional = currentQueuedServers.containsKey(uuid) ? Optional.ofNullable(currentQueuedServers.get(uuid))
                : potentiallyJoinableServers.stream().filter(gameServer -> !rejected.contains(gameServer.getServerID()))
                .sorted((s1, s2) -> s2.getOnlineAndQueuedPlayerCount().compareTo(s1.getOnlineAndQueuedPlayerCount()))
                .findFirst();

        if(!currentGameServerOptional.isPresent()) return null;

        GameServer currentGameServer = currentGameServerOptional.get();

        //Queue the player for a server
        if(!currentQueuedServers.containsValue(currentGameServer))
            currentQueuedServers.put(uuid, currentGameServer);

        OnlineServerPlayer onlinePlayer = (OnlineServerPlayer) ServerPlayerManager.getInstance().getPlayer(uuid);
        if(!onlinePlayer.isInQueue(currentGameServer.getServerID()))
            onlinePlayer.queuePlayer(currentGameServer.getServerID());

        //check response
        Boolean canJoin = connection.listenForAccpetedResponse(uuid, currentGameServer.getServerID());

        //no response
        if(canJoin == null) return null;

        //Timed out after 4 seconds
        if(timePassedPerServer > 4000 && !canJoin)
        {
            String reason = connection.getRejectionReason(uuid, currentGameServer.getServerID()) == null ?
                    "TIMED OUT." : connection.getRejectionReason(uuid, currentGameServer.getServerID()) + ".";
            String rejectionMessage = ChatColor.RED + "FAILED " + currentGameServer.getServerID()
                    + ", REASON: " + reason;

            //add to rejected list, remove player from that queue
            onlinePlayer.getProxiedPlayer().sendMessage(rejectionMessage);

            rejected.add(currentGameServer.getServerID());
            rejectedServers.put(uuid, rejected);
            currentQueuedServers.remove(uuid, currentGameServer);
            onlinePlayer.removeFromQueue(currentGameServer.getServerID());
            return null;
        }

        //accepted
        if(canJoin)
        {
            //found server
            joinableServer = currentGameServer;
            currentQueuedServers.remove(uuid, currentGameServer);
            rejectedServers.remove(uuid);
        }

        return joinableServer;
    }

    @Override
    public boolean canJoinServerNow(UUID uuid, String serverID)
    {
        return connection.listenForAccpetedResponse(uuid, serverID);
    }

    @Override
    public void resetPlayerQueues(UUID uuid)
    {
        ServerPlayer sPlayer = ServerPlayerManager.getInstance().getPlayer(uuid);
        if(!sPlayer.isOnline()) return;

        OnlineServerPlayer onlinePlayer = (OnlineServerPlayer) sPlayer;
        onlinePlayer.getQueuedServers().forEach(queuedServer -> onlinePlayer.removeFromQueue(queuedServer.getServerID()));
        currentQueuedServers.remove(uuid);
        rejectedServers.remove(uuid);

    }

    @Override
    public void removeAllOtherQueues(UUID uuid, GameServer serverToKeep)
    {
        ServerPlayer sPlayer = ServerPlayerManager.getInstance().getPlayer(uuid);
        if(!sPlayer.isOnline()) return;

        OnlineServerPlayer onlinePlayer = (OnlineServerPlayer) sPlayer;

        onlinePlayer.getQueuedServers()
                .stream()
                .filter(queuedServer -> !queuedServer.getServerID().equals(serverToKeep.getServerID()))
                .forEach(queuedServer ->
                        onlinePlayer.removeFromQueue(queuedServer.getServerID()));

        currentQueuedServers.remove(uuid);
        rejectedServers.remove(uuid);
    }

    @Override
    public void registerGameServers(DataManager data) {
        Configuration serverTypeSection = data.getConfig().getSection("games");

        for(String gameType : serverTypeSection.getKeys())
        {
            Configuration serverPropertiesSection = serverTypeSection.getSection(gameType);

            for(String serverID : serverPropertiesSection.getStringList("servers"))
            {
                connection.registerGameServer(serverID, gameType);
            }
        }
    }

    @Override
    public Set<GameServer> getAllServers() {
        return null;
    }

    @Override
    public Set<GameServer> getAllOnlineServers() {
        return connection.getAllOnlineGameServers();
    }

    @Override
    public void updateQueueCountInServers()
    {
        connection.updateQueuesInServers();
    }
}
