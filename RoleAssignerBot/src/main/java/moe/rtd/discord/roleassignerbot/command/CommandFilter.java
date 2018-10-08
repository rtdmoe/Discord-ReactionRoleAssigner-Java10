package moe.rtd.discord.roleassignerbot.command;

import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Permissions;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Filters commands; makes sure that command users are authorised.
 * @author Big J
 */
public class CommandFilter implements Runnable {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(CommandFilter.class);

    /**
     * Queue for storing reaction events to be processed.
     */
    private static final BlockingQueue<MessageReceivedEvent> queue = new ArrayBlockingQueue<>(128);

    /**
     * Thread for processing events.
     */
    private static final Thread thread = new Thread(new CommandFilter());

    /**
     * Whether or not this class has been stopped.
     */
    private static volatile boolean stopped = false;

    /**
     * Processes the message received events in the queue.
     */
    @Override
    public void run() {
        log.info(Markers.COMMAND, "Command filter has started.");
        while(!stopped) {
            MessageReceivedEvent e;
            try {
                e = queue.take();
                var s = BotSettings.getServer(e.getGuild().getLongID());
                if(e.getAuthor().getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR) ||
                        ((s != null) &&
                        e.getAuthor().hasRole(e.getGuild().getRoleByID((Long)
                        s.getProperty(ServerConfiguration.Properties.AUTHORIZED_ROLE))))) {
                    CommandHandler.accept(e);
                }
            } catch(InterruptedException ie) {
                log.warn(Markers.COMMAND, "Command filter has been interrupted.");
            }
        }
        log.info(Markers.COMMAND, "Command filter has stopped.");
    }

    /**
     * Puts an event at the end of the queue.
     * @param messageReceivedEvent The event to add to the queue.
     * @throws InterruptedException If the current thread is interrupted when waiting for a free space in the queue.
     */
    public static void accept(MessageReceivedEvent messageReceivedEvent) throws InterruptedException {
        if(stopped) return;
        log.debug(Markers.COMMAND, "Received command \"" +
                Integer.toHexString(messageReceivedEvent.getMessage().hashCode()).toUpperCase() + "\".");
        queue.put(messageReceivedEvent);
    }

    /**
     * Starts this reactions and the package-private command handler.
     */
    public static void start() {
        synchronized(thread) {
            thread.start();
        }
        CommandHandler.start();
        log.info(Markers.COMMAND, "Command handling setup complete.");
    }

    /**
     * Stops this reactions and the package-private command handler.
     */
    public static void stop() {
        if(stopped) return;
        synchronized(thread) {
            if(stopped) return;
            stopped = true;
            thread.interrupt();
            CommandHandler.stop();
            try {
                thread.join();
            } catch(InterruptedException e) {
                log.fatal(Markers.COMMAND, "Thread interrupted while waiting for command filter to stop.", e);
            }
        }
    }
}
