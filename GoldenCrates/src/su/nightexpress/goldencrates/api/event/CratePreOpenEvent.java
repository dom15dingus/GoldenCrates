package su.nightexpress.goldencrates.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.goldencrates.manager.crate.Crate;

public class CratePreOpenEvent extends CrateEvent implements Cancellable {

	private boolean isCancelled;
	
	public CratePreOpenEvent(@NotNull Crate crate, @NotNull Player player) {
		super(crate, player);
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}
}
