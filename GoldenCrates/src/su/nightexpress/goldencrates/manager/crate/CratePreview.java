package su.nightexpress.goldencrates.manager.crate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.goldencrates.GoldenCrates;

public class CratePreview extends NGUI<GoldenCrates> {
	
	private Crate crate;
	private int[] rewardSlots;
	private List<String> rewardLore;
	
	public CratePreview(@NotNull GoldenCrates plugin, @NotNull Crate crate, @NotNull JYML cfg) {
		super(plugin, cfg, "");
		
		this.crate = crate;
		this.title = this.title.replace("%crate%", crate.getName());
		this.rewardSlots = cfg.getIntArray("reward-slots");
		this.rewardLore = StringUT.color(cfg.getStringList("reward-lore"));
		
		GuiClick click = (p, type, e) -> {
			if (type == null || !type.getClass().equals(ContentType.class)) return;
			
			ContentType type2 = (ContentType) type;
			switch (type2) {
				case EXIT: {
					p.closeInventory();
					break;
				}
				case BACK: {
					this.open(p, this.getUserPage(p, 0) - 1);
					break;
				}
				case NEXT: {
					this.open(p, this.getUserPage(p, 0) + 1);
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
	}

	@Override
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
		int len = rewardSlots.length;
		List<CrateReward> list = new ArrayList<>(crate.getRewards());
		List<List<CrateReward>> split = CollectionsUT.split(list, len);
		
		int pages = split.size();
		if (pages < 1) list = Collections.emptyList();
		else list = split.get(page - 1);
		
		int count = 0;
		for (CrateReward reward : list) {
			ItemStack item = reward.getPreview();
			ItemMeta meta = item.getItemMeta();
			if (meta == null) continue;
			
			if (!meta.hasDisplayName()) {
				meta.setDisplayName(reward.getName());
			}
			
			List<String> lore = new ArrayList<>();
			for (String line : this.rewardLore) {
				if (line.equalsIgnoreCase("%reward-lore%")) {
					List<String> mLore = meta.getLore();
					if (mLore != null) {
						for (String lineMeta : mLore) {
							lore.add(lineMeta);
						}
					}
					continue;
				}
				lore.add(line.replace("%reward-chance%", NumberUT.format(reward.getChance())));
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			JIcon icon = new JIcon(item);
			this.addButton(player, icon, rewardSlots[count++]);
		}
		
		this.setUserPage(player, page, pages);
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
