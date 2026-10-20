package me.cryptidyy.coreplugin.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChatUtil {

    public static void sendMessage(ProxiedPlayer player, String message)
    {
        player.sendMessage(new TextComponent(format(message)));
    }

    public static String format(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
