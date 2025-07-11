package net.craftersland.bridge.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BackgroundTask {

    private Inv m;

    public BackgroundTask(Inv m) {
        this.m = m;
        runTask();
    }

    private void runTask() {
        if (m.getConfigHandler().getBoolean("General.saveDataTask.enabled")) {
            Inv.log.info("Data save task is enabled.");

            long intervalTicks = m.getConfigHandler().getInteger("General.saveDataTask.interval") * 60L * 20L;

            // Folia Uyumlu Scheduler:
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
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

                if (!m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages")) {
                    Inv.log.info("Saving online players data...");
                }

                for (Player p : onlinePlayers) {
                    if (p.isOnline()) {
                        m.getInventoryDataHandler().onDataSaveFunction(p, false, "false", null, null);
                    }
                }

                if (!m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages")) {
                    Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
                }

                onlinePlayers.clear();
            }
        }
    }

    public void onShutDownDataSave() {
        Inv.log.info("Saving online players data...");
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Player p : onlinePlayers) {
            if (p.isOnline()) {
                m.getInventoryDataHandler().onDataSaveFunction(p, false, "true", null, null);
            }
        }

        Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
    }
}
