package de.jumpingpxl.jumpingaddon.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.jumpingpxl.jumpingaddon.JumpingAddon;
import de.jumpingpxl.jumpingaddon.util.elements.BetterBooleanElement;
import de.jumpingpxl.jumpingaddon.util.elements.BetterDropDownElement;
import de.jumpingpxl.jumpingaddon.util.elements.BetterHeaderElement;
import de.jumpingpxl.jumpingaddon.util.elements.BetterListContainerElement;
import de.jumpingpxl.jumpingaddon.util.elements.BetterNumberElement;
import de.jumpingpxl.jumpingaddon.util.elements.BetterStringElement;
import de.jumpingpxl.jumpingaddon.util.elements.TextElement;
import de.jumpingpxl.jumpingaddon.util.serversupport.Server;
import net.labymod.main.Source;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.HeaderElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.event.ClickEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nico (JumpingPxl) Middendorf
 * @date 25.02.2019
 */

public class Settings {

	//Integer
	public static int glintColor;
	private final JumpingAddon jumpingAddon;
	private final List<SettingValue> settingValues = new ArrayList<>();
	private double updateAvailable = -1.0D;
	private ChatComponent startupNotification;
	private String tabHeader;
	private String tabFooter;
	private GuiScreen currentGui;
	private GuiScreen previousGui;
	//String
	private SettingValue language;
	private SettingValue pingFormatting;
	//Boolean
	private boolean defaultChecks;
	private SettingValue debug;
	private SettingValue nobob;
	private SettingValue ping;
	private SettingValue discord;
	private SettingValue discordSmallIcon;
	private SettingValue discordTimeStamp;
	private SettingValue coloredGlint;
	private SettingValue idleSeconds;

	public Settings(JumpingAddon jumpingAddon) {
		this.jumpingAddon = jumpingAddon;
		checkForUpdates();
	}

	public Settings loadSettings() {
		debug = new SettingValue(jumpingAddon, settingValues, "debug", false);

		//String
		language = new SettingValue(jumpingAddon, settingValues, "language", "Deutsch");
		pingFormatting = new SettingValue(jumpingAddon, settingValues, "pingFormatting", "%color%%ping%ms");

		//Boolean
		nobob = new SettingValue(jumpingAddon, settingValues, "enabledNoBob", true);
		ping = new SettingValue(jumpingAddon, settingValues, "enabledPing", true);
		discord = new SettingValue(jumpingAddon, settingValues, "enabledDiscordRPC", false);
		discordSmallIcon = new SettingValue(jumpingAddon, settingValues, "enabledDiscordRPCSmallIcon", true);
		discordTimeStamp = new SettingValue(jumpingAddon, settingValues, "enabledDiscordRPCTimeStamp", true);
		coloredGlint = new SettingValue(jumpingAddon, settingValues, "enabledGlintColor", false);

		//Integer
		idleSeconds = new SettingValue(jumpingAddon, settingValues, "idleSeconds", 60);
		return this;
	}

	public void loadConfig() {
		glintColor = jumpingAddon.getConfiguration().has("glintColor") ?
				jumpingAddon.getConfiguration()
				.getAsInt("glintColor") : ModColor.WHITE.getColor().getRGB();
		jumpingAddon.getModuleHandler().getHitBoxModule().loadSettings();
		jumpingAddon.getCommandHandler().setCommandSettings(settingValues);
		settingValues.forEach(SettingValue::load);
		jumpingAddon.getModuleHandler().getActionModule().loadActions();
		jumpingAddon.getModuleHandler().getDiscordRPCModule().load();
	}

