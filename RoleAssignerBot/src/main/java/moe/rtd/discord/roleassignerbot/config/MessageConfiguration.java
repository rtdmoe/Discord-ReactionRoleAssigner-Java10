package moe.rtd.discord.roleassignerbot.config;

import moe.rtd.discord.roleassignerbot.interfaces.Terminable;
import moe.rtd.discord.roleassignerbot.reaction.ReactionHandler;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * Class responsible for one Discord message per instance.
 * Stores the reaction handler and configuration for the message it is responsible for.
 * @author Big J
 */
public class MessageConfiguration extends IdentifiableChild<ChannelConfiguration> implements Terminable {

    /**
     * @see ReactionHandler
     */
    private final ReactionHandler reactionHandler;
    /**
     * Whether or not this instance has been terminated.
     */
    private volatile boolean terminated = false;

    /**
     * Map of role IDs to emote IDs that this message is configured for.<br>
     * The role is the key, the emote is the value.
     */
    private final Map<Long, String> configuration;

    /**
     * Instantiates this {@link MessageConfiguration}; sets up the reaction handler.
     * @param ID The ID of the channel.
     * @param parent The channel that this message belongs to.
     */
    MessageConfiguration(long ID, ChannelConfiguration parent) {
        super(ID, parent);
        configuration = new TreeMap<>();
        reactionHandler = new ReactionHandler(this);
    }

    /**
     * Returns a deep clone of the map.
     * <p><b>WARNING: TIME CONSUMING MAP BLOCKING METHOD!</b></p>
     */
    public Map<Long, String> getConfiguration() {
        if(terminated) return null;
        synchronized(configuration) {
            if(terminated) return null;
            return new TreeMap<>(configuration);
        }
    }

    /**
     * @param ROLE The role to search for.
     * @return The emote that the role is bound to.
     */
    public String getEmote(long ROLE) {
        if(terminated) return null;
        synchronized(configuration) {
            if(terminated) return null;
            return configuration.get(ROLE);
        }
    }

    /**
     * Returns the role that is bound to the emote {@code EMOTE}, or null if such a role doesn't exist.
     * <p><b>WARNING: TIME CONSUMING MAP BLOCKING METHOD!</b></p>
     * @param EMOTE The emote to search for.
     * @return The role which is bound to the emote.
     */
    public Long getRole(String EMOTE) {
        if(terminated) return null;
        synchronized(configuration) {
            if(terminated) return null;
            for(var entry : configuration.entrySet()) if(entry.getValue().equals(EMOTE)) return entry.getKey();
            return null;
        }
    }

    /**
     * Binds the role to an emote.
     * @param ROLE The role to bind (The key).
     * @param EMOTE The emote to bind it to (The value).
     */
    public void setRole(long ROLE, String EMOTE) {
        if(terminated) return;
        synchronized(configuration) {
            if(terminated) return;
            configuration.put(ROLE, EMOTE);
        }
    }

    /**
     * Removes a role from the map.
     * @param ROLE The role to remove.
     */
    public void removeRole(long ROLE) {
        if(terminated) return;
        synchronized(configuration) {
            if(terminated) return;
            configuration.remove(ROLE);
        }
    }

    /**
     * @param ROLE The role to check.
     * @return Whether or not the role is mapped.
     */
    public boolean isMapped(long ROLE) {
        if(terminated) return false;
        synchronized(configuration) {
            if(terminated) return false;
            return configuration.get(ROLE) != null;
        }
    }

    /**
     * @param EMOTE The emote to search for.
     * @return Whether or not the emote is already used in the map.
     */
    public boolean isUsed(String EMOTE) {
        if(terminated) return false;
        synchronized(configuration) {
            if(terminated) return false;
            return configuration.containsValue(EMOTE);
        }
    }

    /**
     * @see Map#forEach(BiConsumer)
     */
    public void forEach(BiConsumer<Long, String> action) {
        synchronized(configuration) {
            configuration.forEach(action);
        }
    }

    /**
     * @return The reaction handler which handles reactions for this message.
     */
    public ReactionHandler getReactionHandler() {
        return reactionHandler;
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        if(terminated) return;
        synchronized(reactionHandler) {
            if(terminated) return;
            terminated = true;
            getParent().removeMessage(getID());
            reactionHandler.terminate();
        }
        synchronized(configuration) {
            configuration.clear();
        }
    }
}
