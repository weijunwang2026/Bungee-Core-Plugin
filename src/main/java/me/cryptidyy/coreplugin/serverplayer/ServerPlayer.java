package me.cryptidyy.coreplugin.serverplayer;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public interface ServerPlayer {

    UUID getUUID();

    boolean isOnline();
    //Safe to cast to OnlineServerPlayer

    String getName();

    ServerInfo getLastGameServer();

    void setLastGameServer(String serverID);

    ServerInfo getCurrentGameServer();

    void scheduleLastGameRemoval();

    boolean doesExist();

}
