package de.jumpingpxl.jumpingaddon.util;

import de.jumpingpxl.jumpingaddon.JumpingAddon;
import lombok.Getter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nico (JumpingPxl) Middendorf
 * @date 12.03.2019
 */

@Getter
public class ChatComponent {

	private String formattedText;
	private String unformattedText;
	private HoverEvent.Action hoverEventAction;
	private String hoverEventValue;
	private ClickEvent.Action clickEventAction;
	private String clickEventValue;
	private boolean hoverEvent;
	private boolean clickEvent;
	private boolean byText;
	private boolean sibling;
	private boolean prefix;
	private List<ChatComponent> siblings = new ArrayList<>();
	private ChatComponent parent;

	public ChatComponent(String formattedText) {
		byText = true;
		this.formattedText = formattedText;
		this.unformattedText = JumpingAddon.getInstance().getStringUtils().stripColor(formattedText);
	}

	public ChatComponent(IChatComponent chatComponent, boolean... sibling) {
		formattedText = chatComponent.getFormattedText();
		unformattedText = chatComponent.getUnformattedText();
		if (chatComponent.getChatStyle().getChatClickEvent() != null) {
			clickEvent = true;
			clickEventAction = chatComponent.getChatStyle().getChatClickEvent().getAction();
			clickEventValue = chatComponent.getChatStyle().getChatClickEvent().getValue();
		}
		if (chatComponent.getChatStyle().getChatHoverEvent() != null) {
			hoverEvent = true;
			hoverEventAction = chatComponent.getChatStyle().getChatHoverEvent().getAction();
			hoverEventValue = chatComponent.getChatStyle().getChatHoverEvent().getValue().getFormattedText();
		}
		if (sibling.length == 0)
			append(chatComponent);
	}

	public ChatComponent getSibling(String string, boolean ignoreCase) {
		ChatComponent chatComponent = null;
		for (ChatComponent chatComponents : siblings)
			if (ignoreCase ? chatComponents.getUnformattedText().equalsIgnoreCase(string) : chatComponents.getUnformattedText().equals(string)) {
				chatComponent = chatComponents;
				break;
			}
		return chatComponent;
	}

	public ChatComponent getFormattedSibling(String string, boolean ignoreCase) {
		ChatComponent chatComponent = null;
		for (ChatComponent chatComponents : siblings) {
			String formatted = chatComponents.getFormattedText().replace("§r", "");
			if (ignoreCase ? formatted.equalsIgnoreCase(string) : formatted.equals(string)) {
				chatComponent = chatComponents;
				break;
			}
		}
		return chatComponent;
	}

	public ChatComponent getFormattedSibling(String string, boolean ignoreCase, int index) {
		ChatComponent chatComponent = null;
		for (ChatComponent chatComponents : siblings) {
			String formatted = chatComponents.getFormattedText().replace("§r", "").split(" ")[index];
			if (ignoreCase ? formatted.equalsIgnoreCase(string) : formatted.equals(string)) {
				chatComponent = chatComponents;
				break;
			}
		}
		return chatComponent;
	}

	public ChatComponent getSibling(int index) throws IndexOutOfBoundsException {
		return siblings.get(index);
	}

	public ChatComponent setPrefix(boolean value) {
		prefix = value;
		return this;
	}

	public ChatComponent setText(String value) {
		formattedText = value;
		unformattedText = JumpingAddon.getInstance().getStringUtils().stripColor(value);
		return this;
	}

	public ChatComponent append(ChatComponent... chatComponents) {
		if (chatComponents.length == 1 && !chatComponents[0].getSiblings().isEmpty())
			chatComponents[0].getSiblings().forEach(chatComponent -> siblings.add(chatComponent.setParent(this)));
		else
			Arrays.asList(chatComponents).forEach(chatComponent -> siblings.add(chatComponent.setParent(this)));
		return this;
	}

	public ChatComponent append(IChatComponent... chatComponents) {
		if (chatComponents.length == 1 && !chatComponents[0].getSiblings().isEmpty())
			chatComponents[0].getSiblings().forEach(chatComponent -> siblings.add(new ChatComponent(chatComponent, true).setParent(this)));
		else
			Arrays.asList(chatComponents).forEach(chatComponent -> siblings.add(new ChatComponent(chatComponent, true).setParent(this)));
		return this;
	}

	public ChatComponent setClickEvent(ClickEvent.Action action, String value) {
		this.clickEventAction = action;
		this.clickEventValue = value;
		this.clickEvent = true;
		return this;
	}

	public ChatComponent setHoverEvent(HoverEvent.Action action, String value) {
		this.hoverEventAction = action;
		this.hoverEventValue = value;
		this.hoverEvent = true;
		return this;
	}

	public ChatComponent setParent(ChatComponent chatComponent) {
		parent = chatComponent;
		sibling = true;
		return this;
	}

	public IChatComponent create() {
		IChatComponent build = new ChatComponentText((JumpingAddon.getInstance().getStringUtils().repeatLastColor(sibling ? formattedText : "").replace("[%prefix$$]", JumpingAddon.getInstance().getSettings().getPrefix()))).setChatStyle(new ChatStyle());
		if (sibling) {
			if (clickEvent)
				build.getChatStyle().setChatClickEvent(new ClickEvent(clickEventAction, clickEventValue));
			if (hoverEvent)
				build.getChatStyle().setChatHoverEvent(new HoverEvent(hoverEventAction, new ChatComponentText(hoverEventValue)));
		} else {
			if (byText) {
				IChatComponent chatComponent = new ChatComponentText(JumpingAddon.getInstance().getStringUtils().repeatLastColor(formattedText.replace("[%prefix$$]", JumpingAddon.getInstance().getSettings().getPrefix()))).setChatStyle(new ChatStyle());
				if (clickEvent)
					chatComponent.getChatStyle().setChatClickEvent(new ClickEvent(clickEventAction, clickEventValue));
				if (hoverEvent)
					chatComponent.getChatStyle().setChatHoverEvent(new HoverEvent(hoverEventAction, new ChatComponentText(hoverEventValue)));
				build.appendSibling(chatComponent);
			}
			siblings.forEach(siblings -> build.appendSibling(siblings.create()));
		}
		return build;
	}
}
