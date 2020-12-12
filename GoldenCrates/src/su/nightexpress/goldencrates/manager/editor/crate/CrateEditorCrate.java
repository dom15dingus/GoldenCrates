package su.nightexpress.goldencrates.manager.editor.crate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateEffect;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHub;
import su.nightexpress.goldencrates.manager.editor.CrateEditorType;

public class CrateEditorCrate extends NGUI<GoldenCrates> {

	private Crate crate;
	private CrateEditorRewards editorRewards;
	
	public CrateEditorCrate(@NotNull GoldenCrates plugin, @NotNull Crate crate) {
		super(plugin, GoldenCrates.EDITOR_CRATE, "");
		this.crate = crate;
		
		GuiClick clickHandler = (p, type, e) -> {
			if (type == null) return;
				
			Class<?> clazz = type.getClass();
			if (clazz.equals(ContentType.class)) {
				ContentType type2 = (ContentType) type;
				switch (type2) {
					case EXIT: {
						p.closeInventory();
						break;
					}
					case RETURN: {
						EditorHandler<GoldenCrates> editorHandler = plugin.getEditorHandler();
						if (editorHandler == null) return;
						
						CrateEditorHub main = (CrateEditorHub) editorHandler.getMainEditor();
			    		if (main != null) main.getCratesEditor().open(p, 1);
						break;
					}
					default: {
						break;
					}
				}
			}
			else if (clazz.equals(CrateEditorType.class)) {
				CrateEditorType type2 = (CrateEditorType) type;
				ClickType click = e.getClick();
				
				switch (type2) {
					case CRATE_DELETE: {
						if (!e.isShiftClick()) return;
						
						p.closeInventory();
						plugin.getCrateManager().delete(crate);
						
						EditorHandler<GoldenCrates> editorHandler = plugin.getEditorHandler();
						if (editorHandler == null) return;
						
						CrateEditorHub main = (CrateEditorHub) editorHandler.getMainEditor();
			    		if (main != null) main.getCratesEditor().open(p, 1);
						return;
					}
					case CRATE_CHANGE_BLOCK_HOLOGRAM: {
						if (click == ClickType.MIDDLE) {
		    				crate.setHologramEnabled(!crate.isHologramEnabled());
		    			}
		    			else {
		    				if (e.isLeftClick()) {
		    					EditorManager.startEdit(p, crate, type2);
		        				EditorManager.tipCustom(p, plugin.lang().Editor_Tip_HologramText.getMsg());
		            			p.closeInventory();
		            			return;
		    				}
		    				else if (e.isRightClick()) {
		    					crate.setHologramText(new ArrayList<>());
		    				}
		    			}
						break;
					}
					case CRATE_CHANGE_ITEM: {
						if (e.getClick() == ClickType.MIDDLE) {
							ItemUT.addItem(p, crate.getItem());
							return;
						}
						
						ItemStack cursor = e.getCursor();
						if (cursor == null || ItemUT.isAir(cursor)) return;
						
						crate.setItem(cursor);
						e.getView().setCursor(null);
						
						return;
					}
					case CRATE_CHANGE_BLOCK_LOCATION: {
						if (e.isLeftClick()) {
							EditorManager.startEdit(p, crate, type2);
							EditorManager.tipCustom(p, plugin.lang().Editor_Tip_BlockLocation.getMsg());
							p.closeInventory();
						}
						else {
							if (e.isRightClick()) {
								crate.getBlockLocations().clear();
							}
							if (e.getClick() == ClickType.MIDDLE) {
								crate.setBlockPushbackEnabled(!crate.isBlockPushbackEnabled());
							}
							break;
						}
						return;
					}
					case CRATE_CHANGE_BLOCK_EFFECTS: {
						if (e.isLeftClick()) {
							CrateEffect effect = crate.getBlockEffect();
							effect.setModel(CollectionsUT.toggleEnum(effect.getModel()));
						}
						else if (e.isRightClick()) {
							EditorManager.startEdit(p, crate, CrateEditorType.CRATE_CHANGE_BLOCK_EFFECTS_PARTICLE);
							EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Name.getMsg());
		        			p.closeInventory();
		        			
		        			List<String> items = Arrays.asList(Particle.values()).stream()
		        					.map(Particle::name).collect(Collectors.toList());
		        			EditorManager.sendClickableTips(p, items);
		        			return;
						}
						break;
					}
					case CRATE_CHANGE_COOLDOWN: {
						EditorManager.startEdit(p, crate, type2);
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Cooldown.getMsg());
			   			p.closeInventory();
						return;
					}
					case CRATE_CHANGE_KEY: {
						if (e.isLeftClick()) {
							EditorManager.startEdit(p, crate, type2);
							EditorManager.tipCustom(p, plugin.lang().Editor_Tip_KeyId.getMsg());
							p.closeInventory();
							EditorManager.sendClickableTips(p, plugin.getKeyManager().getKeyIds());
							return;
						}
						
						if (e.isRightClick()) {
							crate.setKeyId(null);
						}
						break;
					}
					case CRATE_CHANGE_NAME: {
						EditorManager.startEdit(p, crate, type2);
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Name.getMsg());
						p.closeInventory();
						return;
					}
					case CRATE_CHANGE_NPC: {
						if (!Hooks.hasPlugin(Hooks.CITIZENS)) return;
						
						if (e.isLeftClick()) {
							EditorManager.startEdit(p, crate, type2);
							EditorManager.tipCustom(p, plugin.lang().Editor_Tip_NPC.getMsg());
			    			p.closeInventory();
			    			return;
						}
						else if (e.isRightClick()) {
							crate.setAttachedNPCs(new int[] {});
						}
						
						break;
					}
					case CRATE_CHANGE_TEMPLATE: {
						if (click == ClickType.LEFT) {
							EditorManager.startEdit(p, crate, type2);
			       			EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Template.getMsg());
			       			p.closeInventory();
			       			EditorManager.sendClickableTips(p, plugin.getTemplateManager().getTemplateIds());
			       			return;
			   			}
			   			else if (click == ClickType.RIGHT) {
			   				crate.setTemplate(null);
			   			}
						break;
					}
					case CRATE_CHANGE_OPEN_COST: {
						if (e.getClick() == ClickType.MIDDLE) {
							crate.setPermissionRequired(!crate.isPermissionRequired());
							break;
						}
						
						if (e.isLeftClick()) {
							EditorManager.startEdit(p, crate, CrateEditorType.CRATE_CHANGE_OPEN_COST_VAULT);
						}
						else if (e.isRightClick()) {
							EditorManager.startEdit(p, crate, CrateEditorType.CRATE_CHANGE_OPEN_COST_EXP);
						}
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_OpenCost.getMsg());
						p.closeInventory();
						return;
					}
					case CRATE_OPEN_REWARDS: {
						this.openEditorRewards(p);
						return;
					}
					default: {
						return;
					}
				}
				plugin.getCrateManager().save(crate);
				crate.getEditor().open(p, 1);
			}
		};
		
		JYML cfg = GoldenCrates.EDITOR_CRATE;
		
		for (String sId : cfg.getSection("content")) {
			GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(clickHandler);
			}
			this.addButton(guiItem);
		}
		
		for (String sId : cfg.getSection("editor")) {
			GuiItem guiItem = cfg.getGuiItem("editor." + sId, CrateEditorType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(clickHandler);
			}
			this.addButton(guiItem);
		}
	}
	
	public void openEditorRewards(@NotNull Player player) {
		// Save memory
		if (this.editorRewards == null) {
			this.editorRewards = new CrateEditorRewards(this.plugin, this.crate);
		}
		this.editorRewards.open(player, 1);
	}
	
	@Override
	protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
		
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
		return true;
	}

	@Override
	protected void replaceMeta(@NotNull Player p, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		super.replaceMeta(p, item, guiItem);
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null || lore.isEmpty()) return;
		
		String keyId = crate.getKeyId();
		
		lore.replaceAll(line -> line
			.replace("%name%", crate.getName())
			.replace("%cd-format%", TimeUT.formatTime(crate.getOpenCooldown() * 1000L))
			.replace("%cd-raw%", String.valueOf(crate.getOpenCooldown()))
			.replace("%template%", crate.getTemplate())
			.replace("%icon%", ItemUT.getItemName(crate.getItem()))
			.replace("%key-id%", keyId == null ? "-" : keyId)
			.replace("%block-hologram-enabled%", plugin.lang().getBool(crate.isHologramEnabled()))
			.replace("%block-pushback%", plugin.lang().getBool(crate.isBlockPushbackEnabled()))
			.replace("%cost-vault%", NumberUT.format(crate.getOpenCostVault()))
			.replace("%cost-exp%", NumberUT.format(crate.getOpenCostExp()))
			.replace("%cost-permission%", plugin.lang().getBool(crate.isPermissionRequired()))
			.replace("%open-perm%", Perms.OPEN + crate.getId())
			.replace("%block-effect-type%", crate.getBlockEffect().getModel().name())
			.replace("%block-effect-particle%", crate.getBlockEffect().getParticleName())
		);
		
		List<String> lore2 = new ArrayList<>();
		for (String line : new ArrayList<>(lore)) {
			if (line.contains("%npc-id%") && Hooks.hasPlugin(Hooks.CITIZENS)) {
				for (int id : crate.getAttachedNPCs()) {
					NPC npc = CitizensAPI.getNPCRegistry().getById(id);
					if (npc == null) continue;
					
					lore2.add(line
							.replace("%npc-name%", npc.getName())
							.replace("%npc-id%", String.valueOf(id)));
				}
			}
			else if (line.contains("%block-world%")) {
				for (Location bLoc : crate.getBlockLocations()) {
					lore2.add(line
							.replace("%block-x%", NumberUT.format(bLoc.getX()))
							.replace("%block-y%", NumberUT.format(bLoc.getY()))
							.replace("%block-z%", NumberUT.format(bLoc.getZ()))
							.replace("%block-world%", LocUT.getWorldName(bLoc)));
				}
			}
			else if (line.contains("%block-hologram-text%")) {
				for (String cmd : crate.getHologramText()) {
					lore2.add(line.replace("%block-hologram-text%", cmd));
				}
			}
			else {
				lore2.add(line);
			}
		}
		
		meta.setLore(lore2);
		item.setItemMeta(meta);
	}
}
