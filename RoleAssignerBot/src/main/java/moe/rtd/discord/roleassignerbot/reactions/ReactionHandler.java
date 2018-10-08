package moe.rtd.discord.roleassignerbot.reactions;

import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.discord.DiscordConnection;
import moe.rtd.discord.roleassignerbot.config.MessageConfiguration;
import moe.rtd.discord.roleassignerbot.interfaces.Terminable;
import moe.rtd.discord.roleassignerbot.misc.DataFormatter;
import moe.rtd.discord.roleassignerbot.interfaces.QueueConsumer;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Processes the reaction events for the message it's assigned to.
 * @author Big J
 */
public class ReactionHandler implements QueueConsumer<ReactionEvent>, Runnable, Terminable {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(ReactionHandler.class);

    /**
     * Queue for storing reaction events to be processed.
     */
    private final BlockingQueue<ReactionEvent> queue;

    /**
     * Reference to the message configuration that this reaction handler is assigned to.
     */
    private final MessageConfiguration messageConfiguration;

    /**
     * Thread which processes the reaction events.
     */
    private final Thread thread;
    /**
     * Whether or not this reaction handler has been terminated.
     */
    private volatile boolean terminated = false;

    /**
     * Constructs a new reaction handler.
     */
    public ReactionHandler(MessageConfiguration messageConfiguration) {
        this.queue = new ArrayBlockingQueue<>(128);
        this.messageConfiguration = messageConfiguration;
        this.thread = new Thread(this);
    }

    /**
     * Starts the reaction handler thread.
     * @throws IllegalThreadStateException – if the thread was already started.
     * @see Thread#start()
     */
    public void start() {
        thread.start();
    }

    /**
     * Processes the reaction events in the queue.
     */
    @Override
    public void run() {
        log.debug(Markers.REACTIONS, "Reaction handler for message " + DataFormatter.format(messageConfiguration) + " has started.");
        process();
        while(!terminated) {
            try {
                handle(queue.take());
            } catch(InterruptedException e) {
                log.debug(Markers.REACTIONS, "Reaction handler for message " + DataFormatter.format(messageConfiguration) + " has been interrupted.");
                if(terminated) break;
            }
        }
        log.debug(Markers.REACTIONS, "Reaction handler for message " + DataFormatter.format(messageConfiguration) + " has been terminated.");
    }

    /**
     * Processes all reactions on startup; catches up.
     * Adds all roles that need to be added, and removes roles that need to be removed.
     */
    private void process() {
        if(terminated) return;

        var client = DiscordConnection.getClient();
        if(client == null) return;

        var channel = messageConfiguration.getParent();
        var server = channel.getParent();

        var messageID = messageConfiguration.getID();

        var dServer = client.getGuildByID(server.getID());
        log.debug(Markers.REACTIONS, "Server: " + ((dServer != null) ? dServer.getLongID() : "null"));
        var dChannel = ((dServer != null) ? dServer.getChannelByID(channel.getID()) : null);
        log.debug(Markers.REACTIONS, "Channel: " + ((dChannel != null) ? dChannel.getLongID() : "null"));
        var dHistory = ((dChannel != null) ? dChannel.getMessageHistoryIn(messageID, messageID) : null);
        log.debug(Markers.REACTIONS, "History: " + ((dHistory != null) ? messageID : "null"));
        var dMessage = ((dHistory != null) ? dHistory.get(messageID) : null);
        log.debug(Markers.REACTIONS, "Message: " + ((dMessage != null) ? dMessage.getLongID() : "null"));

        if(dMessage == null) {
            log.error(Markers.REACTIONS, "Message " + DataFormatter.format(messageConfiguration) + " not found.");
            terminate();
            return;
        }

        List<IReaction> reactions = dMessage.getReactions();

        for(IReaction r : reactions) {
            if(messageConfiguration.isUsed(r.getEmoji().toString())) {
                var ROLE = dMessage.getGuild().getRoleByID(messageConfiguration.getRole(r.getEmoji().toString()));
                // FOR EACH CONFIGURED REACTION:

                for(IUser u : r.getUsers()) {
                    // ADD ALL MISSING ROLES
                    RequestBuffer.request(() -> {
                        try {
                            if(!(u.hasRole(ROLE))) {
                                u.addRole(ROLE);
                                log.info(Markers.REACTIONS, "Added role: " + ROLE.mention() + " to " + u.mention());
                            }
                        } catch(Exception ex) {
                            if(ex instanceof RateLimitException) throw ex;
                            log.error(Markers.REACTIONS, "Error adding role: " + ex.getMessage());
                        }
                    });
                }

                for(IUser u : dMessage.getGuild().getUsersByRole(ROLE)) {
                    // REMOVE ALL UNWANTED ROLES
                    RequestBuffer.request(() -> {
                        try {
                            if(!(r.getUserReacted(u))) {
                                u.removeRole(ROLE);
                                log.info(Markers.REACTIONS, "Removed role: " + ROLE.mention() + " from " + u.mention());
                            }
                        } catch(Exception ex) {
                            if(ex instanceof RateLimitException) throw ex;
                            log.error(Markers.REACTIONS, "Error removing role: " + ex.getMessage());
                        }
                    });
                }
            }
        }
    }

