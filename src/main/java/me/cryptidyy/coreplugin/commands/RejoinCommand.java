package me.cryptidyy.coreplugin.commands;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RejoinCommand extends Command {

    private Main plugin;

    public RejoinCommand(Main plugin) {
        super("rejoin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(!(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerPlayerManager.getInstance().getPlayer(player).rejoinLastGame();

    }
}
