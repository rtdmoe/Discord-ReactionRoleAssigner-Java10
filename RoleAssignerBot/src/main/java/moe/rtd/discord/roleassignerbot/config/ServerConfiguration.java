package moe.rtd.discord.roleassignerbot.config;

/**
 * Class responsible for one Discord server per instance.
 * Stores all channel configuration for the server it is responsible for.
 * @author Big J
 */
public class ServerConfiguration extends Identifiable implements Terminable {



    /**
     * TODO
     * @param ID
     */
    ServerConfiguration(long ID) {
        super(ID);
    }

    /**
     * @see Terminable#terminate()
     */
    @Override
    public void terminate() {

    }
}
