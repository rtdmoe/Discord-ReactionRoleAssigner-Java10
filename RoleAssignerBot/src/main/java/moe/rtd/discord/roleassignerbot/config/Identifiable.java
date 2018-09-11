package moe.rtd.discord.roleassignerbot.config;

/**
 * Superclass for classes with unique identifiers.
 * @author Big J
 */
public class Identifiable {

    /**
     * The unique identifier of this instance.
     */
    private final long ID;

    /**
     * Creates a new instance with the unique identifier {@code ID}.
     * @param ID Unique identifier to instantiate this instance with.
     */
    Identifiable(long ID) {
        this.ID = ID;
    }

    /**
     * @return The unique identifier.
     */
    public long getID() {
        return ID;
    }
}
