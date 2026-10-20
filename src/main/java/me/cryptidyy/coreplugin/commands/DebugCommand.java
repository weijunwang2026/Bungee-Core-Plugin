package me.cryptidyy.coreplugin.commands;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.gameserver.GameServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Optional;

public class DebugCommand extends Command {

    private Main plugin;

    public DebugCommand(Main plugin)
    {
        super("serverdebug");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {

        if(args.length < 1)
        {
            sender.sendMessage("missing arguments!");
            return;
        }

        if(args[0].equals("findavailable"))
        {
            if(args.length < 2)
            {
                sender.sendMessage("Specify a game name!");
                return;
            }

            String gameName = args[1];
            plugin.getGameServerManager().findAvailableServers(gameName)
                    .stream().map(GameServer::getServerID).forEach(System.out::println);
        }

//        if(args.length < 1)
//        {
//            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /serverdebug [playerdata <playername>| serverinfo <servername>]"));
//            return;
//        }
//
//        if(args[0].equals("playerdata"))
//        {
//            if(args.length < 2)
//            {
//                sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /serverdebug playerdata <playername>"));
//                return;
//            }
//
//            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
//            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
//
//            Optional<PlayerData> playerDataOptional = playerDataManager.findPlayerData(target);
//
//            if(!playerDataOptional.isPresent())
//            {
//                sender.sendMessage(new TextComponent(ChatColor.RED + "Cannot find data for this player!"));
//                return;
//            }
//
//            PlayerData playerData = playerDataOptional.get();
//
//            if(args.length > 2)
//            {
//                if(args[2].equals("reset"))
//                {
//                    playerData.setLastGame(null);
//                    playerData.setInGame(false);
//                    sender.sendMessage(new TextComponent("PlayerData was reset for this player!"));
//                    return;
//                }
//            }
//
//            //String lastGame = "Last Game: " + (playerData.getLastGame() != null ? playerData.getLastGame().getGameName() : "null") + " ";
//            //String lastServer = "Last Server: " + (playerData.getLastGame() != null ? playerData.getLastGame().getPlayerLastServer(target).getName() : "null") + " ";
//            //String isInGame = "Is in game: " + playerData.isInGame() + " ";
//
//            sender.sendMessage(new TextComponent(lastGame));
//            sender.sendMessage(new TextComponent(lastServer));
//            sender.sendMessage(new TextComponent(isInGame));
//
//        }
//        else if(args[0].equals("serverinfo"))
//        {
//            if(args.length < 3)
//            {
//                sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /serverdebug serverinfo <gamename> <servername>"));
//                return;
//            }
//
//            String gameName = args[1];
//            String serverName = args[2];
//
//            NetworkServerManager networkServerManager = plugin.getNetworkServerManager();
//            Optional<GameServer> gameServerOptional = networkServerManager.findGameServer(gameName);
//
//            if(!gameServerOptional.isPresent())
//            {
//                sender.sendMessage(new TextComponent(ChatColor.RED + "Cannot find this game!"));
//                return;
//            }
//
//            GameServer gameServer = gameServerOptional.get();
//
//            //String playerCount = "Player count: " + gameServer.getServerPlayerCounts().get(serverName);
//            //String canJoin = "Can join: " + gameServer.getServerJoinableStates().get(serverName);
//            String isStarted = "Is game started: " + ServerSQL.isGameStarted(serverName);
//
//            //sender.sendMessage(new TextComponent(playerCount));
//            //sender.sendMessage(new TextComponent(canJoin));
//            sender.sendMessage(new TextComponent(isStarted));
//
//        }
    }
}
