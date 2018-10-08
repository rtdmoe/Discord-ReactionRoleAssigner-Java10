package moe.rtd.discord.roleassignerbot.gui.javafx;

import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * Class for queueing messages to be added to the console tab WebView in the GUI.
 * @author Big J
 */
@SuppressWarnings("unused")
@Plugin(name = "WebViewAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public final class WebViewAppender extends AbstractAppender {

    private WebViewAppender(String name, Filter filter, Layout layout) {
        super(name, filter, (layout == null) ? PatternLayout.createDefaultLayout() : layout, false);
    }

    @PluginFactory
    public static WebViewAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filters") Filter filter,
            @PluginElement("Layout") Layout layout
    ) {
        return new WebViewAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent logEvent) {
        Layout<? extends Serializable> layout = getLayout();
        try {
            FXMain.logMessageQueue.put((layout == null) ? logEvent.getMessage().getFormattedMessage() : layout.toSerializable(logEvent).toString());
        } catch (InterruptedException e) {
            LogManager.getLogger(WebViewAppender.class).error(Markers.JAVAFX, "Thread interrupted while waiting to queue message.", e);
        }
    }
}
