package moe.rtd.discord.roleassignerbot.reactions;

import moe.rtd.discord.roleassignerbot.config.ChannelConfiguration;
import moe.rtd.discord.roleassignerbot.misc.DataFormatter;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Filter for reaction events from a specific channel.
 */
public class ChannelReactionFilter extends ReactionFilter<ChannelConfiguration> {

    /**
     * Sets up the queue and starts the thread.
     * @param owner Channel that this reactions belongs to.
     */
    public ChannelReactionFilter(ChannelConfiguration owner) {
        super(owner);
    }

    @Override
    protected void filter(ReactionEvent reactionEvent) throws InterruptedException {
        var message = getOwner().getMessage(reactionEvent.getMessageID());
        if(message != null) {
            log.trace(Markers.REACTIONS, "Reaction filtered to: " + DataFormatter.format(message));
            message.getReactionHandler().accept(reactionEvent);
        }
    }
}
