package moe.rtd.discord.roleassignerbot.reactions;

import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import moe.rtd.discord.roleassignerbot.misc.DataFormatter;
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
    protected void filter(ReactionEvent reactionEvent) {
        var ID = reactionEvent.getChannel().getLongID();
        var channel = getOwner().getChannel(ID);
        if(channel != null) {
            System.out.println("Reaction filtered: " + DataFormatter.format(channel));
            try {
                channel.getReactionFilter().accept(reactionEvent);
            } catch(InterruptedException e) {
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    }
}
