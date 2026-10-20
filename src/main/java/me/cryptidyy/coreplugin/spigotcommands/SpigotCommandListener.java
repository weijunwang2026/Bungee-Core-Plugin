package me.cryptidyy.coreplugin.spigotcommands;

import de.simonsator.partyandfriends.api.party.PartyManager;
import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpigotCommandListener {

    private Main plugin;
    private CommandSQL sql;

    private ScheduledTask task = null;

    //private boolean isListening = false;
    public SpigotCommandListener(Main plugin)
    {
        this.plugin = plugin;
        this.sql = new CommandSQL();
    }

    public void listenForCommands()
    {
        //isListening = true;
        task = plugin.getProxy().getScheduler().schedule(plugin, () -> {

            //if(!isListening) return;

            sql.getCommands().forEach((uuid, cmd) -> {
                if(!sql.isExecuting(uuid))
                {
                    executeCommand(uuid, cmd);
                    sql.setExecuting(uuid, true);
                }
            });

        },0, 500, TimeUnit.MILLISECONDS);
    }

    public void cancelListening()
    {
        //isListening = false;
    }

    public void onDisable()
    {
        if(task != null) task.cancel();
    }
    private void executeCommand(UUID uuid, String cmd)
    {
        if(plugin.getProxy().getPlayer(uuid) == null) return;
        ProxiedPlayer sender = plugin.getProxy().getPlayer(uuid);

        //Same behavior as the Bungee command /join oceanraiders
        if(cmd.equalsIgnoreCase("join oceanraiders"))
        {
            if(PartyManager.getInstance().getParty(uuid) != null
                    && !PartyManager.getInstance().getParty(uuid).getLeader().getUniqueId().equals(uuid))
            {
                sender.sendMessage(ChatColor.RED + "You are not the leader of your party!");
                sql.removeCommand(uuid);
                return;
            }

            ServerPlayerManager.getInstance().getPlayer(sender).joinGame("oceanraiders");
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "Invalid command! Try /spigotcmd join <gamename>");
            sql.removeCommand(uuid);
        }


    }
}
