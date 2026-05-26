package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		final Player player = e.getPlayer();

		if (player.isOp()) {
			if (UpdateNotification.isUpdateAvailable()) {
				MessageHandler.sendMessage(player, Messages.UPDATE_AVAILABLE.replace("%plugin%", BfcPlugin.getPlugin().getPluginMeta().getName()));
				MessageHandler.sendMessage(player, Messages.UPDATE_VERSION.replace("%current%", UpdateNotification.getCurrentVersion()).replace("%latest%", UpdateNotification.getLatestVersion()));
				MessageHandler.sendMessage(player, Messages.UPDATE_LINK.replace("%slug%", UpdateNotification.getProjectSlug()));
			}
		}

		Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> BfcPlugin.getBanManager().kickPlayer(player, e.getPlayer().getLocation()), 10L);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent e) {
		final Player player = e.getPlayer();
		BfcPlugin.getBanManager().kickPlayer(player, e.getTo());
	}

	@EventHandler(ignoreCancelled = true)
	public void playerRespawn(PlayerRespawnEvent e) {
		final Player player = e.getPlayer();
		Location loc = e.getRespawnLocation();
		BfcPlugin.getBanManager().kickPlayer(player, loc);
	}

}
