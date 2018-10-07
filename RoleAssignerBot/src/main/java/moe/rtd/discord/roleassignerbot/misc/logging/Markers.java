package moe.rtd.discord.roleassignerbot.misc.logging;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Class containing all custom markers for this module.
 * @author Big J
 */
public final class Markers {
    /**
     * @see moe.rtd.discord.roleassignerbot
     */
    private static final Marker ROLE_ASSIGNER_BOT = MarkerManager.getMarker("ROLE_ASSIGNER_BOT");

    /**
     * @see moe.rtd.discord.roleassignerbot.Main
     */
    public static final Marker MAIN = MarkerManager.getMarker("RAB_MAIN");

    /**
     * @see moe.rtd.discord.roleassignerbot.command
     */
    public static final Marker COMMAND = MarkerManager.getMarker("RAB_COMMAND");

    /**
     * @see moe.rtd.discord.roleassignerbot.config
     */
    public static final Marker CONFIG = MarkerManager.getMarker("RAB_CONFIG");

    /**
     * @see moe.rtd.discord.roleassignerbot.discord
     */
    public static final Marker DISCORD = MarkerManager.getMarker("RAB_DISCORD");

    /**
     * @see moe.rtd.discord.roleassignerbot.gui
     */
    public static final Marker GUI = MarkerManager.getMarker("RAB_GUI");

    /**
     * @see moe.rtd.discord.roleassignerbot.reactions
     */
    public static final Marker REACTIONS = MarkerManager.getMarker("RAB_REACTIONS");

    /**
     * @see moe.rtd.discord.roleassignerbot.gui.javafx
     */
    public static final Marker JAVAFX = MarkerManager.getMarker("RAB_GUI_JAVAFX");

    static { // Create a marker hierarchy to resemble the package structure.
        MAIN.addParents(ROLE_ASSIGNER_BOT);
        COMMAND.addParents(ROLE_ASSIGNER_BOT);
        CONFIG.addParents(ROLE_ASSIGNER_BOT);
        DISCORD.addParents(ROLE_ASSIGNER_BOT);
        GUI.addParents(ROLE_ASSIGNER_BOT);
        REACTIONS.addParents(ROLE_ASSIGNER_BOT);

        JAVAFX.addParents(ROLE_ASSIGNER_BOT, GUI);
    }
}
