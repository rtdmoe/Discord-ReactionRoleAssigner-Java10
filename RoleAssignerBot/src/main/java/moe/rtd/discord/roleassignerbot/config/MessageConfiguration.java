package moe.rtd.discord.roleassignerbot.config;

/**
 * Class responsible for one Discord message per instance.
 * Stores the TODO for the message it is responsible for.
 * @author Big J
 */
public class MessageConfiguration extends IdentifiableChild<ChannelConfiguration> implements Terminable {

    /**
     * Instantiates this {@link MessageConfiguration}; sets up the TODO.
     * @param ID The ID of the channel.
     * @param parent The channel that this message belongs to.
     */
    MessageConfiguration(long ID, ChannelConfiguration parent) {
        super(ID, parent);
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {
        // TODO
    }
}
