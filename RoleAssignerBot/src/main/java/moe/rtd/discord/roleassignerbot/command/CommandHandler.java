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
    private static final Thread thread = new Thread(new CommandHandler());

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
        System.out.println("Command handler starting.");
        while(!stopped) {
            MessageReceivedEvent e;
            try {
                e = queue.take();
                System.out.println("Handling command \"" + Integer.toHexString(e.getMessage().hashCode()).toUpperCase() + "\".");
                try {
                    var s = e.getMessage().getContent();
                    s = s.replace("<@" + e.getClient().getOurUser().getLongID() + ">", "");
                    while(s.startsWith(" ") || s.startsWith("\n")) s = s.substring(1);
                    String name = s.split(" ")[0].toUpperCase();

                    String msg = null;
                    Commands cmd = null;

                    for(Commands c : Commands.values()) {
                        if(c.name().equals(name)) {
                            cmd = c;
                            msg = cmd.execute(e);
                            break;
                        }
                    }

                    if(cmd == null) msg = "Command \"" + name + "\" does not exist.";
                    if(msg != null) e.getChannel().sendMessage(e.getAuthor().mention() + " " + msg);
                } catch(CommandSyntaxException cse) {
                    e.getChannel().sendMessage(e.getAuthor().mention() + " Syntax Error: " + cse.getMessage());
                } catch(RuntimeException re) {
                    e.getChannel().sendMessage(e.getAuthor().mention() + " Discord Error: " + re.getMessage());
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
        System.out.println("Filtered command \"" +
                Integer.toHexString(messageReceivedEvent.getMessage().hashCode()).toUpperCase() + "\"."); // TODO replace with log4j
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
