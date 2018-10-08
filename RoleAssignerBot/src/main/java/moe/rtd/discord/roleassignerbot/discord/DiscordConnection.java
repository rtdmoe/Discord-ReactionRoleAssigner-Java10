package moe.rtd.discord.roleassignerbot.discord;

import javafx.scene.control.Alert;
import moe.rtd.discord.roleassignerbot.Main;
import moe.rtd.discord.roleassignerbot.gui.GUI;
import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(DiscordConnection.class);

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
                        log.fatal(Markers.DISCORD, "Thread interrupted while waiting for user to close GUI dialog.", e);
                    }
                }
            } while(discordException != null);

            // Register listeners
            var ed = client.getDispatcher();

            log.debug(Markers.DISCORD, "Registering listeners.");
            for(var f : Listeners.class.getDeclaredFields()) {
                StringBuilder msg = new StringBuilder("Found \"");
                msg.append(f.getName());
                msg.append("\"");
                if(Modifier.isStatic(f.getModifiers())) {
                    msg.append(", is static");
                    if(f.canAccess(null)) {
                        msg.append(", accessible");
                        if(IListener.class.isAssignableFrom(f.getType())) {
                            msg.append(", and a listener");
                            log.debug(Markers.DISCORD, msg.toString());
                            try {
                                ed.registerListener(f.get(null));
                                log.info(Markers.DISCORD, "Registered listener \"" + f.getName() + "\".");
                            } catch(Exception e) {
                                log.fatal(Markers.DISCORD, "Error registering listener: ", e);
                            }
                        } else log.debug(Markers.DISCORD, msg.toString());
                    } else log.debug(Markers.DISCORD, msg.toString());
                } else log.debug(Markers.DISCORD, msg.toString());
            }

            // Log in and wait until ready
            client.login();

            synchronized(lockClient) {
                while(!(client.isReady() && client.isLoggedIn())) {
                    try {
                        Thread.sleep(MiscConstants.ARBITRARY_SLEEP_DURATION);
                    } catch(InterruptedException e) {
                        log.error(Markers.DISCORD, "Thread interrupted while waiting for Discord to login.", e);
                    }
                }
            }

            // Change presence to show the bot is setting up
            client.changePresence(StatusType.IDLE, ActivityType.PLAYING, "setting up...");
            log.info(Markers.DISCORD, "Discord setup complete.");
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
