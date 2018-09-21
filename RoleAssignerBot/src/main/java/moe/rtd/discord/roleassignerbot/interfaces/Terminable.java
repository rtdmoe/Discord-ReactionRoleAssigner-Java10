package moe.rtd.discord.roleassignerbot.interfaces;

/**
 * Similar to {@link java.io.Closeable}, but for non-I/O resources i.e. {@link Thread}s and memory.
 * @author Big J
 */
@FunctionalInterface
public interface Terminable {
    /**
     * Terminates {@code this}; Terminates any {@link Terminable} members and {@link Thread}s
     * that this instance is responsible for.
     */
    void terminate();
}
