package su.nightexpress.goldencrates.manager.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.Loadable;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.manager.template.CrateTemplate.SpinTask;

public class TemplateManager implements Loadable {

	private GoldenCrates plugin;
	private Map<String, CrateTemplate> templates;
	
	static final Map<Player, Set<SpinTask>> SPIN_TASKS = new WeakHashMap<>();
	
	public TemplateManager(@NotNull GoldenCrates plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void setup() {
		this.plugin.getConfigManager().extract(Config.DIR_TEMPLATES);
		this.templates = new HashMap<>();
		
		for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + Config.DIR_TEMPLATES, true)) {
			try {
				CrateTemplate template = new CrateTemplate(plugin, cfg);
				this.templates.put(template.getId(), template);
			}
			catch (Exception ex) {
				plugin.error("Could not load crate template: " + cfg.getFile().getName());
				ex.printStackTrace();
			}
		}
		
		this.plugin.info("Templates Loaded: " + templates.size());
	}

	@Override
	public void shutdown() {
		SPIN_TASKS.values().forEach(set -> set.forEach(task -> task.finish(true)));
		SPIN_TASKS.clear();
		
		if (this.templates != null) {
			this.templates.values().forEach(template -> template.clear());
			this.templates.clear();
		}
	}

	public boolean isTemplate(@NotNull String id) {
		return this.getTemplateById(id) != null;
	}

	@Nullable
	public CrateTemplate getTemplateById(@NotNull String id) {
		return this.templates.get(id.toLowerCase());
	}
	
	@NotNull
	public Collection<CrateTemplate> getTemplates() {
		return this.templates.values();
	}
	
	@NotNull
	public List<String> getTemplateIds() {
		return new ArrayList<>(this.templates.keySet());
	}
	
	public static boolean isSpinning(@NotNull Player player) {
		return SPIN_TASKS.containsKey(player);
	}
}
