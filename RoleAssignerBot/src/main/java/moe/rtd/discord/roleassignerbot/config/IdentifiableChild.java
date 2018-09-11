package moe.rtd.discord.roleassignerbot.config;

/**
 * Superclass for classes with unique identifiers and parent objects, which also have unique identifiers.
 * @param <T> The instance's parent object type.
 * @author Big J
 */
public class IdentifiableChild<T extends Identifiable> extends Identifiable {

    /**
     * The parent object of this instance.
     */
    private final T parent;

    /**
     * Creates a new instance with the unique identifier {@code ID} and parent {@code parent}.
     * @param ID Unique identifier to instantiate this instance with.
     * @param parent Parent object reference to instantiate this instance with.
     */
    IdentifiableChild(long ID, T parent) {
        super(ID);
        this.parent = parent;
    }

    /**
     * @return The parent object reference.
     */
    public T getParent() {
        return parent;
    }
}
