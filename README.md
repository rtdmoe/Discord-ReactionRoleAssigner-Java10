# Role Assignment Discord Bot

This is a Discord bot which can be configured to manage roles based on reactions to a specific message.
The configuration is very simple and consists of 8 commands which are listed and described below.

## Commands:

**NOTE #1: Commands must mention the bot user either on the first line or on a seperate line at the end of the message.**  
**NOTE #2: Commands can only be used by administrators or users with the authorized role for the server.**

* `authorize [@role]`<br>
Sets the current server's authorized role to the mentioned role. Can only be used by administrators.
* `help`<br>
Sends the list of commands via a private text channel to the command user.
* `list < all | [#channel] >`<br>
Lists all configured messages for either this server, or the mentioned channel, depending on the used parameter.
* `show [ID] < in [#channel] >`<br>
Shows the configuration for the message with the specified ID, if it exists, in either the current channel, or the optionally mentioned channel.

* `add [ID] < in [#channel] >`<br>
Adds a new, blank configuration for the specified message, in either the current channel, or the optionally mentioned channel.
* `remove [ID] < in [#channel] >`<br>
Removes the configuration for the specified message, if it exists, in either the current channel, or the optionally mentioned channel.

* `configure [ID] < in [#channel] > \n { [@role] to [:emote:] }`<br>
Maps the specified role(s) to the corresponding emotes in the configuration for the specified message in either the current channel, or the optionally mentioned channel.
* `deconfigure [ID] < in [#channel] > \n { [@role] }`<br>
Removes the mapping for the specified role(s) in the configuration for the specified message in either the current channel, or the optionally mentioned channel.

#### Key:

**NOTE: Extra spaces (more than one in a row, except for the first) can be ignored, as well as spaces at the beginning and end of each line.**

* `[description]`: Replace (including the brackets) with what it's referring to.
* `< parameter >`: Optional parameter.
* `< parameter1 | parameter2 >`: Use only one of the "`|`" character seperated parameters.
* `{ parameter }`: List of any amount of new line seperated parameters.
* `\n`: Replace with a new line.

### Miscellanous Information

The reaction event handling and configuration data for this program is multi-threaded and structured
in a way so that it increases efficiency with multiple configured messages on multiple channels on multiple servers,
especially at high load.

The command handling, however, is single threaded and not seperated for each server or channel,
because commands shouldn't have to be used very often, and it would be less efficient to actually do so,
because it would take up more memory and processing power.
