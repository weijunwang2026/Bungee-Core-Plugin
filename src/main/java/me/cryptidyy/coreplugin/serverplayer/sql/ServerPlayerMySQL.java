package me.cryptidyy.coreplugin.serverplayer.sql;

import me.cryptidyy.coreplugin.serverplayer.ServerPlayer;
import me.cryptidyy.coreplugin.serverplayer.data.PlayerDataSet;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManagerMySQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class ServerPlayerMySQL implements ServerPlayer {

    //represents a player from the sql

    protected int id;
    private String name;
    private UUID uuid;
    private ServerInfo lastGameServer;
    private ServerInfo currentGameServer;

    public ServerPlayerMySQL(int pID) {
        id = pID;
    }

    public ServerPlayerMySQL(PlayerDataSet pData)
    {
        id = pData.ID;
        name = pData.NAME;
        uuid = pData.UUID;
    }

    @Override
    public UUID getUUID()
    {
        if (uuid != null)
            return uuid;
        return uuid = ServerPlayerManagerMySQL.getConnection().getUUID(id);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName()
    {
        if (name != null)
            return name;
		return name = ServerPlayerManagerMySQL.getConnection().getName(id);
    }

    @Override
    public ServerInfo getLastGameServer()
    {
        return lastGameServer = ServerPlayerManagerMySQL.getConnection().getLastGameServer(uuid);
    }

    @Override
    public void setLastGameServer(String serverID)
    {
        ServerPlayerManagerMySQL.getConnection().setLastGameServer(uuid, serverID);
    }

    @Override
    public ServerInfo getCurrentGameServer()
    {
        return currentGameServer = ServerPlayerManagerMySQL.getConnection().getCurrentGameServer(id);
    }

    @Override
    public void scheduleLastGameRemoval() {

    }

    @Override
    public boolean doesExist() {
        return id > 0;
    }

    public int getPlayerID()
    {
        return id;
    }
}
