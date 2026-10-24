package me.cryptidyy.coreplugin.serverplayer.sql;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.gameserver.GameServer;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManager;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManagerMySQL;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.data.PlayerDataSet;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManagerMySQL;
import me.cryptidyy.coreplugin.spigotcommands.CommandSQL;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OnlineServerPlayerMySQL extends ServerPlayerMySQL implements OnlineServerPlayer {
    private final ProxiedPlayer PLAYER;
    private ScheduledTask joinTask;
    private GameServerManager serverManager = new GameServerManagerMySQL();
    private ServerPlayerManager playerManager = new ServerPlayerManagerMySQL();

    private CommandSQL cmdSQL = new CommandSQL();
    //constructors will now contain ProxiedPlayer because player is online
    public OnlineServerPlayerMySQL(int pID, ProxiedPlayer player)
    {
        super(pID);
        PLAYER = player;
    }

    public OnlineServerPlayerMySQL(PlayerDataSet pData, ProxiedPlayer player)
    {
        super(pData);
        PLAYER = player;
    }

    @Override
    public void createEntry()
    {
        id = ServerPlayerManagerMySQL.getConnection().firstJoin(PLAYER);
    }

    @Override
    public ProxiedPlayer getProxiedPlayer() {
        return PLAYER;
    }

    @Override
    public void connect(ServerInfo info) {

    }

    @Override
    public ServerInfo getServer() {
        return null;
    }

    @Override
    public void joinGame(String gameName)
    {
        serverManager.resetPlayerQueues(PLAYER.getUniqueId());
        long timeStarted = System.currentTimeMillis();
        PLAYER.sendMessage(ChatColor.GREEN + "Connecting to " + gameName + "...");

        //repeatedly find an available server
        joinTask = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
            GameServer joinableServer = serverManager.findJoinableServer(gameName, PLAYER.getUniqueId(), timeStarted);

            if(joinableServer != null && !PLAYER.getServer().getInfo().getName().equals(joinableServer.getServerInfo().getName()))
            {
                playerManager.joinGame(PLAYER.getUniqueId(), joinableServer.getServerID());
                playerManager.removeLastGameServer(PLAYER.getUniqueId());
                serverManager.removeAllOtherQueues(PLAYER.getUniqueId(), joinableServer);
                joinTask.cancel();
                return;
            }

            //timeout if exceeds 15 seconds, end of queueing
            else if(System.currentTimeMillis() - timeStarted > 15000)
            {
                PLAYER.sendMessage(ChatColor.RED + "Timed out while joining " + gameName + ". This usually means that all game servers are full, please try again later.");

                serverManager.resetPlayerQueues(PLAYER.getUniqueId());
                cmdSQL.removeCommand(PLAYER.getUniqueId());
                joinTask.cancel();
            }
        },0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void quitGame()
    {
        Server currentServer = PLAYER.getServer();
        if(serverManager.getAllOnlineServers()
                .stream()
                .allMatch(gameServer -> !gameServer.getServerID().equals(currentServer.getInfo().getName())))
        {
            PLAYER.sendMessage("You are not in a game!");
            return;
        }

        playerManager.setLastGameServer(PLAYER.getUniqueId(), currentServer.getInfo().getName());
        //this.setLastGameServer(currentServer.getInfo().getName());

        PLAYER.connect(ProxyServer.getInstance().getServerInfo("lobby"));
    }

    @Override
    public void rejoinLastGame()
    {
        Long rejoinStartTime = System.currentTimeMillis();
        ServerInfo lastGameServer = playerManager.getLastGameServer(PLAYER.getUniqueId());

        if(lastGameServer == null)
        {
            PLAYER.sendMessage(ChatColor.RED + "No server for you to rejoin!");
            return;
        }

        queuePlayer(lastGameServer.getName());

        joinTask = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {

            if(serverManager.canJoinServerNow(PLAYER.getUniqueId(), lastGameServer.getName()))
            {
                //joinGame removes player from queue
                playerManager.joinGame(PLAYER.getUniqueId(), lastGameServer.getName());
                playerManager.removeLastGameServer(PLAYER.getUniqueId());
                joinTask.cancel();
            }
            else if(System.currentTimeMillis() - rejoinStartTime > 8000)
            {
                PLAYER.sendMessage(ChatColor.RED + "Rejoin timed out, please try again.");
                removeFromQueue(lastGameServer.getName());
                joinTask.cancel();
            }

        },0, 500, TimeUnit.MILLISECONDS);
    }


    @Override
    public boolean isOnline()
    {
        return true;
    }

    @Override
    public void update() {
        if (ProxyServer.getInstance().getConfig().isOnlineMode()) {
            playerManager.updatePlayerName(getPlayerID(), PLAYER.getName());
        } else if (!PLAYER.getUniqueId().equals(playerManager.getUUID(getPlayerID())))
            playerManager.updateUUID(getPlayerID(), PLAYER.getUniqueId());

        playerManager.setOnline(id, true);
    }

    @Override
    public void disconnect() {
        serverManager.resetPlayerQueues(PLAYER.getUniqueId());
        quitGame();
        playerManager.setOnline(id, false);
        //set current game to null
        //update last game
    }

    @Override
    public void queuePlayer(String serverID)
    {
        playerManager.queuePlayer(PLAYER.getUniqueId(), serverID);

    }

    @Override
    public void removeFromQueue(String serverID) {
        if(!isInQueue(serverID)) return;

        playerManager.removePlayerFromQueue(PLAYER.getUniqueId(), serverID);

    }

    @Override
    public boolean isInQueue(String serverID) {
        return playerManager.isInQueue(PLAYER.getUniqueId(), serverID);
    }

    @Override
    public List<GameServer> getQueuedServers() {
        return playerManager.getAllQueuedServers(PLAYER.getUniqueId());
    }
}
