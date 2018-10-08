package moe.rtd.discord.roleassignerbot.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import moe.rtd.discord.roleassignerbot.gui.javafx.FXMain;
import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Main class for the graphical user interface for this program.
 * @author Big J
 */
public class GUI {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(GUI.class);

    /**
     * JavaFX launcher thread.
     */
    private static final Thread fxThread = new Thread(FXMain::main);

    /**
     * Sets up the GUI.
     */
    public static void setup() {
        log.debug(Markers.GUI, "Starting GUI.");

        fxThread.setName("JavaFX-Launcher-Thread");
        fxThread.start();

        FXMain.waitForSetup();

        log.info(Markers.GUI, "GUI setup complete.");
    }

    /**
     * Closes the GUI.
     */
    public static void close() {
        log.debug(Markers.GUI, "Closing GUI.");

        Platform.exit();

        log.info(Markers.GUI, "GUI closed.");
    }

    /**
     * Object for synchronizing user text input requests.
     */
    private static final Object requestUserInputLock = new Object();
    /**
     * Dialog for requesting user input.
     */
    private static TextInputDialog tid = null;
    /**
     * Reads a single line of user text input.
     * @param request The name of the input requested from the user.
     * @return The user input, or null if the program is closing.
     */
    public static String requestUserInput(String request) {
        log.debug(Markers.GUI, "Queuing for user to input \"" + request + "\".");
        synchronized(requestUserInputLock) {
            log.info(Markers.GUI, "Requesting user to input \"" + request + "\".");

            if(tid == null) {
                Platform.runLater(() -> {
                    tid = new TextInputDialog();
                    tid.setHeaderText("User input required.");
                    tid.setTitle(MiscConstants.TITLE);
                    ((Stage) tid.getDialogPane().getScene().getWindow()).getIcons().add(new Image(FXMain.class.getResourceAsStream("/res/javafx/icon.jpg")));
                });
            }

            FutureTask<Optional<String>> future = new FutureTask<>(() -> {
                tid.setContentText("Please enter " + request + ": ");
                tid.getEditor().setText("");
                return tid.showAndWait();
            });

            Platform.runLater(future);
            Optional<String> result = Optional.empty();
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.fatal(Markers.GUI, "Error occurred while waiting for the user to close the dialog.", e);
            }

            log.debug(Markers.GUI, "User input entered: \"" + result.orElse("null") + "\" for \"" + request + "\".");

            return result.orElse(null);
        }
    }

    /**
     * Shows a JavaFX dialog to the user.
     * @param type The Alert type.
     * @param title The title of the dialog.
     * @param header The message in the header of the dialog.
     * @param content The message in the content of the dialog.
     * @param buttonTypes Optional custom button types that will replace the default button types if present.
     * @return The button pressed by the user.
     * @throws InterruptedException If the thread is interrupted while waiting for the user to
     */
    public static Optional<ButtonType> showDialog(Alert.AlertType type, String title, String header, String content, ButtonType... buttonTypes) throws InterruptedException {
        log.info(Markers.GUI, "Showing dialog: " + content);

        FutureTask<Optional<ButtonType>> future = new FutureTask<>(() -> {
            Alert a = new Alert(type);
            ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(new Image(FXMain.class.getResourceAsStream("/res/javafx/icon.jpg")));
            a.setTitle(title);
            a.setHeaderText(header);
            a.setContentText(content);
            if(!(buttonTypes == null || buttonTypes.length == 0)) {
                a.getButtonTypes().clear();
                a.getButtonTypes().addAll(buttonTypes);
            }
            return a.showAndWait();
        });

        Platform.runLater(future);
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException("Execution exception in dialog.", e);
        }
    }
}
