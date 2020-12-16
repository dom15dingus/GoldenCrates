package su.nightexpress.goldencrates.manager.editor.crate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateReward;
import su.nightexpress.goldencrates.manager.editor.CrateEditorType;

public class CrateEditorRewards extends NGUI<GoldenCrates> {

	private static int[] objSlots;
	private static String objName;
	private static List<String> objLore;
	
	private Crate crate;
	
	public CrateEditorRewards(@NotNull GoldenCrates plugin, @NotNull Crate crate) {
		super(plugin, GoldenCrates.EDITOR_REWARD_LIST, "");
		this.crate = crate;
		
		JYML cfg = GoldenCrates.EDITOR_REWARD_LIST;
		objSlots = cfg.getIntArray("object-slots");
		objName = StringUT.color(cfg.getString("object-name", "&7Reward #%num%"));
		objLore = StringUT.color(cfg.getStringList("object-lore"));
		
		GuiClick click = new GuiClick() {
			@Override
			public void click(Player p, @Nullable Enum<?> type, InventoryClickEvent e) {
				if (type == null) return;
				
				Class<?> c = type.getClass();
				if (c.equals(ContentType.class)) {
					ContentType type2 = (ContentType) type;
					switch (type2) {
						case EXIT: {
							p.closeInventory();
							break;
						}
						case RETURN: {
							crate.getEditor().open(p, 1);
							break;
						}
						case NEXT: {
							open(p, getUserPage(p, 0) + 1);
							break;
						}
						case BACK: {
							open(p, getUserPage(p, 0) - 1);
							break;
						}
						default: {
							break;
						}
					}
				}
				else if (c.equals(CrateEditorType.class)) {
					CrateEditorType type2 = (CrateEditorType) type;
					switch (type2) {
						case CRATE_CREATE_REWARD: {
							crate.createReward();
			    			plugin.getCrateManager().save(crate);
			    			open(p, getUserPage(p, 0));
							break;
						}
						case CRATE_CHANGE_REWARD_AMOUNT: {
							if (e.isLeftClick()) {
								if (e.isShiftClick()) {
									crate.setMinRewards(crate.getMinRewards() - 1);
								}
								else {
									crate.setMinRewards(crate.getMinRewards() + 1);
								}
							}
							else if (e.isRightClick()) {
								if (e.isShiftClick()) {
									crate.setMaxRewards(crate.getMaxRewards() - 1);
								}
								else {
									crate.setMaxRewards(crate.getMaxRewards() + 1);
								}
							}
							plugin.getCrateManager().save(crate);
							open(p, getUserPage(p, 0));
							break;
						}
						case CRATE_CHANGE_REWARD_BROADCAST: {
							crate.setRewardBroadcast(!crate.isRewardBroadcast());
							plugin.getCrateManager().save(crate);
							open(p, 1);
							break;
						}
						default: {
							break;
						}
					}
				}
			}
		};
		
			
		for (String sId : cfg.getSection("content")) {
			GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addButton(guiItem);
		}
		
		for (String sId : cfg.getSection("editor")) {
			GuiItem guiItem = cfg.getGuiItem("editor." + sId, CrateEditorType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addButton(guiItem);
		}
	}
	
	@Override
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
		int len = objSlots.length;
		List<CrateReward> list = new ArrayList<>(crate.getRewards());
		List<List<CrateReward>> split = CollectionsUT.split(list, len);
		
		int pages = split.size();
		if (pages < 1) list = Collections.emptyList();
		else list = split.get(page - 1);
		
		int count = 0;
		for (CrateReward reward : list) {
			JIcon icon = new JIcon(reward.getPreview());
			icon.setName(objName.replace("%num%", String.valueOf(count)));
			icon.clearLore();
			for (String line : objLore) {
				icon.addLore(line
					.replace("%name%", reward.getName())
					.replace("%chance%", NumberUT.format(reward.getChance())));
			}
			
			icon.setClick((p2, type, e) -> {
				
				// Reward deletion.
				if (e.getClick() == ClickType.MIDDLE) {
    	    		crate.deleteReward(reward.getId());
    	    		plugin.getCrateManager().save(crate);
    	    		open(p2, 1);
    	    		return;
    	    	}
				
				if (e.isShiftClick()) {
					
					// Reward position move.
					List<CrateReward> all = new ArrayList<>(this.crate.getRewards());
					int index = all.indexOf(reward);
					int allSize = all.size();
					
					if (e.isLeftClick()) {
						if (index + 1 >= allSize) return;
						
						all.remove(index);
						all.add(index + 1, reward);
					}
					else if (e.isRightClick()) {
						if (index == 0) return;
						
						all.remove(index);
						all.add(index - 1, reward);
					}
					this.crate.setRewards(all);
					this.open(p2, page);
					return;
				}
				
				if (e.isLeftClick()) {
    				reward.getEditor().open(p2, 1);
    				return;
    	    	}
			});
			
			this.addButton(player, icon, objSlots[count++]);
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

	@Override
	protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		super.replaceMeta(player, item, guiItem);
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null || lore.isEmpty()) return;
		
		lore.replaceAll(line -> line
			.replace("%amount-min%", String.valueOf(crate.getMinRewards()))
			.replace("%amount-max%", String.valueOf(crate.getMaxRewards()))
			.replace("%broadcast%", plugin.lang().getBool(crate.isRewardBroadcast()))
		);
		
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
