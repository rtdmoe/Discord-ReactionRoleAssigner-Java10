package moe.rtd.discord.roleassignerbot.filter;

import moe.rtd.discord.roleassignerbot.config.ChannelConfiguration;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Filter for reaction events from a specific channel.
 */
public class ChannelReactionFilter extends ReactionFilter<ChannelConfiguration> {

    /**
     * Sets up the queue and starts the thread.
     * @param owner Channel that this filter belongs to.
     */
    public ChannelReactionFilter(ChannelConfiguration owner) {
        super(owner);
    }

    @Override
    protected void filter(ReactionEvent reactionEvent) {
        var ID = reactionEvent.getMessageID();
        var message = getOwner().getMessage(ID);
        if(message != null) {
            try {
                message.getReactionHandler().accept(reactionEvent);
            } catch(InterruptedException e) {
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    }
}
