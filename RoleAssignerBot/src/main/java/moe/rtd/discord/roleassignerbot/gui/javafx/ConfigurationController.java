package moe.rtd.discord.roleassignerbot.gui.javafx;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import moe.rtd.discord.roleassignerbot.config.BotSettings;
import moe.rtd.discord.roleassignerbot.discord.DiscordConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JavaFX controller for the configuration tab.
 * @author Big J
 */
public class ConfigurationController {

    /**
     * Log4j2 Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(ConfigurationController.class);

    /**
     * ComboBox for selecting the server to view the configuration of.
     */
    @FXML private ComboBox<Long> serverSelection;

    /**
     * VBox for displaying the currently selected server's configuration.
     */
    @FXML private VBox config;

    /**
     * Method called when the context menu for {@link #serverSelection} is opened.
     */
    @FXML private void contextMenu() {
        log.debug("Refreshing server list...");

        var items = serverSelection.getItems();
        items.clear();
        BotSettings.forEach((sID, sc) -> items.add(sID));
    }

    /**
     * Method called when the refresh button is pressed, refreshes the displayed configuration.
     */
    @FXML private void refresh() {
        log.debug("Refreshing configuration...");

        var selection = serverSelection.getValue();
        if(selection == null) return;
        var sc = BotSettings.getServer(selection);
        if(sc == null) return;

        log.debug("Building configuration for server '" + selection + "'.");

        config.getChildren().clear();

        sc.forEach((cID, cc) -> { // for each ChannelConfiguration
            var ccn = new VBox();
            ccn.getStyleClass().addAll("channel", "VBox");

            var cIDn = new HBox(new Label(Long.toUnsignedString(cID)));
            cIDn.getStyleClass().add("elementID");

            ccn.getChildren().add(cIDn);

            cc.forEach((mID, mc) -> { // for each MessageConfiguration
                var mcn = new VBox();
                mcn.getStyleClass().addAll("message", "VBox");

                var mIDn = new HBox(new Label(Long.toUnsignedString(mID)));
                mIDn.getStyleClass().add("elementID");

                mcn.getChildren().add(mIDn);

                mc.forEach((ID, e) -> { // for each entry
                    var en = new VBox();
                    en.getStyleClass().addAll("entry", "VBox");

                    var role = new Label();
                    Node emoji;
                    {
                        try {
                            var dc = DiscordConnection.getClient();
                            var s = dc.getGuildByID(selection);

                            try {
                                var r = s.getRoleByID(ID);

                                role.setText("@" + r.getName());
                                {
                                    int rgb = r.getColor().getRGB();
                                    int mask = 0xFF;

                                    int cB = rgb & mask;
                                    int cG = (rgb >>> 8) & mask;
                                    int cR = (rgb >>> 16) & mask;

                                    role.setTextFill(Color.rgb(cR, cG, cB));
                                }

                            } catch(RuntimeException roleException) {
                                log.error("Cannot find role \"" + Long.toUnsignedString(ID) + "\": " + roleException.getMessage());

                                role.setText("<@&" + Long.toUnsignedString(ID) + ">");
                                role.setTextFill(Color.web("#99AAB5"));
                            }

                            try {
                                if(e.matches("(<(a)?:)[a-zA-Z0-9_]+(:)[0-9]+(>)")) {
                                    var j = s.getEmojiByID(Long.parseLong(e
                                            .replaceFirst("(<(a)?:)[a-zA-Z0-9_]+(:)", "")
                                            .replace(">", "")));

                                    emoji = new ImageView(j.getImageUrl());
                                    ((ImageView) emoji).setFitWidth(24);
                                    ((ImageView) emoji).setFitHeight(24);
                                } else {
                                    emoji = new Label(e);
                                    ((Label) emoji).setTextFill(Color.web("#F0F0F0"));
                                }

                                Tooltip.install(emoji, new Tooltip(e));

                            } catch(RuntimeException emojiException) {
                                log.error("Cannot find Discord emoji \"" + e + "\": " + emojiException.getMessage());

                                emoji = new Label(e);
                                ((Label) emoji).setTextFill(Color.web("#BC0000"));
                            }

                        } catch(RuntimeException serverException) {
                            log.error("Cannot find Discord server \"" + Long.toUnsignedString(selection) + "\": " + serverException.getMessage());

                            role.setText("<@&" + Long.toUnsignedString(ID) + ">");
                            role.setTextFill(Color.web("#99AAB5"));

                            emoji = new Label(e);
                            ((Label) emoji).setTextFill(Color.web("#BC0000"));
                        }
                    }

                    role.getStyleClass().add("role");
                    var spacer = new Label(" to ");
                    spacer.getStyleClass().add("spacer");
                    var info = new HBox(role, spacer, emoji);
                    en.getChildren().add(info);

                    mcn.getChildren().add(en);
                });
                ccn.getChildren().add(mcn);
            });
            config.getChildren().add(ccn);
        });

        log.debug("Displaying refreshed configuration for the selected server.");
    }
}
