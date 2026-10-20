package me.cryptidyy.coreplugin.serverplayer.data;

import java.util.UUID;

public class PlayerDataSet {

    public final String NAME;
    public final int ID;
    public final UUID UUID;

    public PlayerDataSet(String name, int id, UUID uuid) {
        NAME = name;
        ID = id;
        UUID = uuid;
    }
}
