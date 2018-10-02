package moe.rtd.discord.roleassignerbot;

import moe.rtd.discord.roleassignerbot.command.CommandFilter;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.discord.DiscordConnection;
import moe.rtd.discord.roleassignerbot.gui.GUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

/**
 * Main class - Starts the program.
 * @author Big J
 */
public class Main {

    /**
     * Log4j2 Logger for this class.
     */
    private static Logger log = LogManager.getLogger(Main.class);

    /**
     * Program's entry point method.
     * Starts everything in the correct order.
     * @param args Command line arguments are unused.
     */
    public static void main(String[] args) {

        log.debug("Starting startup sequence...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::exit)); // Sets up shutdown hook first in case the program exits before it is fully setup.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {e.printStackTrace(); Runtime.getRuntime().halt(-1);}); // Program crashes on an uncaught exception.

        GUI.setup(); // Sets up the GUI.
        BotSettings.loadConfiguration(); // Loads the saved message configuration so that events aren't missed.
        CommandFilter.start(); // Starts the command reactions and handler just after connecting to Discord.
        DiscordConnection.start(); // Connects to Discord, and the events can start queuing up.
        BotSettings.start(); // Starts the event handlers last so that the missed events can be processed first.

        log.debug("Setup complete, changing bot presence.");

        DiscordConnection.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "with roles"); // Change bot presence to show that it's running.

        log.info("Startup complete.");
    }

    /**
     * Program's exit point method.
     * Shuts down and saves everything in the correct order.
     */
    private static void exit() {

        log.info("Starting shutdown sequence...");

        BotSettings.stop(); // Stops the event handlers so that the event handling isn't interrupted by the closing connection.
        CommandFilter.stop(); // Stops the command reactions and handler.
        BotSettings.saveConfiguration(); // Saves the configuration to the save file.
        BotSettings.destroy(); // Clears all loaded configuration data and stops any running threads in the process.
        GUI.close(); // Closes the GUI.

        log.info("Shutdown complete.");
    }

    /**
     * Shuts down the program normally.
     * @param status Status code to exit with.
     */
    public static void exit(int status) {

        log.debug("Changing bot presence to shutting down...");

        IDiscordClient dc = DiscordConnection.getClient();
        if(dc != null) dc.changePresence(StatusType.DND, ActivityType.PLAYING, "shutting down..."); // Change bot presence to show that it's shutting down.

        log.debug("Bot presence updated, exiting JVM with exit code " + status + ".");

        System.exit(status); // Exit with the specified status code.
    }
}
