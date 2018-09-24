package moe.rtd.discord.roleassignerbot.command;

import javafx.util.Pair;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.config.MessageConfiguration;
import moe.rtd.discord.roleassignerbot.config.ServerConfiguration;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * List of all commands.
 * @author Big J
 */
@SuppressWarnings("unused")
enum Commands {
    AUTHORIZE("(authorize )((<@&)[0-9]+(>))", "authorize [@role]", e -> {
        if(e.getAuthor().getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR)) {
            BotSettings.addServer(e.getGuild().getLongID()).setProperty(
                            ServerConfiguration.Properties.AUTHORIZED_ROLE,
                            e.getMessage().getRoleMentions().get(0).getLongID());
            return "Successfully set authorized role to " + e.getMessage().getRoleMentions().get(0).mention();
        } else return null;
    }),
    HELP("(help)", "help", e -> {
        e.getAuthor().getOrCreatePMChannel()
                .sendMessage("https://github.com/rtdmoe/DiscordBot-Role-Assigner/blob/master/README.md");
        return "Help has been sent privately.";
    }),
    LIST("(list )((all)|((<#)[0-9]+(>)))", "list < all | [#channel] >", e -> {
        String[] keywords = getMessageCommand(e).split("\\s");
        switch(keywords[1]) {
            case "all":
                {
                    var c = e.getMessage().getChannel();
                    var sc = BotSettings.getServer(c.getGuild().getLongID());
                    if(sc == null) return "No configured messages for this server.";

                    StringBuilder sb = new StringBuilder("List of configured messages in this server:");
                    sb.append("\n```\n");

                    sc.forEach((cID, cc) -> cc.forEach((mID, mc) -> sb.append(mc.getID()).append('\n')));

                    sb.append("```");
                    return sb.toString();
                }
            default:
                {
                    if(Pattern.compile("(<#)[0-9]+(>)").matcher(keywords[1]).matches()) {
                        var c = e.getMessage().getChannelMentions().get(0);
                        var sc = BotSettings.getServer(c.getGuild().getLongID());
                        if(sc == null) return "No configured messages for " +
                                e.getMessage().getChannelMentions().get(0).mention();
                        var cc = sc.getChannel(c.getLongID());
                        if(cc == null) return "No configured messages for " +
                                e.getMessage().getChannelMentions().get(0).mention();
                        StringBuilder sb = new StringBuilder("List of configured messages in " +
                                e.getMessage().getChannelMentions().get(0).mention());
                        sb.append("\n```\n");

                        cc.forEach((ID, mc) -> {
                            sb.append(mc.getID());
                            sb.append('\n');
                        });

                        sb.append("```");
                        return sb.toString();
                    } else throw new RuntimeException("Unchecked syntax error.");
                }
        }
    }),
    SHOW("(show )([0-9]+)( in (<#)[0-9]+(>))?", "show [ID] < in [#channel] >", e -> {
        var m = getMessageCommand(e);
        String[] keywords = getMessageCommand(e).split("\\s");
        var ID = Long.parseUnsignedLong(keywords[1]);
        IChannel c;
        if(m.contains("in")) {
            c = e.getGuild().getChannelByID(Long.parseUnsignedLong(keywords[3]));
        } else {
            c = e.getChannel();
        }
        var sc = BotSettings.getServer(e.getGuild().getLongID());
        if(sc == null) return "No configured messages for this server.";
        var cc = sc.getChannel(c.getLongID());
        if(cc == null) return "No configured messages for the specified channel server.";
        var mc = cc.getMessage(ID);
        if(mc == null) return "The specified message is not configured.";
        StringBuilder sb = new StringBuilder("Configuration for " + ID + " in " + c.mention());
        sb.append("\n```\n");

        mc.forEach((role, emote) -> {
            sb.append(e.getGuild().getRoleByID(role).mention());
            sb.append(" to ");
            sb.append(emote);
            sb.append('\n');
        });

        sb.append("```");
        return sb.toString();
    }),


    ADD("(add )([0-9]+)( in (<#)[0-9]+(>))?", "add [ID] < in [#channel] >", e -> {
        var m = getMessageCommand(e);
        String[] keywords = getMessageCommand(e).split("\\s");
        var ID = Long.parseUnsignedLong(keywords[1]);
        IChannel c;
        if(m.contains("in")) {
            c = e.getGuild().getChannelByID(Long.parseUnsignedLong(keywords[3]));
        } else {
            c = e.getChannel();
        }
        BotSettings.addServer(e.getGuild().getLongID()).addChannel(c.getLongID()).addMessage(ID);
        return "Successfully added configuration for " + ID + " in " + c.mention();
    }),
    REMOVE("(remove )([0-9]+)( in (<#)[0-9]+(>))?", "remove [ID] < in [#channel] >", e -> {
        var m = getMessageCommand(e);
        String[] keywords = getMessageCommand(e).split("\\s");
        var ID = Long.parseUnsignedLong(keywords[1]);
        IChannel c;
        if(m.contains("in")) {
            c = e.getGuild().getChannelByID(Long.parseUnsignedLong(keywords[3]));
        } else {
            c = e.getChannel();
        }
        var sc = BotSettings.getServer(e.getGuild().getLongID());
        if(sc == null) return "No configured messages for this server.";
        var cc = sc.getChannel(c.getLongID());
        if(cc == null) return "No configured messages for the specified channel server.";
        var mc = cc.getMessage(ID);
        if(mc == null) return "The specified message is not configured.";
        mc.terminate();
        return "Successfully removed configuration for " + ID + " in " + c.mention();
    }),


    CONFIGURE("(configure )([0-9]+)( in (<#)[0-9]+(>))?((\n<@&)[0-9]+(> to )(((<(a)?:)[a-zA-Z0-9_]+(:)[0-9]+(>))|(.)))+",
            "configure [ID] < in [#channel] > \\n { [@role] to [:emote:] }", e -> {
        MessageConfiguration mc;
        String[] lines = getMessageCommand(e).split("\\n");
        IChannel c;
        {
            var m = getMessageCommand(e);
            String[] keywords = lines[0].split("\\s");
            var ID = Long.parseUnsignedLong(keywords[1]);
            if(m.contains("in")) {
                c = e.getGuild().getChannelByID(Long.parseUnsignedLong(keywords[3]));
            } else {
                c = e.getChannel();
            }
            var sc = BotSettings.getServer(e.getGuild().getLongID());
            if(sc == null) return "No configured messages for this server.";
            var cc = sc.getChannel(c.getLongID());
            if(cc == null) return "No configured messages for the specified channel server.";
            mc = cc.getMessage(ID);
            if(mc == null) return "The specified message is not configured.";
        }

        for(int i = 1; i < lines.length; i++) {
            var p = getRoleEmotePair(lines[i]);
            mc.setRole(p.getKey(), p.getValue());
        }

        return "Successfully updated configuration for " + mc.getID() + " in " + c.mention();
    }),
    DECONFIGURE("(deconfigure )([0-9]+)( in (<#)[0-9]+(>))?((\n<@&)[0-9]+(>))+",
            "deconfigure [ID] < in [#channel] > \\n { [@role] }", e -> {
        MessageConfiguration mc;
        String[] lines = getMessageCommand(e).split("\\n");
        IChannel c;
        {
            var m = getMessageCommand(e);
            String[] keywords = lines[0].split("\\s");
            var ID = Long.parseUnsignedLong(keywords[1]);
            if(m.contains("in")) {
                c = e.getGuild().getChannelByID(Long.parseUnsignedLong(keywords[3]));
            } else {
                c = e.getChannel();
            }
            var sc = BotSettings.getServer(e.getGuild().getLongID());
            if(sc == null) return "No configured messages for this server.";
            var cc = sc.getChannel(c.getLongID());
            if(cc == null) return "No configured messages for the specified channel server.";
            mc = cc.getMessage(ID);
            if(mc == null) return "The specified message is not configured.";
        }

        for(int i = 1; i < lines.length; i++) {
            mc.removeRole(getRole(lines[i]));
        }

        return "Successfully updated configuration for " + mc.getID() + " in " + c.mention();
    });

    /**
     * The command which corresponds to this instance.
     */
    private final Command command;

    /**
     * Regular expression for checking this instance.
     */
    private final Pattern regex;

    /**
     * User readable syntax for this command.
     */
    private final String syntax;

    Commands(String regex, String syntax, Command command) {
        this.regex = Pattern.compile(regex);
        this.syntax = syntax;
        this.command = command;
    }

    /**
     * @param e The message event to extract the command from.
     * @return The extracted command.
     */
    private static String getMessageCommand(MessageReceivedEvent e) {
        var a = e.getMessage().getContent().replaceAll("(<@)[0-9]+(>)", "").split("\n");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < a.length; i++) {
            while(a[i].startsWith(" ") || a[i].startsWith("\n")) a[i] = a[i].substring(1);
            while(a[i].endsWith(" ") || a[i].endsWith("\n")) a[i] = a[i].substring(0, a[i].length() - 1);
            if(i != 0) sb.append('\n');
            sb.append(a[i]);
        }
        return sb.toString();
    }

    /**
     * @param line The line to parse.
     * @return The parsed pair of parameters.
     */
    private static Pair<Long, String> getRoleEmotePair(String line) {
        String[] words = line.split("\\s");
        String s;
        if(Pattern.compile("((.)|((<(a)?:)[a-zA-Z0-9_]+(:)[0-9]+(>)))").matcher(words[2]).matches()) {
            s = words[2];
        } else throw new RuntimeException("Unchecked syntax error.");
        return new Pair<>(getRole(words[0]), s);
    }

    /**
     * @param line The line to parse.
     * @return The parsed role parameter.
     */
    private static Long getRole(String line) {
        var s = line.replace("<@&", "").replace(">", "");
        return Long.parseUnsignedLong(s);
    }

    /**
     * Checks the syntax of a command.
     * @param command The command to check.
     */
    private void checkSyntax(String command) throws CommandSyntaxException {
        Matcher m = regex.matcher(command);
        if(!m.matches()) {
            throw new CommandSyntaxException("Does not match `" + syntax + "`");
        }
    }

    /**
     * Executes this command.
     * @return The output of running this command.
     */
    String execute(MessageReceivedEvent e) throws CommandSyntaxException {
        var s = getMessageCommand(e);
        System.out.println("Executing command \"" + s + "\"."); // TODO replace with log4j
        checkSyntax(s);
        return getCommand().execute(e);
    }

    /**
     * @return The command which the {@link Commands#name} corresponds to.
     */
    private Command getCommand() {
        return command;
    }
}
