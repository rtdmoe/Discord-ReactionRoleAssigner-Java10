package moe.rtd.discord.roleassignerbot.gui;

import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Main class for the graphical user interface for this program.
 * @author Big J
 */
public class GUI {

    /**
     * Log4j2 Logger for this class.
     */
    private static Logger log = LogManager.getLogger(GUI.class);

    /**
     * Sets up the GUI.
     */
    public static void setup() {
        // TODO
    }

    /**
     * Closes the GUI.
     */
    public static void close() {
        // TODO
    }

    /**
     * Object for synchronizing user text input requests.
     */
    private static final Object requestUserInputLock = new Object();
    /**
     * Reads a single line of user text input.
     * @return The user input, or null if the program is closing.
     */
    public static String requestUserInput(String request) {
        log.debug("Queuing for user to input \"" + request + "\".");
        synchronized(requestUserInputLock) {
            log.info("Requesting user to input \"" + request + "\".");
            return JOptionPane.showInputDialog(
                    null,
                    "Enter " + request + ":",
                    MiscConstants.TITLE + " - User Input Request",
                    JOptionPane.QUESTION_MESSAGE); // TODO convert to JavaFX
        }
    }
}
