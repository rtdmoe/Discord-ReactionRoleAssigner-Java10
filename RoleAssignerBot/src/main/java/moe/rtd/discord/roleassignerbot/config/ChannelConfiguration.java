package moe.rtd.discord.roleassignerbot.config;

import java.util.Map;
import java.util.WeakHashMap;

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
     * Instantiates this {@link ChannelConfiguration}; sets up the map.
     * @param ID The ID of the channel.
     * @param parent The server that this channel belongs to.
     */
    ChannelConfiguration(long ID, ServerConfiguration parent) {
        super(ID, parent);
        messageConfigurations = new WeakHashMap<>();
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        synchronized(messageConfigurations) {
            messageConfigurations.forEach((ID, messageConfigurations) -> messageConfigurations.terminate());
            messageConfigurations.clear();
        }
    }
}
