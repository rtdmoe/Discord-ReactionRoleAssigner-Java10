package moe.rtd.discord.roleassignerbot.config;

import java.util.Map;

/**
 * Class responsible for the bot settings and configuration, including loading and saving it.
 * @author Big J
 */
public class BotSettings {

    /**
     * Object for using as a basic monitor for synchronizing when modifying the server map.
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
     * Returns the {@link ServerConfiguration} with the specified ID, or null if it isn't mapped.
     * @param ID ID of the server to return.
     * @return The requested server, or null if it isn't mapped.
     */
    public static ServerConfiguration getServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            return serverConfigurations.get(ID);
        }
    }

    /**
     * Adds a {@link ServerConfiguration} to the server map, if necessary.
     * @param ID ID of the server to add.
     * @return The {@link ServerConfiguration} that was added if necessary, or the value corresponding to the ID
     * if the server is already mapped.
     */
    public static ServerConfiguration addServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            var gotSC = serverConfigurations.get(ID);
            if(gotSC == null) {
                var server = new ServerConfiguration(ID);
                serverConfigurations.put(ID, server);
                return server;
            } else {
                return gotSC;
            }
        }
    }

    /**
     * Removes a {@link ServerConfiguration} from the server map.
     * @param ID ID of the server to remove.
     */
    public static void removeServer(long ID) {
        ServerConfiguration server;
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            server = serverConfigurations.remove(ID);
        }
        if(server != null) server.terminate();
    }
}
