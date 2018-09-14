package moe.rtd.discord.roleassignerbot.config;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class responsible for one Discord server per instance.
 * Stores all channel configuration for the server it is responsible for.
 * @author Big J
 */
public class ServerConfiguration extends Identifiable implements Terminable {

    /**
     * Map of all channels on this server that the bot is configured for.
     */
    private final Map<Long, ChannelConfiguration> channelConfigurations;
    /**
     * Whether or not this instance has been terminated.
     */
    private volatile boolean terminated = false;

    /**
     * Instantiates this {@link ServerConfiguration}; sets up the map.
     * @param ID The ID of the server.
     */
    ServerConfiguration(long ID) {
        super(ID);
        channelConfigurations = new WeakHashMap<>();
    }

    /**
     * Returns the {@link ChannelConfiguration} with the specified ID, or null if it isn't mapped.
     * @param ID ID of the channel to return.
     * @return The requested channel, or null if it isn't mapped.
     */
    public ChannelConfiguration getChannel(long ID) {
        if(terminated) return null;
        synchronized(channelConfigurations) {
            return channelConfigurations.get(ID);
        }
    }

    /**
     * Adds a {@link ChannelConfiguration} to the channel map, if necessary.
     * @param ID ID of the channel to add.
     * @return The {@link ChannelConfiguration} that was added if necessary, or the value corresponding to the ID
     * if the channel is already mapped.
     */
    public ChannelConfiguration addChannel(long ID) {
        if(terminated) return null;
        synchronized(channelConfigurations) {
            var gotCC = channelConfigurations.get(ID);
            if(gotCC == null) {
                var channel = new ChannelConfiguration(ID, this);
                channelConfigurations.put(ID, channel);
                return channel;
            } else {
                return gotCC;
            }
        }
    }

    /**
     * Removes a {@link ChannelConfiguration} from the channel map.
     * @param ID ID of the channel to remove.
     */
    public void removeChannel(long ID) {
        if(terminated) return;
        ChannelConfiguration channel;
        synchronized(channelConfigurations) {
            channel = channelConfigurations.remove(ID);
            if(channelConfigurations.size() == 0) {
                BotSettings.removeServer(getID());
                terminate();
            }
        }
        if(channel != null) channel.terminate();
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        if(terminated) return;
        synchronized(channelConfigurations) {
            if(terminated) return;
            terminated = true;
            BotSettings.removeServer(getID());
            channelConfigurations.forEach((ID, channelConfiguration) -> channelConfiguration.terminate());
            channelConfigurations.clear();
        }
    }
}
