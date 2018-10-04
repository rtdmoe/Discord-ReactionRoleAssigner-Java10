package moe.rtd.discord.roleassignerbot.discord;

import javafx.scene.control.Alert;
import moe.rtd.discord.roleassignerbot.Main;
import moe.rtd.discord.roleassignerbot.gui.GUI;
import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;

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
            DiscordException discordException;
            do {
                try {
                    var cb = new ClientBuilder();
                    String token = GUI.requestUserInput("bot token");
                    if(token == null) {
                        Main.exit(0);
                        return;
                    }
                    cb.withToken(token);
                    cb.withRecommendedShardCount();
                    client = cb.build();
                    discordException = null;
                } catch(DiscordException e) {
                    discordException = e;
                    try {
                        GUI.showDialog(Alert.AlertType.ERROR, MiscConstants.TITLE, "Discord Error", e.getErrorMessage());
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            } while(discordException != null);

            // Register listeners
            var ed = client.getDispatcher();

            System.out.println("Registering listeners."); // TODO replace with log4j
            for(var f : Listeners.class.getDeclaredFields()) {
                System.out.print(f.getName());
                if(Modifier.isStatic(f.getModifiers())) {
                    System.out.print(" static");
                    if(f.canAccess(null)) {
                        System.out.print(" accessible");
                        if(IListener.class.isAssignableFrom(f.getType())) {
                            System.out.print(" listener");
                            System.out.println();
                            try {
                                ed.registerListener(f.get(null));
                                System.out.println("Registered listener \"" + f.getName() + "\"."); // TODO replace with log4j
                            } catch(IllegalAccessException e) {
                                e.printStackTrace(); // TODO replace with log4j
                            }
                        } else System.out.println();
                    }
                }
            }

            // Log in and wait until ready
            client.login();

            synchronized(lockClient) {
                while(!(client.isReady() && client.isLoggedIn())) {
                    try {
                        Thread.sleep(MiscConstants.ARBITRARY_SLEEP_DURATION);
                    } catch(InterruptedException e) {
                        e.printStackTrace(); // TODO replace with log4j
                    }
                }
            }

            // Change presence to show the bot is setting up
            client.changePresence(StatusType.IDLE, ActivityType.PLAYING, "setting up...");
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
