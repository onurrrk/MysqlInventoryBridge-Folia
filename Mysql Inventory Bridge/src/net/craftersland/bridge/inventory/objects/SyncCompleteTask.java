package net.craftersland.bridge.inventory.objects;

import org.bukkit.entity.Player;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.craftersland.bridge.inventory.Inv;

public class SyncCompleteTask {

	private Inv pd;
	private long startTime;
	private Player p;
	private boolean inProgress = false;

	public SyncCompleteTask(Inv pd, long start, Player player) {
		this.pd = pd;
		this.startTime = start;
		this.p = player;
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
		
		if (pd.getInventoryDataHandler().isSyncComplete(p)) {
			if (!pd.getConfigHandler().getString("ChatMessages.syncComplete").isEmpty()) {
				p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.syncComplete"));
			}
			pd.getSoundHandler().sendLevelUpSound(p);
			task.cancel();
		} else {
			if (System.currentTimeMillis() - startTime >= 40 * 1000) {
				task.cancel();
			} else if (System.currentTimeMillis() - startTime >= 20 * 1000) {
				pd.getInvMysqlInterface().setSyncStatus(p, "true");
			}
		}
		
		inProgress = false;
	}
}
