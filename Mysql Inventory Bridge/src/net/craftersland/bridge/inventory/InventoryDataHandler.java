package net.craftersland.bridge.inventory;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.craftersland.bridge.inventory.objects.DatabaseInventoryData;
import net.craftersland.bridge.inventory.objects.InventorySyncData;
import net.craftersland.bridge.inventory.objects.InventorySyncTask;

public class InventoryDataHandler {

	private final Inv pd;
	private final Set<Player> playersInSync = new HashSet<>();

	public InventoryDataHandler(Inv pd) {
		this.pd = pd;
	}

	public boolean isSyncComplete(Player p) {
		return playersInSync.contains(p);
	}

	private void dataCleanup(Player p) {
		playersInSync.remove(p);
	}

	public void saveInv(Player p, boolean onQuit) {
		if (!isSyncComplete(p)) {
			return;
		}

		if (pd.getConfigHandler().getBoolean("Debug.InventorySync")) {
			Inv.log.info("Inventory Debug - Save Data - Start - " + p.getName());
		}

		String invSerial = encodeItems(p.getInventory().getContents());
		String armorSerial = pd.getConfigHandler().getBoolean("General.syncArmorEnabled") ? encodeItems(p.getInventory().getArmorContents()) : "none";

		pd.getInvMysqlInterface().setData(p, invSerial, armorSerial, "false");

		if (onQuit) {
			dataCleanup(p);
		}
	}

	public void saveOnDeath(Player p) {
		if (!isSyncComplete(p)) {
			return;
		}

		if (pd.getConfigHandler().getBoolean("Debug.InventorySync")) {
			Inv.log.info("Inventory Debug - Save on Death - Start - " + p.getName());
		}

		String invSerial = encodeItems(p.getInventory().getContents());
		String armorSerial = pd.getConfigHandler().getBoolean("General.syncArmorEnabled") ? encodeItems(p.getInventory().getArmorContents()) : "none";

		pd.getInvMysqlInterface().setData(p, invSerial, armorSerial, "false");
	}

	public void onJoinFunction(final Player p) {
		if (Inv.isDisabling) {
			return;
		}
		if (playersInSync.contains(p)) {
			return;
		}

		if (pd.getInvMysqlInterface().hasAccount(p)) {
			final InventorySyncData syncData = new InventorySyncData();
			backupAndReset(p, syncData);
			DatabaseInventoryData data = pd.getInvMysqlInterface().getData(p);

			if (data.getSyncStatus().equals("true")) {
				setPlayerData(p, data, syncData, false);
			} else {
				InventorySyncTask syncTask = new InventorySyncTask(pd, System.currentTimeMillis(), p, syncData);
				p.getScheduler().runAtFixedRate(pd, task -> syncTask.run(task), null, 10L, 10L);
			}
		} else {
			playersInSync.add(p);
			saveInv(p, false);
		}
	}

	public void setPlayerData(final Player p, DatabaseInventoryData data, InventorySyncData syncData, boolean cancelTask) {
		if (playersInSync.contains(p)) {
			return;
		}

		if (Inv.is19Server) {
			if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled")) {
				setInventory(p, data, syncData);
			} else {
				setInventoryNew(p, data, syncData);
			}
		} else {
			setInventory(p, data, syncData);
			if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled")) {
				setArmor(p, data, syncData);
			}
		}

		pd.getInvMysqlInterface().setSyncStatus(p, "false");

		p.getScheduler().runDelayed(pd, task -> {
			playersInSync.add(p);
		}, null, 2L);
	}

	private void backupAndReset(Player p, InventorySyncData syncData) {
		syncData.setBackupInventory(p.getInventory().getContents());
		p.getInventory().clear();

		if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled")) {
			syncData.setBackupArmor(p.getInventory().getArmorContents());
			p.getInventory().setHelmet(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setBoots(null);
		}
		p.updateInventory();
	}

	private void setInventory(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (!"none".equals(data.getRawInventory())) {
			try {
				p.getInventory().setContents(decodeItems(data.getRawInventory()));
			} catch (Exception e) {
				e.printStackTrace();
				if (syncData.getBackupInventory() != null) {
					p.getInventory().setContents(syncData.getBackupInventory());
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.inventorySyncError"));
					pd.getSoundHandler().sendPlingSound(p);
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.inventorySyncBackup"));
				}
			}
		} else {
			p.getInventory().setContents(syncData.getBackupInventory());
		}
		p.updateInventory();
	}

	private void setInventoryNew(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (!"none".equals(data.getRawInventory())) {
			try {
				p.getInventory().setContents(decodeItems(data.getRawInventory()));
				p.getInventory().setArmorContents(syncData.getBackupArmor());
			} catch (Exception e) {
				e.printStackTrace();
				if (syncData.getBackupInventory() != null) {
					p.getInventory().setContents(syncData.getBackupInventory());
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.inventorySyncError"));
					pd.getSoundHandler().sendPlingSound(p);
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.inventorySyncBackup"));
				}
			}
		} else {
			p.getInventory().setContents(syncData.getBackupInventory());
		}
		p.updateInventory();
	}

	private void setArmor(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (!"none".equals(data.getRawArmor())) {
			try {
				p.getInventory().setArmorContents(decodeItems(data.getRawArmor()));
			} catch (Exception e) {
				e.printStackTrace();
				p.getInventory().setArmorContents(syncData.getBackupArmor());
				p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.armorSyncError"));
				pd.getSoundHandler().sendPlingSound(p);
				p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessages.armorSyncBackup"));
			}
		} else {
			p.getInventory().setArmorContents(syncData.getBackupArmor());
		}
		p.updateInventory();
	}

	public String encodeItems(ItemStack[] items) {
		if (pd.useProtocolLib && pd.getConfigHandler().getBoolean("General.enableModdedItemsSupport")) {
			return InventoryUtils.saveModdedStacksData(items);
		} else {
			return InventoryUtils.itemStackArrayToBase64(items);
		}
	}

	public ItemStack[] decodeItems(String data) throws Exception {
		if (pd.useProtocolLib && pd.getConfigHandler().getBoolean("General.enableModdedItemsSupport")) {
			ItemStack[] it = InventoryUtils.restoreModdedStacks(data);
			if (it == null) {
				it = InventoryUtils.itemStackArrayFromBase64(data);
			}
			return it;
		} else {
			return InventoryUtils.itemStackArrayFromBase64(data);
		}
	}
}
