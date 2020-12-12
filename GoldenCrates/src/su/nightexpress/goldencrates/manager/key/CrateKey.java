package su.nightexpress.goldencrates.manager.key;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.Editable;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.manager.editor.key.KeyEditorKey;

public class CrateKey extends LoadableItem implements Editable, Cleanable {

	private String name;
	private boolean isVirtual;
	private ItemStack item;
	
	private KeyEditorKey editor;
	
	public CrateKey(@NotNull GoldenCrates plugin, @NotNull String id) {
		super(plugin, plugin.getDataFolder() + Config.DIR_KEYS + "/" + id.toLowerCase() + ".yml");
		
		String capital = WordUtils.capitalizeFully(this.getId());
		
		this.setName("&d" + capital + " Key");
		this.setVirtual(false);
		
		JIcon icon = new JIcon(Material.TRIPWIRE_HOOK);
		icon.setName(this.getName());
		icon.setLore(Arrays.asList("&bUse this key to open &d" + capital + " &bcrate!"));
		this.setItem(icon.build());
	}
	
	public CrateKey(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.setName(cfg.getString("name", cfg.getFile().getName().replace(".yml", "")));
		this.setVirtual(cfg.getBoolean("virtual"));
		ItemStack item = cfg.getItem("item", true);
		if (ItemUT.isAir(item) && !this.isVirtual()) {
			throw new IllegalStateException("Key item can not be AIR!");
		}
		this.setItem(item);
	}

	@Override
	protected void save(@NotNull JYML cfg) {
		cfg.set("name", this.getName());
		cfg.set("virtual", this.isVirtual());
		cfg.setItem("item", this.getItem());
	}
	
	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.shutdown();
			this.editor = null;
		}
	}

	@Override
	@NotNull
	public KeyEditorKey getEditor() {
		if (this.editor == null) {
			this.editor = new KeyEditorKey((GoldenCrates) plugin, this);
		}
		return this.editor;
	}

	@NotNull
	public String getName() {
		return name;
	}
	
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}
	
	public boolean isVirtual() {
		return isVirtual;
	}
	
	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}
	
	@NotNull
	public ItemStack getItem() {
		return new ItemStack(item);
	}
	
	public void setItem(@NotNull ItemStack item) {
		this.item = new ItemStack(item);
		DataUT.setData(this.item, KeyManager.TAG_KEY, this.getId());
	}
}