	public void fillSettings(final List<SettingsElement> list) {
		list.add(new HeaderElement(
				"§6JumpingAddon v" + jumpingAddon.getVersion() + (updateAvailable == -1.0D ? ""
						: " §7- §4v" + updateAvailable + " available!")));
		list.add(new BetterHeaderElement(SettingsCategory.MAINSETTINGS).getElement());
		list.add(new BetterBooleanElement("NoBob", new ControlElement.IconData(Material.EMERALD),
				nobob)
				.getElement());
		list.add(
				new BetterNumberElement("Idle after (Seconds)",
						new ControlElement.IconData(Material.WATCH),
						idleSeconds, 10, 1800).getElement());
		list.add(jumpingAddon.getModuleHandler().getHitBoxModule().getElement().getElement());
		list.add(new BetterListContainerElement("DiscordRPC",
				new ControlElement.IconData(Material.REDSTONE)).addSettings(
				new BetterBooleanElement("Enabled", new ControlElement.IconData(Material.LEVER), discord,
						enabled -> jumpingAddon.getModuleHandler().getDiscordRPCModule().toggleStatus(enabled)),
				new HeaderElement(""),
				new BetterBooleanElement("Small Icon", new ControlElement.IconData(Material.LEVER),
						discordSmallIcon, enabled -> jumpingAddon.getModuleHandler()
						.getDiscordRPCModule()
						.toggleSmallIconStatus(enabled)),
				new BetterBooleanElement("Server TimeStamp", new ControlElement.IconData(Material.LEVER),
						discordTimeStamp, enabled -> jumpingAddon.getModuleHandler()
						.getDiscordRPCModule()
						.toggleTimeStampStatus(enabled))).getElement());
		list.add(new BetterListContainerElement("Ping above head",
				new ControlElement.IconData(Material.ARROW)).addSettings(
				new BetterBooleanElement("Enabled", new ControlElement.IconData(Material.LEVER), ping),
				new HeaderElement(""),
				new BetterStringElement("Formatting", new ControlElement.IconData(Material.PAPER),
						pingFormatting)).getElement());
		list.add(new BetterHeaderElement(SettingsCategory.SERVERSUPPORT).getElement());
		Arrays.stream(Server.values()).filter(server -> server.getServerSupport() != null
				&& server.getServerSupport().getSettingsElement() != null).forEach(
				server -> list.add(server.getServerSupport().getSettingsElement().getElement()));
		list.add(new BetterHeaderElement(SettingsCategory.MISCELLANEOUS).getElement());
		list.add(jumpingAddon.getCommandHandler().getCommandSettings().getElement());
		list.add(new TextElement(""));
		if (jumpingAddon.getLanguageHandler().getLanguages().size() > 1) {
			list.add(new BetterDropDownElement("Language",
					jumpingAddon.getLanguageHandler().getLanguages().toArray(new String[0]),
					language).getElement());
		}
	}

	public void checkForUpdates() {
		new Thread(() -> {
			try {
				jumpingAddon.log("[JumpingAddon] Checking for updates...");
				JsonObject jsonObject = getJsonObjectFromUrl("update.json", null);
				double version = jsonObject.get("currentVersion").getAsDouble();
				if (jumpingAddon.getVersion() < version) {
					jumpingAddon.log("[JumpingAddon] New version found: v" + version);
					updateAvailable = version;
					setStartupNotification(new ChatComponent(
							"[%prefix$$]§cA new version is available. Download JumpingAddon v" + version).append(
							new ChatComponent(" §4§lHERE").setClickEvent(ClickEvent.Action.OPEN_URL,
									"https://nicomiddendorf.de/jumpingaddon/JumpingAddon_1-8-9.jar")));
				} else {
					jumpingAddon.log("[JumpingAddon] No new version found.");
				}
			} catch (IOException e) {
				jumpingAddon.log("[JumpingAddon] An error occurred while trying to check for updates");
			}
		}).start();
	}

	public JsonObject getJsonObjectFromUrl(String fileName, JsonObject jsonObject)
			throws IOException {
		InputStreamReader inputStreamReader;
		try {
			URLConnection urlConnection = new URL(
					"https://nicomiddendorf.de/jumpingaddon/" + fileName).openConnection();
			urlConnection.setRequestProperty("User-Agent", Source.getUserAgent());
			urlConnection.setReadTimeout(5000);
			urlConnection.setConnectTimeout(5000);
			urlConnection.connect();
			inputStreamReader = new InputStreamReader(urlConnection.getInputStream(),
					StandardCharsets.UTF_8);
			System.out.println("[JumpingAddon] Successfully downloaded the file " + fileName);
		} catch (IOException e) {
			System.out.println("[JumpingAddon] An error occurred while loading the file " + fileName);
			if (jsonObject != null) {
				return jsonObject;
			} else {
				throw e;
			}
		}
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String line;
		StringBuilder contents = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			contents.append(contents.toString().equals("") ? "" : "\n").append(line);
		}

