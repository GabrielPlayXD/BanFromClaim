package no.vestlandetmc.BanFromClaim.utils;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.handler.CallbackReturnLocation;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

public class LocationFinder {

	private final Location circumferenceCenter;
	private final UUID circumferenceWorldUUID;
	private int circumferenceRadius;

	public LocationFinder(Location loc1, Location loc2, UUID circumferenceWorldUUID, int circumferenceRadius) {
		this.circumferenceCenter = findCenter(loc1, loc2);
		this.circumferenceWorldUUID = circumferenceWorldUUID;
		this.circumferenceRadius = circumferenceRadius;
	}

	/**
	 * [Run asynchronously] Callback returns "safe" location outside a claim if found, if not found returns null (Uses expanding iterating circumferences method)
	 */
	public void IterateCircumferences(CallbackReturnLocation callback) {
		final World circumferenceWorld = Bukkit.getWorld(this.circumferenceWorldUUID);
		if (circumferenceWorld == null) return;

		final BfcPlugin plugin = BfcPlugin.getPlugin();
		final Location checkLoc = new Location(circumferenceWorld, 0, 120, 0);
		Location resultLoc = null;

		final int maxCircleIterations = 10;
		final int checkLocationsPerCircumference = 4;
		final int maxSafeLocationFailures = 5;
		int safeLocationChecks = 0;

		outer:
		for (int i = 0; i < maxCircleIterations; i++) { //Circle radius iteration
			circumferenceRadius *= 2;

			for (int j = 0; j < checkLocationsPerCircumference; j++) { //Circumference position + check within claim
				updateRandomCircumferenceLoc(checkLoc, this.circumferenceCenter, circumferenceRadius);

				if (!hasClaim(checkLoc)) {
					safeLocationChecks++;

					final Block highestBlock = circumferenceWorld.getHighestBlockAt(checkLoc);

					if (SafeLocationCheck.BlockSafetyCheck(highestBlock)) {
						resultLoc = highestBlock.getLocation().add(0.5, 1, 0.5);
						break outer;
					} else if (safeLocationChecks < maxSafeLocationFailures)
						j = 0; //Reset circumference position search unless it's the last safe check
				}
			}
		}

		final Location finalResultLoc = resultLoc;
		Bukkit.getScheduler().runTask(plugin, () -> callback.onDone(finalResultLoc));
	}

	/**
	 * Updates the provided Location with a random position from a circumference of circumferenceRadius and circumferenceCenter
	 */
	private void updateRandomCircumferenceLoc(Location loc, Location circumferenceCenter, int circumferenceRadius) {
		final double randomAngle = Math.random() * Math.PI * 2;
		loc.setX(circumferenceCenter.getX() + Math.cos(randomAngle) * circumferenceRadius);
		loc.setY(120);
		loc.setZ(circumferenceCenter.getZ() + Math.sin(randomAngle) * circumferenceRadius);
	}

	private Location findCenter(Location loc1, Location loc2) {
		final int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		final int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		final int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		final int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

		return new Location(loc1.getWorld(), minX + (maxX - minX) / 2D, 64D, minZ + (maxZ - minZ) / 2D);
	}

	private boolean hasClaim(Location loc) {
		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(loc);
		return regionID != null;
	}

}
