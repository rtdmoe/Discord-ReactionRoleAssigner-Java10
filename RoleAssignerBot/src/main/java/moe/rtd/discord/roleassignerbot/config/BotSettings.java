package moe.rtd.discord.roleassignerbot.config;

import java.util.Map;

/**
 * Class responsible for the bot settings and configuration, including loading and saving it.
 * @author Big J
 */
public class BotSettings {

    /**
     * Object for using as a basic monitor for thread-safely modifying the server map.
     */
    private static final Object lockServerConfigurations = new Object();
    /**
     * Map of all servers which the bot is a member of.
     */
    private static Map<Long, ServerConfiguration> serverConfigurations;

    /**
     * Loads the bot configuration.
     */
    public static void loadConfiguration() {
        // TODO
    }

    /**
     * Saves the bot configuration.
     */
    public static void saveConfiguration() {
        // TODO
    }

    /**
     * Adds a server to the server map.
     * @param ID ID of the server to add.
     * @return The object for the server that was added.
     */
    public static ServerConfiguration addServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            ServerConfiguration server = new ServerConfiguration(ID);
            serverConfigurations.put(ID, server);
            return server;
        }
    }

    /**
     * Removes a server from the server map.
     * @param ID ID of the server to remove.
     */
    public static void removeServer(long ID) {
        ServerConfiguration server;
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            server = serverConfigurations.remove(ID);
        }
        server.terminate();
    }
}
