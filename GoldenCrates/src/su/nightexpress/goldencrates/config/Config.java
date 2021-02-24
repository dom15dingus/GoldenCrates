package su.nightexpress.goldencrates.config;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nightexpress.goldencrates.GoldenCrates;

public class Config extends IConfigTemplate {
	
	public static boolean 		CRATE_BLOCK_NO_VISUAL_DROP;
	
	public static final String DIR_CRATES = 	"/crates/";
	public static final String DIR_PREVIEWS = 	"/previews/";
	public static final String DIR_KEYS = 		"/keys/";
	public static final String DIR_MENUS = 		"/menu/";
	public static final String DIR_TEMPLATES = 	"/templates/";
	
	public Config(@NotNull GoldenCrates plugin) {
		super(plugin);
	}

	@Override
	public void load() {
		cfg.remove("crates.preview");
		cfg.addMissing("crates.block.disable-visual-drop", false);
		cfg.addMissing("crates.menu.lore", Arrays.asList(
				"%crate_lore%", 
				"&7", 
				"&bYou have &a%keys% &bkeys!", 
				"&bCrate Open Cost: &c%cost%$"));
		
    	CRATE_BLOCK_NO_VISUAL_DROP = cfg.getBoolean("crates.block.disable-visual-drop");
	}
}
