package no.vestlandetmc.BanFromClaim.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerRidePlayer {

	public static Player getPassenger(Player player) {
		final List<Entity> nearby = player.getNearbyEntities(1.0, 4.0, 1.0);

		for (final Entity entity : nearby) {
			if (!(entity instanceof Player target)) {
				continue;
			}

			final int xTarget = target.getLocation().getBlockX();
			final int zTarget = target.getLocation().getBlockZ();
			final int yTarget = target.getLocation().getBlockY();
			final int xPlayer = player.getLocation().getBlockX();
			final int zPlayer = player.getLocation().getBlockZ();
			final int yPlayer = player.getLocation().getBlockY();

			if (xTarget == xPlayer && zTarget == zPlayer && (yTarget > yPlayer && yTarget < (yPlayer + 4))) {
				return target;
			}
		}

		return null;
	}
}
