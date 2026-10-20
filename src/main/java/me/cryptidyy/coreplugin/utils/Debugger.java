package me.cryptidyy.coreplugin.utils;

import net.md_5.bungee.api.ProxyServer;

import java.util.logging.Level;

public class Debugger {

    private static final boolean isDebugMode = true;

    public static void sendDebugMessage(String message)
    {
        if(!isDebugMode) return;
        ProxyServer.getInstance().getLogger().log(Level.SEVERE, message);
    }
}
