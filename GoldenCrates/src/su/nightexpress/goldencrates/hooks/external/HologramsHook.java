package su.nightexpress.goldencrates.hooks.external;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class HologramsHook extends NHook<GoldenCrates> {

	private Map<String, Set<Hologram>> holoCrates;
	
	public HologramsHook(@NotNull GoldenCrates plugin) {
		super(plugin);
	}

	@Override 
	@NotNull
	protected HookState setup() {
		this.holoCrates = new HashMap<>();
		
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		if (this.holoCrates != null) {
			this.holoCrates.values().forEach(set -> set.forEach(holo -> holo.delete()));
			this.holoCrates.clear();
			this.holoCrates = null;
		}
	}

	public void create(@NotNull Crate crate) {
		String id = crate.getId();

		crate.getBlockLocations().forEach(loc -> {
			Hologram hologram = HologramsAPI.createHologram(plugin, crate.getBlockHologramLocation(loc));
			for (String line : crate.getHologramText()) {
				hologram.appendTextLine(line);
			}
			this.holoCrates.computeIfAbsent(id, set -> new HashSet<>()).add(hologram);
		});
	}
	
	public void remove(@NotNull Crate crate) {
		Set<Hologram> set = this.holoCrates.get(crate.getId());
		if (set == null) return;
		
		set.forEach(holo -> holo.delete());
		this.holoCrates.remove(crate.getId());
	}
}
