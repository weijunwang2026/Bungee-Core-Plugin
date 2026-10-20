package me.cryptidyy.coreplugin.events;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManagerMySQL;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;

public class LeaveEvent implements Listener {

    private Main plugin;

    public LeaveEvent(Main plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        ServerPlayerManager.getInstance().getPlayer(player).disconnect();
        //dataManager.findPlayerData(player).get().setInGame(false);
    }
}
