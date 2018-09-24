package moe.rtd.discord.roleassignerbot.config;

import javafx.util.Pair;
import moe.rtd.discord.roleassignerbot.filter.ServerReactionFilter;
import moe.rtd.discord.roleassignerbot.interfaces.Terminable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Class responsible for one Discord server per instance.
 * Stores all channel configuration for the server it is responsible for.
 * Also stores all properties for this server.
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
     * The reaction event filter for this instance.
     */
    private final ServerReactionFilter reactionFilter;

    /**
     * Map of all of the bot properties for this server.
     */
    private final Map<Properties, Serializable> properties;

    /**
     * Enum containing all properties and their default values.
     */
    public enum Properties {
        AUTHORIZED_ROLE(0L);

        /**
         * The default value of this property.
         */
        private final Serializable defaultValue;

        /**
         * @param defaultValue The default value of this property.
         */
        Properties(Serializable defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * @return The default value for this property.
         */
        public Serializable getDefaultValue() {
            return defaultValue;
        }
    }

    /**
     * Instantiates this {@link ServerConfiguration}; sets up the map.
     * @param ID The ID of the server.
     */
    ServerConfiguration(long ID) {
        super(ID);
        this.channelConfigurations = new TreeMap<>();
        this.properties = new TreeMap<>(Arrays.stream(Properties.values()).collect(Collectors.toMap(
                e -> e,
                Properties::getDefaultValue)));
        this.reactionFilter = new ServerReactionFilter(this);
    }

    /**
     * @return The value of the property with the key {@code K}.
     */
    public Serializable getProperty(Properties K) {
        synchronized(properties) {
            return properties.get(K);
        }
    }

    /**
     * Sets the value of the property with the key {@code K} to {@code V}.
     * @throws NullPointerException If the key is null.
     * @throws IllegalArgumentException If the .
     */
    public void setProperty(Properties K, Serializable V) {
        if(K == null) throw new NullPointerException("The key cannot be null.");
        if(V.getClass().isPrimitive()) {
            if(!K.getDefaultValue().getClass().isPrimitive())
                throw new IllegalArgumentException("Wrong value type for this property.");
        } else if(!(K.getDefaultValue().getClass().isAssignableFrom(V.getClass())))
            throw new IllegalArgumentException("Wrong value type for this property.");
        synchronized(properties) {
            properties.put(K, K.getDefaultValue().getClass().cast(V));
        }
    }

    /**
     * @see Map#forEach(BiConsumer)
     */
    void forEachProperty(BiConsumer<Properties, Serializable> action) {
        synchronized(properties) {
            properties.forEach(action);
        }
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
        end: synchronized(channelConfigurations) {
            channel = channelConfigurations.remove(ID);
            if(channelConfigurations.size() == 0) {
                synchronized(properties) {
                    for(Map.Entry<Properties, Serializable> e : properties.entrySet()) {
                        if(!e.getValue().equals(e.getKey().getDefaultValue())) {
                            break end;
                        }
                    }
                    BotSettings.removeServer(getID());
                    terminate();
                }
            }
        }
        if(channel != null) channel.terminate();
    }

    /**
     * @see Map#forEach(BiConsumer)
     */
    public void forEach(BiConsumer<? super Long, ? super ChannelConfiguration> action) {
        synchronized(channelConfigurations) {
            channelConfigurations.forEach(action);
        }
    }

    /**
     * @return The {@link ServerReactionFilter} for this instance.
     */
    public ServerReactionFilter getReactionFilter() {
        return reactionFilter;
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
            reactionFilter.terminate();
            channelConfigurations.forEach((ID, channelConfiguration) -> channelConfiguration.terminate());
            channelConfigurations.clear();
        }
    }
}
