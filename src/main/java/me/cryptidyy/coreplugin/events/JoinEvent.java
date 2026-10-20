package me.cryptidyy.coreplugin.events;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.OnlineServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManagerMySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinEvent implements Listener {

    private Main plugin;

    public JoinEvent(Main plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PostLoginEvent event)
    {
//        if(!dataManager.findPlayerData(event.getPlayer()).isPresent())
//            dataManager.addPlayer(event.getPlayer());

        if (Main.getInstance().isShuttingDown())
            return;
        if (event.getPlayer().isConnected())
            ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> sbLoggedIn(event));

        event.getPlayer().sendMessage(new TextComponent("Welcome to the Minigame Server!"));
    }

    private void sbLoggedIn(PostLoginEvent event)
    {
        OnlineServerPlayer player = ServerPlayerManager.getInstance().getPlayer(event.getPlayer());
        if (!player.doesExist()) {
            player.createEntry();
        }
        else
        {
            player.update();
        }

    }
}
