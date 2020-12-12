package su.nightexpress.goldencrates.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.users.IUserManager;
import su.nightexpress.goldencrates.GoldenCrates;

public class UserManager extends IUserManager<GoldenCrates, CrateUser> {
	
	public UserManager(@NotNull GoldenCrates plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected CrateUser createData(@NotNull Player player) {
		return new CrateUser(this.plugin, player);
	}

	@Override
	protected void onUserLoad(@NotNull CrateUser user) {
		super.onUserLoad(user);
		
		Player player = user.getPlayer();
		if (player == null) return;
		
		this.plugin.getKeyManager().giveOfflineKeys(player);
	}
	
}
