package moe.rtd.discord.roleassignerbot.command;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Interface for command objects.
 * @author Big J
 */
@FunctionalInterface
interface Command {

    /**
     * Checks and executes the command.
     * @return The result of the execution.
     * @throws CommandSyntaxException If there is a syntax error in the command.
     */
    String execute(MessageReceivedEvent messageReceivedEvent) throws CommandSyntaxException;
}
