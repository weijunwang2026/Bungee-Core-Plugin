package me.cryptidyy.coreplugin;

import me.cryptidyy.coreplugin.commands.JoinCommand;
import me.cryptidyy.coreplugin.commands.QuitCommand;
import me.cryptidyy.coreplugin.commands.RejoinCommand;
import me.cryptidyy.coreplugin.data.MySQL;
import me.cryptidyy.coreplugin.data.SQLConnection;
import me.cryptidyy.coreplugin.data.SQLHelper;
import me.cryptidyy.coreplugin.commands.DebugCommand;
import me.cryptidyy.coreplugin.data.DataManager;
import me.cryptidyy.coreplugin.events.JoinEvent;
import me.cryptidyy.coreplugin.events.LeaveEvent;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManager;
import me.cryptidyy.coreplugin.gameserver.manager.GameServerManagerMySQL;
import me.cryptidyy.coreplugin.serverplayer.manager.ServerPlayerManagerMySQL;
import me.cryptidyy.coreplugin.spigotcommands.SpigotCommandListener;
import me.cryptidyy.coreplugin.utils.Disabler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;

public class Main extends Plugin {

    private static Main instance;

    private GameServerManagerMySQL gameServerManagerMySQL;

    public SQLConnection sqlConnection;

    public SpigotCommandListener spigotCmdListener;

    private SQLHelper sqlHelper;

    private boolean isShuttingDown = false;

    public Main()
    {
        instance = this;
    }

    @Override
    public void onEnable()
    {
        isShuttingDown = false;
        this.sqlConnection = new SQLConnection();

//        this.SQL = new MySQL();
//
        connectToSQL();
//
//        this.sqlHelper = new SQLHelper(SQL);
//        getProxy().registerChannel("my:channel");

        this.gameServerManagerMySQL = new GameServerManagerMySQL();
        initServerClasses();

        this.getProxy().getPluginManager().registerCommand(this, new JoinCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new DebugCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new QuitCommand(this));
        this.getProxy().getPluginManager().registerCommand(this, new RejoinCommand(this));
        this.getProxy().getPluginManager().registerListener(this, new JoinEvent(this));
        this.getProxy().getPluginManager().registerListener(this, new LeaveEvent(this));

        //loadGamePlugins();

        DataManager data = new DataManager(this, "servers.yml");
        gameServerManagerMySQL.registerGameServers(data);

        String[] nameParam = {"NAME", "VARCHAR(100)"};
        String[] stateParam = {"STATE", "VARCHAR(100)"};
        String[] gameStateParam = {"GAMESTATE", "VARCHAR(100)"};
        String[] isStartedParam = {"ISSTARTED", "BOOL"};

        new GameServerManagerMySQL();
        new ServerPlayerManagerMySQL();

        this.spigotCmdListener = new SpigotCommandListener(this);
        spigotCmdListener.listenForCommands();

        //sqlHelper.createTable("servers", "NAME", nameParam, stateParam, gameStateParam, isStartedParam);
    }

    @Override
    public void onDisable()
    {
        isShuttingDown = true;
        spigotCmdListener.onDisable();
        Disabler.getInstance().disableAll();
    }

    private void connectToSQL()
    {
        try
        {
            sqlConnection.connect();
        }
        catch (ClassNotFoundException | SQLException e)
        {
            //Login info incorrect
            //not using database

            ProxyServer.getInstance().getLogger().info("Database not connected!");
        }

        if(sqlConnection.isConnected())
        {
            ProxyServer.getInstance().getLogger().info("Database is connected!");
        }
    }

    public void initServerClasses()
    {
        new ServerPlayerManagerMySQL();
    }

    public static Main getInstance()
    {
        return instance;
    }

    public SQLHelper getSqlHelper()
    {
        return sqlHelper;
    }

    public boolean isShuttingDown()
    {
        return isShuttingDown;
    }

    public GameServerManager getGameServerManager()
    {
        return this.gameServerManagerMySQL;
    }

}
