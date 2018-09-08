package moe.rtd.discord.roleassignerbot.config;

/**
 * Class responsible for one Discord channel per instance.
 * Stores all message handlers for the channel it is responsible for.
 * @author Big J
 */
public class ChannelConfiguration extends Identifiable implements Terminable {



    /**
     * TODO
     * @param ID
     */
    ChannelConfiguration(long ID) {
        super(ID);
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {

    }
}
