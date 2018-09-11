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
     * Instantiates this {@link ServerConfiguration}; sets up the map.
     * @param ID The ID of the server.
     */
    ServerConfiguration(long ID) {
        super(ID);
        channelConfigurations = new WeakHashMap<>();
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        synchronized(channelConfigurations) {
            channelConfigurations.forEach((ID, channelConfiguration) -> channelConfiguration.terminate());
            channelConfigurations.clear();
        }
    }
}
