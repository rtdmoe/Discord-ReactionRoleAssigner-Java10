package moe.rtd.discord.roleassignerbot.config;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Class responsible for the bot settings and configuration, including loading and saving it.
 * @author Big J
 */
public class BotSettings {

    /**
     * Object for synchronizing server map modifications.
     */
    private static final Object lockServerConfigurations = new Object();
    /**
     * Map of all servers which the bot is a member of.
     */
    private static Map<Long, ServerConfiguration> serverConfigurations;

    /**
     * @return The save file which contains .
     */
    private static File getSaveFile() {
        try {
            var jar = new File(BotSettings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            return new File(jar.getPath().replaceFirst("(.jar)$", ".xml"));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to locate save file.", e);
        }
    }

    /**
     * Loads the bot configuration.
     */
    public static void loadConfiguration() {
        // TODO
    }

    /**
     * Saves the bot configuration.
     */
    public static void saveConfiguration() {
        // TODO
    }

    /**
     * Validates an XML file against the save file schema.
     */
    private static void validate(InputStream xml) throws SAXException, IOException {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(BotSettings.class.getResourceAsStream("/res/save_file.xsd")))
                .newValidator().validate(new StreamSource(xml));
    }

    /**
     * Starts all of the event handlers.
     */
    public static void start() {
        // TODO
    }

    /**
     * Stops all of the event handlers.
     */
    public static void stop() {
        // TODO
    }

    /**
     * Clears all loaded configuration data and stops any running threads in the process.
     */
    public static void destroy() {
        // TODO
    }

    /**
     * Returns the {@link ServerConfiguration} with the specified ID, or null if it isn't mapped.
     * @param ID ID of the server to return.
     * @return The requested server, or null if it isn't mapped.
     */
    public static ServerConfiguration getServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            return serverConfigurations.get(ID);
        }
    }

    /**
     * Adds a {@link ServerConfiguration} to the server map, if necessary.
     * @param ID ID of the server to add.
     * @return The {@link ServerConfiguration} that was added if necessary, or the value corresponding to the ID
     * if the server is already mapped.
     */
    public static ServerConfiguration addServer(long ID) {
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            var gotSC = serverConfigurations.get(ID);
            if(gotSC == null) {
                var server = new ServerConfiguration(ID);
                serverConfigurations.put(ID, server);
                return server;
            } else {
                return gotSC;
            }
        }
    }

    /**
     * Removes a {@link ServerConfiguration} from the server map.
     * @param ID ID of the server to remove.
     */
    public static void removeServer(long ID) {
        ServerConfiguration server;
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            server = serverConfigurations.remove(ID);
        }
        if(server != null) server.terminate();
    }
}
