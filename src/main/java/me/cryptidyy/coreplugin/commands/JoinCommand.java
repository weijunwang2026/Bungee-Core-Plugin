package me.cryptidyy.coreplugin.commands;

import de.simonsator.partyandfriends.api.party.PartyManager;
import me.cryptidyy.coreplugin.Main;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class JoinCommand extends Command {

    private Main plugin;
    public JoinCommand(Main plugin)
    {
        super("join");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(!(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if(PartyManager.getInstance().getParty(player.getUniqueId()) != null
                && !PartyManager.getInstance().getParty(player.getUniqueId()).getLeader().getUniqueId().equals(player.getUniqueId()))
        {
            sender.sendMessage(ChatColor.RED + "You are not the leader of your party!");
            return;
        }

        if(args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "Please specify a game!");
            return;
        }

        String gameName = args[0];
        ServerPlayerManager.getInstance().getPlayer(player).joinGame(gameName);
    }
}
