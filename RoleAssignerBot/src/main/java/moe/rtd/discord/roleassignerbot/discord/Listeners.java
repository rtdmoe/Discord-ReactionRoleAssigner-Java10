package moe.rtd.discord.roleassignerbot.discord;

import moe.rtd.discord.roleassignerbot.command.CommandFilter;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;

/**
 * Class containing all Discord listeners.
 * The listeners get added via reflection for faster and simpler modification.
 * @author Big J
 */
@SuppressWarnings("unused")
class Listeners {

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
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    };

    /**
     * Listener for receiving and filtering commands.
     */
    static final IListener<MessageReceivedEvent> messageRecievedEvent = messageReceivedEvent -> {
        if(messageReceivedEvent.getMessage().getMentions().contains(messageReceivedEvent.getClient().getOurUser())) {
            try {
                CommandFilter.accept(messageReceivedEvent);
            } catch(InterruptedException e) {
                e.printStackTrace(); // TODO replace with log4j
            }
        }
    };
}
