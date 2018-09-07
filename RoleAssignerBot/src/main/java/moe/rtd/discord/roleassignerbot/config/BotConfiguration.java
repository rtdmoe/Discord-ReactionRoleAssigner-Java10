package moe.rtd.discord.roleassignerbot.config;

import java.util.Map;

/**
 * Class responsible for the bot configuration, including loading and saving it.
 * @author Big J
 */
public class BotConfiguration {

    /**
     * Object for using as a basic monitor for thread-safely modifying the server map.
     */
    private static final Object lockServerConfigurations = new Object();
    /**
     * Map of all servers which the bot is a member of.
     */
    private static Map<Long, Object> serverConfigurations;// TODO change Object to server configuration object

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
     */
    public static void addServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            serverConfigurations.put(ID, new Object());// TODO change Object to server configuration object
        }
    }

    /**
     * Removes a server from the server map.
     */
    public static void removeServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            serverConfigurations.remove(ID);
        }
    }
}
