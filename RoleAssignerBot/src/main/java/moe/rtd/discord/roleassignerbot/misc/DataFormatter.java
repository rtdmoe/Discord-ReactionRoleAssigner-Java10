package moe.rtd.discord.roleassignerbot.misc;

import moe.rtd.discord.roleassignerbot.config.Identifiable;
import moe.rtd.discord.roleassignerbot.config.IdentifiableChild;

import java.util.ArrayList;

/**
 * Static methods for formatting data so that it's more readable.
 * @author Big J
 */
public final class DataFormatter {

    /**
     * Formats the {@link Identifiable} so that it is more readable.
     * @param identifiable The identifiable to represent.
     * @return The formatted representation of the {@link Identifiable}.
     */
    public static String format(Identifiable identifiable) {
        var IDs = new ArrayList<Long>(3);
        var current = identifiable;

        while(current instanceof IdentifiableChild) {
            current = ((IdentifiableChild) current).getParent();
            IDs.add(current.getID());
        }

        var sb = new StringBuilder("[ ");

        for(int i = (IDs.size() - 1); i >= 0; i--) {
            sb.append(Long.toHexString(IDs.get(i)).toUpperCase());
            sb.append(" -> ");
        }

        sb.append(Long.toHexString(identifiable.getID()).toUpperCase());
        sb.append(" ]");
        return sb.toString();
    }
}
