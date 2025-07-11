package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;
import net.craftersland.bridge.inventory.objects.SyncCompleteTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

	private Inv inv;

	public PlayerJoin(Inv inv) {
		this.inv = inv;
	}

	@EventHandler
	public void onLogin(final PlayerJoinEvent event) {
		if (Inv.isDisabling) {
			return;
		}

		final Player p = event.getPlayer();

		p.getScheduler().runDelayed(inv, (scheduledTask) -> {
			if (p.isOnline()) {
				inv.getInventoryDataHandler().onJoinFunction(p);
				
				SyncCompleteTask syncTask = new SyncCompleteTask(inv, System.currentTimeMillis(), p);

				p.getScheduler().runAtFixedRate(inv, (repeatingTask) -> {
					syncTask.run(repeatingTask);
				}, null, 5L, 20L);
			}
		}, null, 5L);
	}
}
