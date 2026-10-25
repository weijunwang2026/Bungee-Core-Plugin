# Bungee-Core-Plugin #

## A BungeeCord plugin that handles player/party matchmaking supporting multiple game instances ##

This plugin uses BungeeCord and MySQL to support multiple game instances and player queueing. Some features include:

* Automatic game lobby assignment, checking current capacity and other predicates defined by each game server
* Party queueing
* Game rejoining after a disconnection

## Commands ##
/join \<game\> - Enters the queue for the specified game

## Dependencies ##
* [BungeecordPartyAndFriends](https://github.com/Simonsator/BungeecordPartyAndFriends)
* [Party-API-For-Spigot](https://github.com/Simonsator/Party-API-For-Spigot)
* [Friends-API-For-Spigot-For-PAF-Proxy](https://github.com/Simonsator/Friends-API-For-Spigot-For-PAF-Proxy)

## Plugin Installation ##
0. Create and set up a Bungeecord network with a lobby and a game server. Create a MySQL database for plugin communication. Set up the dependencies using the links above.
1. Place the plugin in the plugins folder and start the proxy. This will generate the plugin folder. You will be prompted to enter the database login information in mysql.yml.
2. In the servers.yml, create a section for each game on your network. The section title is the name used for the join command. Under the "servers" section, list the server ids for each game server (see [Spigot-CoreAPI](https://github.com/weijunwang2026/Spigot-CoreAPI)).
```yaml
games:
  oceanraiders:
    servers:
      - "oc001"
      - "oc002"
  desert:
    servers:
      - "dc001"
```
4. In the config.yml for BungeeCord, set force_default_server to true, and route it to the lobby server. This ensures the player always lands in the lobby server when they join.
5. Restart the proxy to apply those changes.
