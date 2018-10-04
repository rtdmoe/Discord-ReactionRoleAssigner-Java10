package moe.rtd.discord.roleassignerbot.gui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import moe.rtd.discord.roleassignerbot.Main;
import moe.rtd.discord.roleassignerbot.misc.MiscConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class for the JavaFX GUI.
 * @author Big J
 */
public class FXMain extends Application {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(FXMain.class);

    /**
     * Whether or not JavaFX has finished setting up.
     */
    private static final AtomicBoolean setup = new AtomicBoolean(false);

    /**
     * Object for waiting on until JavaFX has finished setting up.
     */
    private static final Object waitForSetup = new Object();

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        
        log.trace("Adding JavaFX crash hook and shutdown hook.");

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            RuntimeException e = new RuntimeException("JavaFX has crashed.", throwable);
            log.fatal(e);
            Main.exit(-2);
        });
        
        log.trace("Added JavaFX crash hook and shutdown hook.");
        log.debug("Setting up GUI...");

        primaryStage.setTitle(MiscConstants.TITLE);
        primaryStage.setOnCloseRequest((we) -> {
            we.consume();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(MiscConstants.TITLE);
            alert.setHeaderText("Shutting down...");
            alert.setContentText("Are you sure you want to shut down this bot?");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent()) {
                if(result.get() == ButtonType.OK) {
                    Main.exit(0);
                }
            }
        });

        FXMLLoader window = new FXMLLoader(FXMain.class.getResource("/res/fx/window.fxml"));
        Scene scene = new Scene(window.load());
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(340);
        primaryStage.setWidth(640);
        primaryStage.setHeight(340);

        primaryStage.getIcons().add(new Image(FXMain.class.getResourceAsStream("/res/fx/icon.jpg")));

        primaryStage.show();
        setup.set(true);
        synchronized(waitForSetup) {
            waitForSetup.notifyAll();
        }

        log.info("GUI setup complete.");
    }

    @Override
    public void stop() {
        log.info("JavaFX stopped.");
    }

    /**
     * Wait for JavaFX to finish setting up.
     */
    public static void waitForSetup() {
        while(!setup.get()) {
            synchronized(waitForSetup) {
                try {
                    waitForSetup.wait(1000);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
    }
}
