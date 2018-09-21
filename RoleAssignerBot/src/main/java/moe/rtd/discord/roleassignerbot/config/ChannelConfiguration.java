package moe.rtd.discord.roleassignerbot.config;

import moe.rtd.discord.roleassignerbot.filter.ChannelReactionFilter;
import moe.rtd.discord.roleassignerbot.interfaces.Terminable;

import java.util.Map;
import java.util.TreeMap;

/**
 * Class responsible for one Discord channel per instance.
 * Stores all message configuration for the channel it is responsible for.
 * @author Big J
 */
public class ChannelConfiguration extends IdentifiableChild<ServerConfiguration> implements Terminable {

    /**
     * Map of all messages in this channel that the bot is configured for.
     */
    private final Map<Long, MessageConfiguration> messageConfigurations;
    /**
     * Whether or not this instance has been terminated.
     */
    private volatile boolean terminated = false;

    /**
     * The reaction event filter for this instance.
     */
    private final ChannelReactionFilter reactionFilter;

    /**
     * Instantiates this {@link ChannelConfiguration}; sets up the map.
     * @param ID The ID of the channel.
     * @param parent The server that this channel belongs to.
     */
    ChannelConfiguration(long ID, ServerConfiguration parent) {
        super(ID, parent);
        messageConfigurations = new TreeMap<>();
        reactionFilter = new ChannelReactionFilter(this);
    }

    /**
     * Returns the {@link MessageConfiguration} with the specified ID, or null if it isn't mapped.
     * @param ID ID of the message to return.
     * @return The requested message, or null if it isn't mapped.
     */
    public MessageConfiguration getMessage(long ID) {
        if(terminated) return null;
        synchronized(messageConfigurations) {
            return messageConfigurations.get(ID);
        }
    }

    /**
     * Adds a {@link MessageConfiguration} to the message map, if necessary.
     * @param ID ID of the message to add.
     * @return The {@link MessageConfiguration} that was added if necessary, or the value corresponding to the ID
     * if the message is already mapped.
     */
    public MessageConfiguration addMessage(long ID) {
        if(terminated) return null;
        synchronized(messageConfigurations) {
            var gotMC = messageConfigurations.get(ID);
            if(gotMC == null) {
                var message = new MessageConfiguration(ID, this);
                messageConfigurations.put(ID, message);
                return message;
            } else {
                return gotMC;
            }
        }
    }

    /**
     * Removes a {@link MessageConfiguration} from the message map.
     * @param ID ID of the message to remove.
     */
    public void removeMessage(long ID) {
        if(terminated) return;
        MessageConfiguration message;
        synchronized(messageConfigurations) {
            message = messageConfigurations.remove(ID);
            if(messageConfigurations.size() == 0) {
                getParent().removeChannel(getID());
                terminate();
            }
        }
        if(message != null) message.terminate();
    }

    /**
     * @return The {@link ChannelReactionFilter} for this instance.
     */
    public ChannelReactionFilter getReactionFilter() {
        return reactionFilter;
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        if(terminated) return;
        synchronized(messageConfigurations) {
            if(terminated) return;
            terminated = true;
            getParent().removeChannel(getID());
            messageConfigurations.forEach((ID, messageConfigurations) -> messageConfigurations.terminate());
            messageConfigurations.clear();
        }
    }
}
