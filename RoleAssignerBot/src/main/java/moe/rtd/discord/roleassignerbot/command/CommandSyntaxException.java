package moe.rtd.discord.roleassignerbot.command;

/**
 * Exception thrown when there is a syntax error in a command.
 * @author Big J
 */
class CommandSyntaxException extends Exception {

    /**
     * @param message The detail message.
     * @see Exception#Exception(String)
     */
    CommandSyntaxException(String message) {
        super(message);
    }
}
