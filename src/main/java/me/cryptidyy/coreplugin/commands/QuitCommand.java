package me.cryptidyy.coreplugin.commands;

import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class QuitCommand extends Command {

    private Main plugin;

    public QuitCommand(Main plugin)
    {
        super("quit");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) sender;

        ServerPlayerManager.getInstance().getPlayer(player).quitGame();
    }
}
