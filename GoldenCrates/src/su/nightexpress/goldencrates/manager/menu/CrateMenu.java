package su.nightexpress.goldencrates.manager.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class CrateMenu extends LoadableItem implements Cleanable {
	
	private CrateMenu.GUI gui;
	
	public CrateMenu(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
	}
	
	@Override
	protected void save(@NotNull JYML cfg) {
		
	}

	@Override
	public void clear() {
		if (this.gui != null) {
			this.gui.shutdown();
			this.gui = null;
		}
	}
	
	public void open(@NotNull Player player) {
		if (this.gui == null) {
			this.gui = new CrateMenu.GUI((GoldenCrates) plugin, this.getConfig(), "");
		}
		this.gui.open(player, 1);
	}

	class GUI extends NGUI<GoldenCrates> {

		GUI(@NotNull GoldenCrates plugin, @NotNull JYML cfg, @NotNull String path) {
			super(plugin, cfg, path);
			
			GuiClick click = (p, type, e) -> {
				if (type == null || !type.getClass().equals(ContentType.class)) return;
				
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case EXIT: {
						p.closeInventory();
						break;
					}
					default: {
						break;
					}
				}
			};
			
			for (String id : cfg.getSection("content")) {
				GuiItem guiItem = cfg.getGuiItem("content." + id, ContentType.class);
				if (guiItem == null) continue;
				
				if (guiItem.getType() != null) {
					guiItem.setClick(click);
				}
				
				this.addButton(guiItem);
			}
			
			GuiClick clickCrate = (p, type, e) -> {
				int slot = e.getRawSlot();
				
				GuiItem guiItem = this.getButton(p, slot);
				if (guiItem == null) return;
				
				String crateId = guiItem.getId();
				Crate crate = plugin.getCrateManager().getCrateById(crateId);
				if (crate == null) return;
				
				if (e.isRightClick()) {
					crate.openPreview(p);
					return;
				}
				p.closeInventory();
				plugin.getCrateManager().openCrate(p, crate, null, null);
			};
			
			for (String id : cfg.getSection("crates")) {
				GuiItem guiItem = cfg.getGuiItem("crates." + id);
				if (guiItem == null) continue;
				
				Crate crate = plugin.getCrateManager().getCrateById(id);
				if (crate == null) {
					plugin.error("Invalid crate '" + id + "' in '" + getId() + "' menu!");
					continue;
				}
				
				guiItem.setClick(clickCrate);
				this.addButton(guiItem);
			}
		}

		@Override
		protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {

		}
		
		@Override
		protected void replaceMeta(@NotNull Player p, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
			super.replaceMeta(p, item, guiItem);
			
			ItemMeta meta = item.getItemMeta();
			if (meta == null) return;
			
			Crate crate = plugin.getCrateManager().getCrateById(guiItem.getId());
			if (crate == null) return;
			
			ItemStack crateIcon = crate.getItem();
			ItemMeta iconMeta = crateIcon.getItemMeta();
			
			String iconName = iconMeta != null ? iconMeta.getDisplayName() : "";
			List<String> iconLore = iconMeta != null ? iconMeta.getLore() : null;
			
			meta.setDisplayName(meta.getDisplayName().replace("%crate_item_name%", iconName));
			
			List<String> lore = meta.getLore();
			List<String> lore2 = new ArrayList<>();
			if (lore != null) {
				for (String line : lore) {
					if (line.equalsIgnoreCase("%crate_item_lore%")) {
						if (iconLore != null) {
							lore2.addAll(iconLore);
						}
						continue;
					}
					lore2.add(line);
				}
			}
			lore2.replaceAll(line -> line
				.replace("%keys-amount%", String.valueOf(plugin.getKeyManager().getKeys(p, crate)))
				.replace("%cost-vault%", NumberUT.format(crate.getOpenCostVault()))
				.replace("%cost-exp%", NumberUT.format(crate.getOpenCostExp()))
			);
			
			meta.setLore(lore2);
			item.setItemMeta(meta);
		}

		@Override
		protected boolean cancelClick(int slot) {
			return true;
		}
	
		@Override
		protected boolean cancelPlayerClick() {
			return true;
		}
	
		@Override
		protected boolean ignoreNullClick() {
			return true;
		}
	}
}
