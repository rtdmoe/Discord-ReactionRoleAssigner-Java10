package moe.rtd.discord.roleassignerbot.interfaces;

/**
 * Consumer interface for consumers that put things into queues.
 * @author Big J
 */
@FunctionalInterface
public interface QueueConsumer<T> {

    /**
     * Puts the input argument into the back of the queue.
     * @param t The input argument.
     * @throws InterruptedException If the current thread is interrupted when waiting for a free space in the queue.
     */
    void accept(T t) throws InterruptedException;
}
