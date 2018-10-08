package moe.rtd.discord.roleassignerbot.config;

import moe.rtd.discord.roleassignerbot.misc.logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * Class responsible for the bot settings and configuration, including loading and saving it.
 * @author Big J
 */
public class BotSettings {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(BotSettings.class);

    /**
     * Object for synchronizing server map modifications.
     */
    private static final Object lockServerConfigurations = new Object();
    /**
     * Map of all servers which the bot is a member of.
     */
    private static Map<Long, ServerConfiguration> serverConfigurations;

    /**
     * @return The save file which contains the saved settings for the bot.
     */
    private static File getSaveFile() {
        try {
            var jar = new File(BotSettings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            var jp = jar.getPath();
            var path = (jar.isFile()) ? jp.replaceFirst("(.jar)$", ".xml") :
                    jp + ((jp.endsWith("\\") || jp.endsWith("/")) ? "" : "/") + "configuration.xml";
            log.info(Markers.CONFIG, "Save file path: \"" + path + "\".");
            return new File(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to locate save file.", e);
        }
    }

    /**
     * Loads the bot configuration.
     */
    public static void loadConfiguration() {
        synchronized(lockServerConfigurations) {
            serverConfigurations = new TreeMap<>();
        }

        File sf = getSaveFile();
        if(!sf.exists()) return;

        try(var fis = new FileInputStream(sf)) {
            // Validates the DOM against the XSD
            validate(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate save file.", e);
        } catch (SAXException e) {
            throw new RuntimeException("Invalid save file.", e);
        }

        // Save file DOM
        Document doc;

        try {

            // Loads the save file into a DOM
            var dbf = DocumentBuilderFactory.newDefaultInstance();
            var db = dbf.newDocumentBuilder();
            doc = db.parse(sf);

            // Normalizes the DOM
            doc.normalize();

        } catch(Exception e) {
            throw new RuntimeException("Failed to load save file into a DOM.", e);
        }

        // Save file root element
        Element root = (Element) doc.getElementsByTagName("BotSettings").item(0);

        {
            var snList = root.getElementsByTagName("ServerConfiguration");
            // Server loop
            for(int i = 0; i < snList.getLength(); i++) {
                var sn = (Element) snList.item(i);
                log.debug(Markers.CONFIG, "Server found: " + sn.getAttribute("id"));
                var s = addServer(Long.parseUnsignedLong(sn.getAttributes().getNamedItem("id").getNodeValue()));
                // Server Properties
                {
                    var enList = ((Element) sn.getElementsByTagName("Properties").item(0))
                            .getElementsByTagName("Entry");
                    // Properties Entry loop
                    for(int k = 0; k < enList.getLength(); k++) {
                        var en = (Element) enList.item(k);
                        log.debug(Markers.CONFIG, "Properties Entry found: " + en.getAttribute("key"));
                        s.setProperty(
                                ServerConfiguration.Properties.valueOf(en.getAttribute("key").toUpperCase()),
                                deserializeBase64(en.getAttribute("value")));
                    }
                }
                var cnList = sn.getElementsByTagName("ChannelConfiguration");
                // Channel loop
                for(int j = 0; j < cnList.getLength(); j++) {
                    var cn = (Element) cnList.item(j);
                    log.debug(Markers.CONFIG, "Channel found: " + cn.getAttribute("id"));
                    var c = s.addChannel(Long.parseUnsignedLong(cn.getAttributes().getNamedItem("id").getNodeValue()));
                    var mnList = cn.getElementsByTagName("MessageConfiguration");
                    // Message loop
                    for(int k = 0; k < mnList.getLength(); k++) {
                        var mn = (Element) mnList.item(k);
                        log.debug(Markers.CONFIG, "Message found: " + mn.getAttribute("id"));
                        var m = c.addMessage(Long.parseUnsignedLong(mn.getAttribute("id")));
                        var enList = mn.getElementsByTagName("Entry");
                        // Message Configuration loop
                        for(int l = 0; l < enList.getLength(); l++) {
                            var en = (Element) enList.item(l);
                            log.debug(Markers.CONFIG, "Message Entry found: " +
                                    en.getAttribute("role") + " to " + en.getAttribute("emote"));
                            m.setRole(
                                    Long.parseUnsignedLong(en.getAttribute("role")),
                                    en.getAttribute("emote")
                            );
                        }
                    }
                }
            }
        }
        log.info(Markers.CONFIG, "Bot configuration has been loaded.");
    }

    /**
     * Saves the bot configuration.
     */
    public static void saveConfiguration() {
        File sf = getSaveFile();
        if(sf.exists()) if(!sf.delete()) throw new RuntimeException("Failed to delete old save file.");

        // Save file DOM
        Document doc;

        {
            // Create new DOM
            var dbf = DocumentBuilderFactory.newDefaultInstance();
            try {

                var db = dbf.newDocumentBuilder();
                doc = db.newDocument();

            } catch (ParserConfigurationException e) {
                throw new RuntimeException("Failed to create new DOM.", e);
            }
        }

        // Save file root element
        var root = doc.createElement("BotSettings");

        {
            // Set up the DOM
            doc.setXmlStandalone(true);
            doc.appendChild(root);
        }

        {
            synchronized(lockServerConfigurations) {
                // Server loop
                serverConfigurations.forEach((sID, sc) -> {
                    Element s;
                    {
                        // Create ServerConfiguration node
                        s = doc.createElement("ServerConfiguration");
                        s.setAttribute("id", String.valueOf(sc.getID()));
                        root.appendChild(s);
                    }
                    {
                        Element p;
                        {
                            // Create Properties node
                            p = doc.createElement("Properties");
                            s.appendChild(p);
                        }
                        // Properties loop
                        sc.forEachProperty((k, v) -> {
                            Element e;
                            if(!v.equals(k.getDefaultValue())) {
                                {
                                    // Create Entry node
                                    e = doc.createElement("Entry");
                                    p.appendChild(e);
                                }
                                {
                                    // Set Entry attributes
                                    e.setAttribute("key", k.name().toLowerCase());
                                    e.setAttribute("value", serializeBase64(v));
                                }
                            }
                        });
                    }
                    // Channel loop
                    sc.forEach((cID, cc) -> {
                        Element c;
                        {
                            // Create ChannelConfiguration node
                            c = doc.createElement("ChannelConfiguration");
                            c.setAttribute("id", String.valueOf(cc.getID()));
                            s.appendChild(c);
                        }
                        // Message loop
                        cc.forEach((mID, mc) -> {
                            Element m;
                            {
                                // Create MessageConfiguration node
                                m = doc.createElement("MessageConfiguration");
                                m.setAttribute("id", String.valueOf(mc.getID()));
                                c.appendChild(m);
                            }
                            // Configuration loop
                            mc.forEach((role, emote) -> {
                                Element e;
                                {
                                    // Create Entry node
                                    e = doc.createElement("Entry");
                                    m.appendChild(e);
                                }
                                {
                                    // Set Entry attributes
                                    e.setAttribute("role", role.toString());
                                    e.setAttribute("emote", emote);
                                }
                            });
                        });
                    });
                });
            }
        }

        // DOM transformer
        Transformer transformer;

        {
            try {

                transformer = TransformerFactory.newDefaultInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Failed to create new DOM transformer.", e);
            }
        }

        {
            // Save DOM to save file
            try {
                if(!sf.createNewFile()) throw new RuntimeException("Failed to create new save file.");
            } catch (IOException e) {
                throw new RuntimeException("Failed to create new save file.");
            }

            var source = new DOMSource(doc);
            var target = new StreamResult(sf);

            try {
                transformer.transform(source, target);
            } catch (TransformerException e) {
                throw new RuntimeException("Failed to save settings.", e);
            }
        }

        try(var fis = new FileInputStream(sf)) {
            validate(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate saved file.", e);
        } catch (SAXException e) {
            throw new RuntimeException("Saved file is invalid.", e);
        }
        log.info(Markers.CONFIG, "Bot configuration has been saved.");
    }

    /**
     * @param s The object to serialize.
     * @return The serialized base64 string.
     */
    private static String serializeBase64(Serializable s) {
        byte[] ba;
        {
            // Serialize the object to a byte array
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try(var oos = new ObjectOutputStream(os)) {

                oos.writeObject(s);
                oos.flush();

            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize an object.", e);
            }

            ba = os.toByteArray();
        }
        return Base64.getEncoder().encodeToString(ba);
    }

    /**
     * @param s The base64 string to deserialize.
     * @return The deserialized object.
     */
    private static Serializable deserializeBase64(String s) {
        {
            // Deserialize the object to a byte array
            byte[] ba = Base64.getDecoder().decode(s);
            ByteArrayInputStream is = new ByteArrayInputStream(ba);
            try(var ois = new ObjectInputStream(is)) {

                return (Serializable) ois.readObject();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("Failed to deserialize an object.", e);
            }
        }
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
        synchronized(lockServerConfigurations) {
            serverConfigurations
                    .forEach((k0, v0) -> v0
                            .forEach((k1, v1) -> v1
                                    .forEach((k2, v2) -> v2
                                            .getReactionHandler().start())));
        }
        log.info(Markers.CONFIG, "Bot configuration threads have been started.");
    }

    /**
     * Stops all of the event handlers.
     */
    public static void stop() {
        synchronized(lockServerConfigurations) {
            serverConfigurations
                    .forEach((k0, v0) -> v0
                            .forEach((k1, v1) -> v1
                                    .forEach((k2, v2) -> v2
                                            .getReactionHandler().terminate())));
        }
        log.info(Markers.CONFIG, "Bot configuration threads have been stopped.");
    }

    /**
     * Recursively clears all configuration data and stops the threads.
     */
    public static void destroy() {
        synchronized(lockServerConfigurations) {
            for(Iterator<Map.Entry<Long, ServerConfiguration>> i = serverConfigurations.entrySet().iterator(); i.hasNext();) {
                var current = i.next();
                i.remove();
                current.getValue().terminate();
            }
        }
        log.info(Markers.CONFIG, "Bot settings have been terminated.");
    }

    /**
     * @see Map#forEach(BiConsumer)
     */
    public static void forEach(BiConsumer<? super Long, ? super ServerConfiguration> action) {
        synchronized(lockServerConfigurations) {
            serverConfigurations.forEach(action);
        }
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
    static void removeServer(long ID) {
        ServerConfiguration server;
        synchronized(lockServerConfigurations) {
            if(serverConfigurations == null) throw new IllegalStateException("Configuration is not loaded.");
            server = serverConfigurations.remove(ID);
        }
        if(server != null) server.terminate();
    }
}
