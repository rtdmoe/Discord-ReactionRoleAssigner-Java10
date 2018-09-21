package moe.rtd.discord.roleassignerbot.command;

import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
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
        // TODO add log4j
        while(!stopped) {
            MessageReceivedEvent e;
            try {
                e = queue.take();
                if(e.getAuthor().hasRole(e.getGuild().getRoleByID((Long) BotSettings.getServer(
                        e.getGuild().getLongID()).getProperty(ServerConfiguration.Properties.AUTHORIZED_ROLE)))
                        || e.getAuthor().getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR)) {
                    CommandHandler.accept(e);
                }
            } catch(InterruptedException ie) {
                ie.printStackTrace(); // TODO replace with log4j
            }
        }
    }

    /**
     * Puts an event at the end of the queue.
     * @param messageReceivedEvent The event to add to the queue.
     * @throws InterruptedException If the current thread is interrupted when waiting for a free space in the queue.
     */
    public static void accept(MessageReceivedEvent messageReceivedEvent) throws InterruptedException {
        if(stopped) return;
        queue.put(messageReceivedEvent);
    }

    /**
     * Starts this filter and the package-private command handler.
     */
    public static void start() {
        synchronized(thread) {
            thread.start();
        }
        CommandHandler.start();
    }

    /**
     * Stops this filter and the package-private command handler.
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
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    }
}
