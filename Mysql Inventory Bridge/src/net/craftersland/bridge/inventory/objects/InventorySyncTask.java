package net.craftersland.bridge.inventory.objects;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import net.craftersland.bridge.inventory.Inv;

public class InventorySyncTask {

	private final Inv pd;
	private final long startTime;
	private final Player p;
	private boolean inProgress = false;
	private final InventorySyncData syncD;

	public InventorySyncTask(Inv pd, long start, Player player, InventorySyncData syncData) {
		this.pd = pd;
		this.startTime = start;
		this.p = player;
		this.syncD = syncData;
	}

	public void run(ScheduledTask task) {
		if (inProgress) {
			return;
		}

		if (p == null || !p.isOnline()) {
			task.cancel();
			return;
		}

		inProgress = true;

		DatabaseInventoryData data = pd.getInvMysqlInterface().getData(p);

		if (data.getSyncStatus().equals("true") ||
				(System.currentTimeMillis() - Long.parseLong(data.getLastSeen()) >= 600 * 1000) ||
				(System.currentTimeMillis() - startTime >= 22 * 1000)) {

			pd.getInventoryDataHandler().setPlayerData(p, data, syncD, true);
			task.cancel();
		}

		inProgress = false;
	}
}
