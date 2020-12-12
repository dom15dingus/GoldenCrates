package su.nightexpress.goldencrates.api.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateReward;

public class CrateOpenEvent extends CrateEvent {

	private List<CrateReward> rewards;
	
	public CrateOpenEvent(@NotNull Crate crate, @NotNull Player player) {
		super(crate, player);
		this.setRewards(new ArrayList<>());
	}

	@NotNull
	public List<CrateReward> getRewards() {
		return this.rewards;
	}
	
	public void setRewards(@NotNull List<CrateReward> rewards) {
		this.rewards = rewards;
	}
}
