package moe.rtd.discord.roleassignerbot.command;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Handles checking and execution of commands.
 * @author Big J
 */
class CommandHandler implements Runnable {

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
                try {
                    var s = e.getMessage().getContent();
                    Commands.valueOf(s.substring(0, s.indexOf(' '))).getCommand().execute(e);
                } catch(CommandSyntaxException cse) {
                    e.getChannel().sendMessage("Syntax Error: " + cse.getMessage());
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
    static void accept(MessageReceivedEvent messageReceivedEvent) throws InterruptedException {
        if(stopped) return;
        queue.put(messageReceivedEvent);
    }

    /**
     * Starts handling commands.
     */
    static void start() {
        synchronized(thread) {
            thread.start();
        }
    }

    /**
     * Stops handling commands.
     */
    static void stop() {
        if(stopped) return;
        synchronized(thread) {
            if(stopped) return;
            stopped = true;
            thread.interrupt();
            try {
                thread.join();
            } catch(InterruptedException e) {
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    }
}
