package su.nightexpress.goldencrates.manager.editor.crate;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateReward;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHandler;
import su.nightexpress.goldencrates.manager.editor.CrateEditorType;

public class CrateEditorReward extends NGUI<GoldenCrates> {

	private CrateReward reward;
	
	public CrateEditorReward(@NotNull GoldenCrates plugin, @NotNull CrateReward reward) {
		super(plugin, CrateEditorHandler.CRATE_REWARD_MAIN, "");
		this.reward = reward;
		Crate crate = reward.getCrate();
		
		GuiClick click = (p, type, e) -> {
			if (type == null) return;
			
			ClickType clickType = e.getClick();
			Class<?> clazz = type.getClass();
			if (clazz.equals(ContentType.class)) {
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case EXIT: {
						p.closeInventory();
						break;
					}
					case RETURN: {
						crate.getEditor().openEditorRewards(p);
						break;
					}
					default: {
						break;
					}
				}
				return;
			}
			
			if (clazz.equals(CrateEditorType.class)) {
				CrateEditorType type2 = (CrateEditorType) type;
				
				switch (type2) {
					case CRATE_CHANGE_REWARD_NAME: {
						if (e.isRightClick()) {
							ItemStack item = reward.getItem();
							if (item != null) {
								reward.setName(ItemUT.getItemName(item));
								plugin.getCrateManager().save(crate);
				    			open(p, 1);
							}
							break;
						}
						EditorManager.startEdit(p, reward, type2);
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Name.getMsg());
		    			p.closeInventory();
						break;
					}
					case CRATE_CHANGE_REWARD_PREVIEW: {
						if (clickType == ClickType.MIDDLE) {
		    				ItemUT.addItem(p, reward.getPreview());
		    				return;
		    			}
		    			else {
			    			ItemStack cu = e.getCursor();
				    		if (cu != null && cu.getType() != Material.AIR) {
				    			reward.setPreview(cu);
				    			e.getView().setCursor(null);
				    			plugin.getCrateManager().save(crate);
				    			open(p, 1);
				    		}
		    			}
						break;
					}
					case CRATE_CHANGE_REWARD_ITEM: {
		    			if (clickType == ClickType.MIDDLE) {
		    				ItemStack item = reward.getItem();
		    				if (item != null) {
		    					ItemUT.addItem(p, item);
		    				}
		    				return;
		    			}
		    			else {
		    				if (e.isShiftClick() && e.isRightClick()) {
			    				reward.setItem(null);
			    			}
			    			else {
			    				ItemStack cu = e.getCursor();
				    			if (cu != null && cu.getType() != Material.AIR) {
				    				reward.setItem(cu);
				    				e.getView().setCursor(null);
				    			}
		    				}
		    				plugin.getCrateManager().save(crate);
			    			open(p, 1);
		    			}
						break;
					}
					case CRATE_CHANGE_REWARD_CHANCE: {
						EditorManager.startEdit(p, reward, type2);
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Chance.getMsg());
		    			p.closeInventory();
						break;
					}
					case CRATE_CHANGE_REWARD_COMMANDS: {
						if (e.isRightClick()) {
							List<String> cmds = reward.getCommands();
		    				if (!cmds.isEmpty()) {
		    					cmds.remove(cmds.size()-1);
		    				}
		    				plugin.getCrateManager().save(crate);
		    				open(p, 1);
		    			}
		    			else {
		    				EditorManager.startEdit(p, reward, type2);
		        			EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Command.getMsg());
		        			EditorManager.sendCommandTips(p);
		        			p.closeInventory();
		        			return;
		    			}
						break;
					}
					default: {
						break;
					}
				}
			}
		};
		
		JYML cfg = CrateEditorHandler.CRATE_REWARD_MAIN;
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
		
	}

	@Override
	protected boolean cancelClick(int slot) {
		return true;
	}

	@Override
	protected boolean cancelPlayerClick() {
		return false;
	}

	@Override
	protected boolean ignoreNullClick() {
		return false;
	}

	@Override
	protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		super.replaceMeta(player, item, guiItem);
		
		Enum<?> type = guiItem.getType();
		if (type == CrateEditorType.CRATE_CHANGE_REWARD_PREVIEW) {
			item.setType(reward.getPreview().getType());
			item.setAmount(reward.getPreview().getAmount());
		}
		else if (type == CrateEditorType.CRATE_CHANGE_REWARD_ITEM) {
			ItemStack rewardItem = reward.getItem();
			if (rewardItem != null) {
				item.setType(rewardItem.getType());
				item.setAmount(rewardItem.getAmount());
			}
		}
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null || lore.isEmpty()) return;
		
		ItemStack ritem = reward.getItem();
		String itemName = ritem == null ? "<No Item>" : ItemUT.getItemName(ritem);
		
		lore.replaceAll(line -> line
			.replace("%name%", reward.getName())
			.replace("%chance%", String.valueOf(reward.getChance()))
			.replace("%item%", itemName)
			.replace("%preview%", ItemUT.getItemName(reward.getPreview()))
		);
		
		List<String> lore2 = new ArrayList<>();
		for (String line : new ArrayList<>(lore)) {
			if (line.contains("%commands-list%")) {
				for (String cmd : reward.getCommands()) {
					String l2 = line.replace("%commands-list%", cmd).replace("%player%", player.getName());
					lore2.add(l2);
				}
				continue;
			}
			else {
				lore2.add(line);
			}
		}
		
		meta.setLore(lore2);
		item.setItemMeta(meta);
	}
}