    /**
     * Handles a reaction event.
     * @param reactionEvent The reaction event to handle.
     */
    private void handle(ReactionEvent reactionEvent) throws InterruptedException {
        if(terminated) throw new InterruptedException();

        String out = "Reaction to " + DataFormatter.format(BotSettings
                .getServer(reactionEvent.getGuild().getLongID())
                .getChannel(reactionEvent.getChannel().getLongID())
                .getMessage(reactionEvent.getMessageID())
        );

        if(reactionEvent instanceof ReactionAddEvent) {
            var e = (ReactionAddEvent) reactionEvent;

            out += " has been added";

            String EMOTE = e.getReaction().getEmoji().toString();
            if(messageConfiguration.isUsed(EMOTE)) {
                out += " and accepted.";
                var ROLE = e.getGuild().getRoleByID(messageConfiguration.getRole(EMOTE));
                RequestBuffer.request(() -> {
                    try {
                        e.getUser().addRole(ROLE);
                        log.debug(Markers.REACTIONS, "Added role: " + ROLE.mention() + " to " + e.getUser().mention());
                    } catch(Exception ex) {
                        if(ex instanceof RateLimitException) throw ex;
                        log.error(Markers.REACTIONS, "Error adding role: " + ex.getMessage());
                    }
                });
            }

        } else if(reactionEvent instanceof ReactionRemoveEvent) {
            var e = (ReactionRemoveEvent) reactionEvent;

            out += " has been removed";

            String EMOTE = e.getReaction().getEmoji().toString();
            if(messageConfiguration.isUsed(EMOTE)) {
                out += " and accepted.";
                var ROLE = e.getGuild().getRoleByID(messageConfiguration.getRole(EMOTE));
                RequestBuffer.request(() -> {
                    try {
                        e.getUser().removeRole(ROLE);
                        log.debug(Markers.REACTIONS, "Removed role: " + ROLE.mention() + " from " + e.getUser().mention());
                    } catch(Exception ex) {
                        if(ex instanceof RateLimitException) throw ex;
                        log.error(Markers.REACTIONS, "Error removing role: " + ex.getMessage());
                    }
                });
            }

        } else throw new RuntimeException("ReactionEvent is neither one of the two known subclasses.");
        log.info(out);
    }

    /**
     * Puts an event at the end of the queue.
     * @param reactionEvent The event to add to the queue.
     * @throws InterruptedException If the current thread is interrupted when waiting for a free space in the queue.
     */
    public void accept(ReactionEvent reactionEvent) throws InterruptedException {
        if(terminated) return;
        queue.put(reactionEvent);
    }

    /**
     * Terminates this reaction handler, clears the queue and tries to stop its thread immediately.
     */
    @Override
    public void terminate() {
        if(terminated) return;
        synchronized(thread) {
            if(terminated) return;
            terminated = true;
            thread.interrupt();
            queue.clear();
            if(Thread.currentThread() == thread) return;
            try {
                thread.join();
            } catch(InterruptedException e) {
                throw new RuntimeException("Terminator has been interrupted.");
            }
        }
    }
}
