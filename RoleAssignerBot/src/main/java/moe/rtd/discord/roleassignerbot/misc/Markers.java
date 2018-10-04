package moe.rtd.discord.roleassignerbot.misc;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Class containing all custom markers for this module.
 * @author Big J
 */
public final class Markers {
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
}