		String content = contents.toString();
		content = content.replace("\\u003c", "<").replace("\u003c", "<");
		content = content.replace("\\u003d", "=").replace("\u003d", "=");
		System.out.println("GETFILE " + fileName + ";" + content.replace("\n", ""));
		return (JsonObject) new JsonParser().parse(content);
	}

	public JsonObject getJsonObjectFromResource(String fileName) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
				"assets/minecraft/jumpingaddon/data/" + fileName);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
				StandardCharsets.UTF_8);
		return (JsonObject) new JsonParser().parse(inputStreamReader);
	}

	public JumpingAddon getJumpingAddon() {
		return this.jumpingAddon;
	}

	public List<SettingValue> getSettingValues() {
		return this.settingValues;
	}

	public double getUpdateAvailable() {
		return this.updateAvailable;
	}

	public ChatComponent getStartupNotification() {
		return this.startupNotification;
	}

	public void setStartupNotification(ChatComponent chatComponent) {
		if (chatComponent == null) {
			startupNotification = null;
		} else if (startupNotification == null) {
			startupNotification = chatComponent;
		} else {
			startupNotification = chatComponent.append(
					startupNotification.setText("\n" + startupNotification.getFormattedText()));
		}
	}

	public String getTabHeader() {
		return this.tabHeader;
	}

	public void setTabHeader(String tabHeader) {
		this.tabHeader = tabHeader;
	}

	public String getTabFooter() {
		return this.tabFooter;
	}

	public void setTabFooter(String tabFooter) {
		this.tabFooter = tabFooter;
	}

	public GuiScreen getCurrentGui() {
		return this.currentGui;
	}

	public void setCurrentGui(GuiScreen currentGui) {
		this.currentGui = currentGui;
	}

	public GuiScreen getPreviousGui() {
		return this.previousGui;
	}

	public void setPreviousGui(GuiScreen previousGui) {
		this.previousGui = previousGui;
	}

	public SettingValue getLanguage() {
		return this.language;
	}

	public SettingValue getPingFormatting() {
		return this.pingFormatting;
	}

	public boolean isDefaultChecks() {
		return this.defaultChecks;
	}

	public SettingValue getDebug() {
		return this.debug;
	}

	public SettingValue getNobob() {
		return this.nobob;
	}

	public SettingValue getPing() {
		return this.ping;
	}

	public SettingValue getDiscord() {
		return this.discord;
	}

	public SettingValue getDiscordSmallIcon() {
		return this.discordSmallIcon;
	}

	public SettingValue getDiscordTimeStamp() {
		return this.discordTimeStamp;
	}

	public SettingValue getColoredGlint() {
		return this.coloredGlint;
	}

	public SettingValue getIdleSeconds() {
		return this.idleSeconds;
	}

	public enum SettingsCategory {
		MAINSETTINGS("Main Settings", "§e"),
		SERVERSUPPORT("Server-Support", "§c"),
		MISCELLANEOUS("Miscellaneous", "§b"),
		DEBUG("Debug", "§d");

		private static SettingsCategory lastCategory;
		private final String displayName;
		private final String color;

		SettingsCategory(String displayName, String color) {
			this.displayName = displayName;
			this.color = color;
		}

		public static SettingsCategory getLastCategory() {
			return SettingsCategory.lastCategory;
		}

		public static void setLastCategory(SettingsCategory lastCategory) {
			SettingsCategory.lastCategory = lastCategory;
		}

		public String getCompleteName() {
			return color + displayName;
		}

		public String getDisplayName() {
			return this.displayName;
		}

		public String getColor() {
			return this.color;
		}
	}
}
