package moe.rtd.discord.roleassignerbot;

import sx.blah.discord.api.IDiscordClient;

/**
 * Creates and maintains a Discord connection.
 * @author Big J
 */
public class DiscordConnection {

    /**
     * TODO
     */
    private static final Object lockClient = new Object();
    /**
     * TODO
     */
    private static volatile IDiscordClient client;

    /**
     * TODO
     */
    public static IDiscordClient getClient() {
        synchronized(lockClient) {
            return client;
        }
    }

    // TODO
}
