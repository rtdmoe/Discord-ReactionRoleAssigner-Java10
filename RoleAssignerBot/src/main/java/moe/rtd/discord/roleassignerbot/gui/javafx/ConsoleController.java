package moe.rtd.discord.roleassignerbot.gui.javafx;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import netscape.javascript.JSException;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * JavaFX controller for the console tab.
 * @author Big J
 */
public class ConsoleController implements Runnable {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(ConsoleController.class);

    /**
     * WebView for displaying log messages.
     */
    @FXML private WebView webView;

    /**
     * Thread for appending messages to the WebView.
     */
    private final Thread thread;
    private volatile boolean running = true;

    /**
     * Stops a previous controller if present, and assigns the thread.
     */
    public ConsoleController() {
        synchronized(FXMain.lockConsole) {
            if(FXMain.console != null) FXMain.console.terminate();
            FXMain.console = this;
        }
        thread = new Thread(this);
        log.debug(Markers.JAVAFX, "Console controller constructed.");
    }

    /**
     * Initializes the controller.
     */
    @FXML private void initialize() {
        webView.getEngine().load(getClass().getResource("/res/javafx/webview/console.html").toExternalForm());
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                thread.start();
            }
        });
        log.debug(Markers.JAVAFX, "Console controller initialized.");
    }

    /**
     * Appends Log4j messages to the console.
     */
    @Override public void run() {
        while(running) {
            String message;
            try {
                message = FXMain.logMessageQueue.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn(Markers.JAVAFX, "WebView appender interrupted.");
                continue;
            }
            if(message != null) {
                Platform.runLater(() -> {
                    try {
                        String cmd = "append(\"" + StringEscapeUtils.escapeEcmaScript(message) + "\");";
                        webView.getEngine().executeScript(cmd);
                    } catch(JSException jse) {
                        log.error(Markers.JAVAFX, "JavaScript exception has occurred: ", jse);
                    }
                });

            }
        }
        log.info(Markers.JAVAFX, "WebView appender stopped.");
    }

    /**
     * Stops this instance's thread.
     */
    void terminate() {
        if(running) {
            synchronized(thread) {
                running = false;
                thread.interrupt();
            }
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while waiting for WebView appender to stop.", e);
        }
        log.info(Markers.JAVAFX, "Console controller terminated.");
    }
}
