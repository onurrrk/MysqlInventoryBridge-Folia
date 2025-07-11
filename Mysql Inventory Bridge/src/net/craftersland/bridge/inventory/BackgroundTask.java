package net.craftersland.bridge.inventory;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BackgroundTask {

	private final Inv m;

	public BackgroundTask(Inv m) {
		this.m = m;
		runTask();
	}

	private void runTask() {
		if (m.getConfigHandler().getBoolean("General.saveDataTask.enabled")) {
			Inv.log.info("Data save task is enabled.");
			long intervalTicks = m.getConfigHandler().getInteger("General.saveDataTask.interval") * 60L * 20L;

			m.getServer().getGlobalRegionScheduler().runAtFixedRate(
					m,
					task -> runSaveData(),
					intervalTicks,
					intervalTicks
			);
		} else {
			Inv.log.info("Data save task is disabled.");
		}
	}

	private void runSaveData() {
		if (m.getConfigHandler().getBoolean("General.saveDataTask.enabled")) {
			Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
			if (!onlinePlayers.isEmpty()) {
				if (!m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages")) {
					Inv.log.info("Saving online players data...");
				}

				for (Player p : new ArrayList<>(onlinePlayers)) {
					if (p.isOnline()) {
						m.getInventoryDataHandler().saveInv(p, false);
					}
				}

				if (!m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages")) {
					Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
				}
			}
		}
	}

	public void onShutDownDataSave() {
		Inv.log.info("Saving online players data...");
		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

		for (Player p : new ArrayList<>(onlinePlayers)) {
			if (p.isOnline()) {
				m.getInventoryDataHandler().saveInv(p, true);
			}
		}

		Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}
}
