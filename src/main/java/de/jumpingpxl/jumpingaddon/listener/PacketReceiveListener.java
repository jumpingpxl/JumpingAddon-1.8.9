package de.jumpingpxl.jumpingaddon.listener;

import de.jumpingpxl.jumpingaddon.JumpingAddon;
import net.labymod.utils.Consumer;
import net.minecraft.network.play.server.S02PacketChat;

/**
 * @author Nico (JumpingPxl) Middendorf
 * @date 04.04.2019
 */

public class PacketReceiveListener {

	private final JumpingAddon jumpingAddon;

	public PacketReceiveListener(JumpingAddon jumpingAddon) {
		this.jumpingAddon = jumpingAddon;
	}

	public Consumer<Object> onPacketReceive() {
		return object -> {
			if (object instanceof S02PacketChat) {
				S02PacketChat packet = (S02PacketChat) object;
				if (packet.getType() == 2) {
					jumpingAddon.getConnection().setLastActionMessage(
							packet.getChatComponent().getFormattedText());
				}
			}
		};
	}
}
