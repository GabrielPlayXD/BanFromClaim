package no.vestlandetmc.BanFromClaim.config;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClaimData {

	private static final FileConfiguration cfg = BfcPlugin.getDataFile();
	private static final String prefix = "bfc_claim_data";
	private static final String banAllPrefix = "claims-ban-all";

	private static final Map<String, Set<String>> bannedPlayersCache = new HashMap<>();
	private static final Set<String> banAllCache = new HashSet<>();

	public ClaimData() {

	}

	public static void loadCache() {
		bannedPlayersCache.clear();
		banAllCache.clear();

		final ConfigurationSection bannedSection = cfg.getConfigurationSection(prefix);
		if (bannedSection != null) {
			for (final String claimID : bannedSection.getKeys(false)) {
				final List<String> players = cfg.getStringList(prefix + "." + claimID);
				if (!players.isEmpty()) {
					bannedPlayersCache.put(claimID, new HashSet<>(players));
				}
			}
		}

		final ConfigurationSection banAllSection = cfg.getConfigurationSection(banAllPrefix);
		if (banAllSection != null) {
			for (final String claimID : banAllSection.getKeys(false)) {
				if (cfg.getBoolean(banAllPrefix + "." + claimID + ".ban-all")) {
					banAllCache.add(claimID);
				}
			}
		}
	}

	public boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		if (add) {
			if (!existData(claimID, bannedUUID)) {
				addData(claimID, bannedUUID);
				return true;
			} else {
				return false;
			}
		} else {
			if (existData(claimID, bannedUUID)) {
				removeData(claimID, bannedUUID);
				return true;
			} else {
				return false;
			}
		}
	}

	public void changeRegionID(String oldID, String newID) {
		if (bannedPlayersCache.containsKey(oldID) || banAllCache.contains(oldID)) {
			final Set<String> players = bannedPlayersCache.remove(oldID);
			final boolean banAll = banAllCache.remove(oldID);

			if (players != null) {
				bannedPlayersCache.put(newID, players);
				cfg.set(prefix + "." + newID, new ArrayList<>(players));
			}

			if (banAll) {
				banAllCache.add(newID);
				cfg.set(banAllPrefix + "." + newID + ".ban-all", true);
			}

			cfg.set(prefix + "." + oldID, null);
			cfg.set(banAllPrefix + "." + oldID, null);
			saveDatafile();
		}
	}

	private void addData(String claimID, String bannedUUID) {
		final Set<String> players = bannedPlayersCache.computeIfAbsent(claimID, k -> new HashSet<>());
		players.add(bannedUUID);

		cfg.set(prefix + "." + claimID, new ArrayList<>(players));
		saveDatafile();
	}

	public void banAll(String claimID) {
		if (banAllCache.contains(claimID)) {
			banAllCache.remove(claimID);
			cfg.set(banAllPrefix + "." + claimID + ".ban-all", false);
		} else {
			banAllCache.add(claimID);
			cfg.set(banAllPrefix + "." + claimID + ".ban-all", true);
		}

		saveDatafile();
	}

	public boolean isAllBanned(String claimID) {
		return banAllCache.contains(claimID);
	}

	private void removeData(String claimID, String bannedUUID) {
		final Set<String> players = bannedPlayersCache.get(claimID);
		if (players != null) {
			if (players.remove(bannedUUID)) {
				if (players.isEmpty()) {
					bannedPlayersCache.remove(claimID);
					cfg.set(prefix + "." + claimID, null);
				} else {
					cfg.set(prefix + "." + claimID, new ArrayList<>(players));
				}
				saveDatafile();
			}
		}
	}

	private boolean existData(String claimID, String bannedUUID) {
		final Set<String> players = bannedPlayersCache.get(claimID);
		return players != null && players.contains(bannedUUID);
	}

	public boolean checkClaim(String claimID) {
		return bannedPlayersCache.containsKey(claimID);
	}

	public List<String> bannedPlayers(String claimID) {
		final Set<String> players = bannedPlayersCache.get(claimID);
		return players != null ? new ArrayList<>(players) : null;
	}

	public static void saveDatafile() {
		try {
			final File file = new File(BfcPlugin.getPlugin().getDataFolder(), "data.dat");
			BfcPlugin.getDataFile().save(file);
		} catch (final IOException e) {
			BfcPlugin.getPlugin().getLogger().severe(e.getMessage());
		}
	}

	public static void createSection() {
		if (!cfg.contains(prefix)) {
			cfg.createSection(prefix);
		}
		if (!cfg.contains(banAllPrefix)) {
			cfg.createSection(banAllPrefix);
		}
		saveDatafile();
		loadCache();
	}
}
