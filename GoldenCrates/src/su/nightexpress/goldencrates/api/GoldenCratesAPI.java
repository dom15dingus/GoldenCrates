package su.nightexpress.goldencrates.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.data.users.IUserManager;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.manager.crate.CrateManager;
import su.nightexpress.goldencrates.manager.key.KeyManager;
import su.nightexpress.goldencrates.manager.menu.MenuManager;
import su.nightexpress.goldencrates.manager.template.TemplateManager;

public class GoldenCratesAPI {

	private static GoldenCrates plugin = GoldenCrates.getInstance();
	
	@Nullable
	public static CrateUser getUserData(@NotNull Player player) {
		return plugin.getUserManager().getOrLoadUser(player);
	}
	
	@NotNull
	public static IUserManager<GoldenCrates, CrateUser> getUserManager() {
		return plugin.getUserManager();
	}
	
	@NotNull
	public static CrateManager getCrateManager() {
		return plugin.getCrateManager();
	}
	
	@NotNull
	public static KeyManager getKeyManager() {
		return plugin.getKeyManager();
	}
	
	@NotNull
	public static MenuManager getMenuManager() {
		return plugin.getMenuManager();
	}
	
	@NotNull
	public static TemplateManager getTemplateManager() {
		return plugin.getTemplateManager();
	}
}
