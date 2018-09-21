package moe.rtd.discord.roleassignerbot.command;

/**
 * List of all commands.
 * @author Big J
 */
enum Commands {
    ;

    /**
     * The command which corresponds to this instance.
     */
    private final Command command;

    Commands(Command command) {
        this.command = command;
    }

    /**
     * @return The command which the {@link Commands#name} corresponds to.
     */
    public Command getCommand() {
        return command;
    }
}
