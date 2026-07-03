package no.vestlandetmc.BanFromClaim.utils;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanManager {

	private final ClaimData claimData = new ClaimData();

	public void enforceBan(Player player, Location locTo, Location locFrom) {
		if (canBypass(player)) return;

		final RegionHook regionHook = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = regionHook.getRegionID(locTo);

		if (regionID == null) return;

		final boolean isAllBanned = claimData.isAllBanned(regionID);
		final boolean isPlayerBanned = isPlayerBanned(player, regionID);

		if (!isAllBanned && !isPlayerBanned) {
			final Player target = PlayerRidePlayer.getPassenger(player);
			if (target != null && (isPlayerBanned(target, regionID) || claimData.isAllBanned(regionID))
					&& !regionHook.hasTrust(target, regionID) && !canBypass(target)) {
				target.teleport(player.getLocation().add(0, 4, 0));
			}
			return;
		}

		if (regionHook.hasTrust(player, regionID)) return;

		final UUID ownerUUID = regionHook.getOwnerID(regionID);
		if (ownerUUID == null) return;

		boolean hasAttacked = false;
		if (CombatMode.attackerContains(player.getUniqueId()))
			hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

		if (hasAttacked) return;

		final String regionIdFrom = regionHook.getRegionID(locFrom);
		final ParticleHandler ph = new ParticleHandler(locTo);

		if (regionIdFrom != null && regionIdFrom.equals(regionID)) {
			final int sizeRadius = regionHook.sizeRadius(regionID);
			final Location greaterBoundaryCorner = regionHook.getGreaterBoundaryCorner(regionID);
			final Location lesserBoundaryCorner = regionHook.getLesserBoundaryCorner(regionID);

			final LocationFinder lf = new LocationFinder(greaterBoundaryCorner, lesserBoundaryCorner, player.getWorld().getUID(), sizeRadius);
			Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
				if (randomCircumferenceRadiusLoc == null) {
					if (Config.SAFE_LOCATION == null) {
						player.teleport(player.getWorld().getSpawnLocation());
					} else {
						player.teleport(Config.SAFE_LOCATION);
					}
				} else {
					player.teleport(randomCircumferenceRadiusLoc);
				}

			}));

		} else {
			final Location tpLoc = player.getLocation().add(locFrom.toVector().subtract(locTo.toVector()).normalize().multiply(3));
			if (tpLoc.getBlock().getType().equals(Material.AIR)) {
				player.teleport(tpLoc);
			} else {
				final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
				player.teleport(safeLoc);
			}

			ph.drawCircle(1, locTo.getBlockX() == locFrom.getBlockX());
		}

		if (!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
			MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
			MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());

			Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString()), 5L * 20L);
		}
	}

	public void kickPlayer(Player player, Location locTo) {
		if (canBypass(player)) return;

		final RegionHook regionHook = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = regionHook.getRegionID(locTo);

		if (regionID == null) return;

		if ((claimData.isAllBanned(regionID) || isPlayerBanned(player, regionID)) && !regionHook.hasTrust(player, regionID)) {
			final int sizeRadius = regionHook.sizeRadius(regionID);
			final Location greaterBoundaryCorner = regionHook.getGreaterBoundaryCorner(regionID);
			final Location lesserBoundaryCorner = regionHook.getLesserBoundaryCorner(regionID);

			final LocationFinder lf = new LocationFinder(greaterBoundaryCorner, lesserBoundaryCorner, player.getWorld().getUID(), sizeRadius);
			Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
				if (randomCircumferenceRadiusLoc == null) {
					if (Config.SAFE_LOCATION == null) player.teleport(player.getWorld().getSpawnLocation());
					else player.teleport(Config.SAFE_LOCATION);
				} else {
					player.teleport(randomCircumferenceRadiusLoc);
				}
			}));
		}
	}

	public void banPlayer(Player player) {

	}

	private boolean canBypass(Player player) {
		return player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR);
	}

	private boolean isPlayerBanned(Player player, String claimID) {
		return claimData.bannedPlayers(claimID) != null && claimData.bannedPlayers(claimID).contains(player.getUniqueId().toString());
	}
}
