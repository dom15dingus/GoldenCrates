package su.nightexpress.goldencrates.api.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.IEvent;
import su.nightexpress.goldencrates.manager.crate.Crate;

public abstract class CrateEvent extends IEvent {

	private Crate crate;
	private Player player;
	
	public CrateEvent(@NotNull Crate crate, @NotNull Player player) {
		this.crate = crate;
		this.player = player;
	}
	
	@NotNull
	public Crate getCrate() {
		return this.crate;
	}
	
	@NotNull
	public Player getPlayer() {
		return this.player;
	}
}
