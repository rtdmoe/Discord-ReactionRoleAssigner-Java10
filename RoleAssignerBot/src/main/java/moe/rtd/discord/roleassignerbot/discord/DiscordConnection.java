package moe.rtd.discord.roleassignerbot.discord;

import moe.rtd.discord.roleassignerbot.gui.GUI;
import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.lang.reflect.Modifier;

/**
 * Creates and maintains a Discord connection.
 * @author Big J
 */
public class DiscordConnection {

    /**
     * Object for synchronizing changes to the {@link DiscordConnection#client}.
     */
    private static final Object lockClient = new Object();
    /**
     * {@link IDiscordClient} for Discord integration.
     */
    private static volatile IDiscordClient client;

    /**
     * Logs in and sets up the client.
     */
    public static void start() {
        synchronized(lockClient) {

            // Creates a new client
            var cb = new ClientBuilder();

            cb.withToken(GUI.requestUserInput("bot token"));
            cb.withRecommendedShardCount();

            client = cb.build();

            // Register listeners
            var ed = client.getDispatcher();

            for(var f : Listeners.class.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers())) {
                    if(f.canAccess(null)) {
                        if(f.getType().isInstance(IListener.class)) {
                            try {
                                ed.registerListener(f.get(null));
                            } catch(IllegalAccessException e) {
                                e.printStackTrace(); // TODO replace with log4j
                            }
                        }
                    }
                }
            }

            // Log in and wait until ready
            client.login();

            while(!client.isReady()) {
                try {
                    Thread.sleep(MiscConstants.ARBITRARY_SLEEP_DURATION);
                } catch(InterruptedException e) {
                    e.printStackTrace(); // TODO replace with log4j
                }
            }

            // Change presence to show the bot is setting up
            client.changePresence(StatusType.IDLE, ActivityType.PLAYING, "setting up...");
        }
    }

    /**
     * Logs out and removes the client reference.
     */
    public static void stop() {
        synchronized(lockClient) {

            // Logs out on all shards
            client.logout();

            // Removes client reference
            client = null;
        }
    }

    /**
     * @return The {@link DiscordConnection#client}.
     */
    public static IDiscordClient getClient() {
        synchronized(lockClient) {
            return client;
        }
    }
}
