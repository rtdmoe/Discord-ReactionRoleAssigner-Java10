package moe.rtd.discord.roleassignerbot.filter;

import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Filter for reaction events from a specific server.
 */
public class ServerReactionFilter extends ReactionFilter<ServerConfiguration> {

    /**
     * Sets up the queue and starts the thread.
     * @param owner Server that this filter belongs to.
     */
    public ServerReactionFilter(ServerConfiguration owner) {
        super(owner);
    }

    @Override
    protected void filter(ReactionEvent reactionEvent) {
        var ID = reactionEvent.getChannel().getLongID();
        var channel = getOwner().getChannel(ID);
        if(channel != null) {
            try {
                channel.getReactionFilter().accept(reactionEvent);
            } catch(InterruptedException e) {
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    }
}
