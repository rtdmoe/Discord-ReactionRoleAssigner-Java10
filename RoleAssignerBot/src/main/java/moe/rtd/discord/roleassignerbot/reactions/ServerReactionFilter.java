package moe.rtd.discord.roleassignerbot.reactions;

import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import moe.rtd.discord.roleassignerbot.misc.DataFormatter;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Filter for reaction events from a specific server.
 */
public class ServerReactionFilter extends ReactionFilter<ServerConfiguration> {

    /**
     * Sets up the queue and starts the thread.
     * @param owner Server that this reactions belongs to.
     */
    public ServerReactionFilter(ServerConfiguration owner) {
        super(owner);
    }

    @Override
    protected void filter(ReactionEvent reactionEvent) throws InterruptedException {
        var channel = getOwner().getChannel(reactionEvent.getChannel().getLongID());
        if(channel != null) {
            log.trace(Markers.REACTIONS, "Reaction filtered to: " + DataFormatter.format(channel));
            channel.getReactionFilter().accept(reactionEvent);
        }
    }
}
