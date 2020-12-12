package su.nightexpress.goldencrates.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class PlaceholderHK extends NHook<GoldenCrates> {

	private CratesExpansion expansion;
	
	public PlaceholderHK(@NotNull GoldenCrates plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		this.expansion = new CratesExpansion();
		this.expansion.register();
		
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		if (this.expansion != null) {
			this.expansion.unregister();
		}
	}

	class CratesExpansion extends PlaceholderExpansion {

		@Override
		public boolean persist() {
			return true;
		}
		
		@Override
		@NotNull
		public String getAuthor() {
			return plugin.getAuthor();
		}

		@Override
		@NotNull
		public String getIdentifier() {
			return plugin.getName().toLowerCase();
		}

		@Override
		@NotNull
		public String getVersion() {
			return plugin.getDescription().getVersion();
		}
		
		@Override
		public String onPlaceholderRequest(@Nullable Player player, String tmp) {
			if (player == null || tmp == null) return null;
    	   	
    	   	if (tmp.startsWith("keys_")) {
    	   		String id = tmp.replace("keys_", "");
    	   		Crate crate = plugin.getCrateManager().getCrateById(id);
    	   		if (crate != null) {
    	   			int keys = plugin.getKeyManager().getKeys(player, crate);
    	   			return String.valueOf(keys);
    	   		}
    	   	}
    	   	else if (tmp.startsWith("cooldown_")) {
    	   		String id = tmp.replace("cooldown_", "");
    	   		Crate crate = plugin.getCrateManager().getCrateById(id);
    	   		if (crate != null) {
    	   			CrateUser user = plugin.getUserManager().getOrLoadUser(player);
    	   			if (user == null) return "?";
    	   			
    	   			long left = user.getCrateCooldown(crate);
    	   			if (left == 0) return plugin.lang().Crate_Placeholder_Cooldown_Blank.getMsg();
    	   			return TimeUT.formatTimeLeft(left);
    	   		}
    	   	}
			
			return null;
		}
	}
}
