package moe.rtd.discord.roleassignerbot.filter;

import moe.rtd.discord.roleassignerbot.config.Terminable;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

import java.util.function.Consumer;

/**
 * TODO
 * @author Big J
 */
public class ReactionFilter implements Consumer<ReactionEvent>, Runnable, Terminable {

    /**
     * TODO
     */
    public ReactionFilter() {
        // TODO
    }

    /**
     * TODO
     */
    @Override
    public void run() {
        // TODO
    }

    /**
     * Accepts an event.
     * @param reactionEvent The event to add to the queue.
     */
    @Override
    public void accept(ReactionEvent reactionEvent) {
        // TODO
    }

    /**
     * TODO
     */
    @Override
    public void terminate() {
        // TODO
    }
}
