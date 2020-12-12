package su.nightexpress.goldencrates.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.users.IAbstractUser;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class CrateUser extends IAbstractUser<GoldenCrates> {

	private Map<String, Integer> keys;
	private Map<String, Long> openCooldowns;
	
	public CrateUser(@NotNull GoldenCrates plugin, @NotNull Player player) {
		this(plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis(), 
			new HashMap<>(), 			// Keys
			new HashMap<>()				// Cooldowns
		);
	}
	
	public CrateUser(
			@NotNull GoldenCrates plugin,
			@NotNull UUID uuid,
			@NotNull String name,
			long lastOnline,
			@NotNull Map<String, Integer> keys,
			@NotNull Map<String, Long> openCooldowns
			) {
		super(plugin, uuid, name, lastOnline);
		this.keys = keys;
		this.openCooldowns = openCooldowns;
	}
	
	@NotNull
	public Map<String, Long> getCooldowns() {
		return this.openCooldowns;
	}
	
	public void setCrateCooldown(@NotNull Crate crate) {
		this.openCooldowns.put(crate.getId(), System.currentTimeMillis() + crate.getOpenCooldown() * 1000L);
	}
	
	public boolean isCrateOnCooldown(@NotNull Crate crate) {
		return this.getCrateCooldown(crate) > 0;
	}
	
	public long getCrateCooldown(@NotNull Crate crate) {
		this.openCooldowns.values().removeIf(cd -> cd < System.currentTimeMillis());
		return this.openCooldowns.getOrDefault(crate.getId(), 0L);
	}
	
	@NotNull
	public Map<String, Integer> getKeysMap() {
		return this.keys;
	}
	
	public int addKeys(@NotNull String id, int amount) {
		id = id.toLowerCase();
		this.keys.computeIfAbsent(id, keys -> 0);
		return this.keys.computeIfPresent(id, (crate, keys) -> Math.max(0, keys + amount));
	}
	
	public int takeKeys(@NotNull String id, int amount) {
		return this.addKeys(id, -amount);
	}
	
	public int getKeys(@NotNull String id) {
		return this.keys.getOrDefault(id.toLowerCase(), 0);
	}
}
