package su.nightexpress.goldencrates.manager.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.Loadable;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.config.Config;

public class MenuManager implements Loadable {

	private GoldenCrates plugin;
	private Map<String, CrateMenu> menuMap;
	
	public MenuManager(@NotNull GoldenCrates plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void setup() {
		this.menuMap = new HashMap<>();
		this.plugin.getConfigManager().extract(Config.DIR_MENUS);
		
		for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + Config.DIR_MENUS, true)) {
			try {
				CrateMenu menu = new CrateMenu(plugin, cfg);
				this.menuMap.put(menu.getId(), menu);
			}
			catch (Exception ex) {
				plugin.error("Could not load crate menu: '" + cfg.getFile().getName() + "'");
				ex.printStackTrace();
			}
		}
		
		this.plugin.info("Menus Loaded: " + menuMap.size());
	}

	@Override
	public void shutdown() {
		if (this.menuMap != null) {
			this.menuMap.values().forEach(menu -> menu.clear());
			this.menuMap.clear();
		}
	}

	@Nullable
	public CrateMenu getMenuById(@NotNull String id) {
		return menuMap.get(id.toLowerCase());
	}
	
	@NotNull
	public Collection<CrateMenu> getMenus() {
		return menuMap.values();
	}
	
	@NotNull
	public List<String> getMenuIds() {
		return new ArrayList<>(this.menuMap.keySet());
	}
}
