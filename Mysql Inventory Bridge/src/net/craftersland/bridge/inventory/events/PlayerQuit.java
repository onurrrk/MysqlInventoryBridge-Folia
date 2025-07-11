package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

	private final Inv inv;

	public PlayerQuit(Inv inv) {
		this.inv = inv;
	}

	@EventHandler
	public void onDisconnect(final PlayerQuitEvent event) {
		if (Inv.isDisabling) {
			return;
		}

		final Player p = event.getPlayer();

		p.getScheduler().run(inv, task -> {
			inv.getInventoryDataHandler().saveInv(p, true);
		}, null);
	}
}
