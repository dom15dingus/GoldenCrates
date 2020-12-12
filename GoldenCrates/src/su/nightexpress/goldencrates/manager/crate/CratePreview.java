package su.nightexpress.goldencrates.manager.crate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nightexpress.goldencrates.GoldenCrates;

public class CratePreview extends NGUI<GoldenCrates> {
	
	private Crate crate;
	private static int[] REWARD_SLOTS;
	
	public CratePreview(@NotNull GoldenCrates plugin, @NotNull Crate crate) {
		super(plugin, plugin.cfg().getJYML(), "crates.preview.gui.");
		JYML cfg = plugin.cfg().getJYML();
		String path = "crates.preview.gui.";
		
		this.title = this.title.replace("%crate%", crate.getName());
		this.crate = crate;
		REWARD_SLOTS = cfg.getIntArray(path + "reward-slots");
		
		GuiClick click = new GuiClick() {
			@Override
			public void click(@NotNull Player p, @Nullable  Enum<?> type,
					@NotNull InventoryClickEvent e) {
				if (type == null || !type.getClass().equals(ContentType.class)) return;
				
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case BACK: {
						open(p, getUserPage(p, 0) - 1);
						break;
					}
					case EXIT: {
						p.closeInventory();
						break;
					}
					case NEXT: {
						open(p, getUserPage(p, 0) + 1);
						break;
					}
					default: {
						break;
					}
				}
			}
		};
		
		for (String id : cfg.getSection(path + "content")) {
			GuiItem guiItem = cfg.getGuiItem(path + "content." + id, ContentType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			
			this.addButton(guiItem);
		}
	}

	@Override
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
		int len = REWARD_SLOTS.length;
		List<CrateReward> list = new ArrayList<>(crate.getRewards());
		List<List<CrateReward>> split = CollectionsUT.split(list, len);
		
		int pages = split.size();
		if (pages < 1) list = Collections.emptyList();
		else list = split.get(page - 1);
		
		int count = 0;
		for (CrateReward reward : list) {
			JIcon icon = new JIcon(reward.getPreviewFormatted());
			this.addButton(player, icon, REWARD_SLOTS[count++]);
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
