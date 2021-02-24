package su.nightexpress.goldencrates.data;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.event.EngineUserLoadEvent;
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
	
	@EventHandler
	public void onUserLoad(EngineUserLoadEvent<GoldenCrates, CrateUser> e) {
		if (!(e.getPlugin() instanceof GoldenCrates)) return;
		
		Player player = e.getUser().getPlayer();
		if (player == null) return;
		
		this.plugin.getKeyManager().giveOfflineKeys(player);
	}
}
