package moe.rtd.discord.roleassignerbot.discord;

import moe.rtd.discord.roleassignerbot.command.CommandFilter;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Class containing all Discord listeners.
 * The listeners get added via reflection for faster and simpler modification.
 * @author Big J
 */
@SuppressWarnings("unused")
final class Listeners {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(Listeners.class);

    /**
     * Listener for receiving and filtering {@link ReactionEvent}s into the {@link ServerConfiguration}s.
     */
    static final IListener<ReactionEvent> reactionEvent = reactionEvent -> {
        var ID = reactionEvent.getGuild().getLongID();
        var server = BotSettings.getServer(ID);
        if(server != null) {
            try {
                server.getReactionFilter().accept(reactionEvent);
            } catch(InterruptedException e) {
                log.error(Markers.DISCORD, "Thread interrupted while waiting to queue the event.", e);
            }
        }
    };

    /**
     * Listener for receiving and filtering commands.
     */
    static final IListener<MessageReceivedEvent> messageReceivedEvent = messageReceivedEvent -> {
        if(messageReceivedEvent.getMessage().getMentions().contains(messageReceivedEvent.getClient().getOurUser())) {
            try {
                CommandFilter.accept(messageReceivedEvent);
            } catch(InterruptedException e) {
                log.error(Markers.DISCORD, "Thread interrupted while waiting to queue the event.", e);
            }
        }
    };
}
