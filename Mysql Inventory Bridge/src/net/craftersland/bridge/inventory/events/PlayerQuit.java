package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.logging.Level;

public class PlayerQuit implements Listener {

	private final Inv inv;

	public PlayerQuit(Inv inv) {
		this.inv = inv;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDisconnect(final PlayerQuitEvent event) {
		if (Inv.isDisabling) {
			return;
		}

		final Player p = event.getPlayer();
		
		try {
			inv.getInventoryDataHandler().saveInv(p, true);
			inv.getInventoryDataHandler().dataCleanup(p);
		} catch (Exception e) {
			inv.getLogger().log(Level.SEVERE, "Oyuncu " + p.getName() + " için çıkışta senkron kayıt sırasında hata oluştu!", e);
		}
	}
}
