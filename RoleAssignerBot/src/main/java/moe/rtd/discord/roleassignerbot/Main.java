package moe.rtd.discord.roleassignerbot;

import moe.rtd.discord.roleassignerbot.command.CommandFilter;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.discord.DiscordConnection;
import moe.rtd.discord.roleassignerbot.gui.GUI;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class - Starts the program.
 * @author Big J
 */
public class Main {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log;

    /**
     * Object for synchronizing the {@link #exit(int)} method.
     */
    private static final Object lockExit = new Object();
    /**
     * Whether or not {@link #exit(int)} has already been called.
     */
    private static volatile boolean exit = false;

    static { // Sets the system property for the Log4j log folder before initializing Log4j.
        String logFolder;
        boolean created;
        {
            try {
                var jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                var jp = jar.getPath();
                logFolder = (jar.isFile()) ? jp.replaceFirst("(.jar)$", "") :
                        jp + ((jp.endsWith("\\") || jp.endsWith("/")) ? "" : "/") + "logs";
                created = new File(logFolder).mkdirs();
            } catch(URISyntaxException e) {
                throw new Error("Cannot find currently running code.", e);
            }
        }
        System.setProperty("roleAssignerBot.logFolder", logFolder);
        log = LogManager.getLogger(Main.class);
        log.debug(Markers.MAIN, "Log folder set to: \"" + logFolder + "\", folder has " + (created ? "" : "not ") + "been created.");
    }

    /**
     * Whether or not the bot has been set up yet.
     */
    private static final AtomicBoolean setup = new AtomicBoolean(false);

    /**
     * Program's entry point method.
     * Starts everything in the correct order.
     * @param args Command line arguments: "debug": enable console logging.
     */
    public static void main(String[] args) {

        for(var arg : args) { // Performs command line arguments.
            switch(arg.toLowerCase()) {
                case "debug":
                    Configurator.setRootLevel(Level.DEBUG);
                    log.warn(Markers.MAIN, "Debugging mode activated.");
                    break;
                    default: log.warn(Markers.MAIN, "Argument \"" + arg.toLowerCase() + "\" has not been recognised.");
            }
        }

        log.debug(Markers.MAIN, "Starting startup sequence...");

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {e.printStackTrace(); Runtime.getRuntime().halt(-1);}); // Program crashes on an uncaught exception.

        GUI.setup(); // Sets up the GUI.
        BotSettings.loadConfiguration(); // Loads the saved message configuration so that events aren't missed.
        CommandFilter.start(); // Starts the command reactions and handler just after connecting to Discord.
        DiscordConnection.start(); // Connects to Discord, and the events can start queuing up.
        BotSettings.start(); // Starts the event handlers last so that the missed events can be processed first.

        setup.set(true);
        synchronized(setup) {
            setup.notifyAll();
        }

        log.debug(Markers.MAIN, "Setup complete, changing bot presence.");

        IDiscordClient dc = DiscordConnection.getClient();
        if(dc != null) dc.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "with roles"); // Change bot presence to show that it's running.

        log.info(Markers.MAIN, "Startup complete.");
    }

    /**
     * Shuts down the program.
     * @param status Status code to exit with.
     */
    public static void exit(int status) {
        if(exit) return;
        synchronized(lockExit) {
            if(exit) return;
            exit = true;

            log.debug(Markers.MAIN, "Changing bot presence to shutting down...");

            IDiscordClient dc = DiscordConnection.getClient();
            if(dc != null) dc.changePresence(StatusType.DND, ActivityType.PLAYING, "shutting down..."); // Change bot presence to show that it's shutting down.

            log.debug(Markers.MAIN, "Bot presence updated, exiting JVM with exit code " + status + ".");

            var thread = new Thread(() -> {
                log.info(Markers.MAIN, "Starting shutdown sequence...");

                while(!setup.get()) {
                    synchronized(setup) {
                        try {
                            setup.wait(1000);
                        } catch (InterruptedException e) {
                            log.fatal(Markers.MAIN, "Shutdown thread interrupted.", e);
                        }
                    }
                }

                BotSettings.stop(); // Stops the event handlers so that the event handling isn't interrupted by the closing connection.
                CommandFilter.stop(); // Stops the command reactions and handler.
                BotSettings.saveConfiguration(); // Saves the configuration to the save file.
                BotSettings.destroy(); // Clears all loaded configuration data and stops any running threads in the process.
                GUI.close(); // Closes the GUI.

                log.info(Markers.MAIN, "Shutdown complete.");
                LogManager.shutdown();
                System.exit(status);
            });

            thread.start(); // Exit with the specified status code.
        }
    }
}
