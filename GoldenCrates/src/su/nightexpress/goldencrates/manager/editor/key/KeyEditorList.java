package su.nightexpress.goldencrates.manager.editor.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.editor.CrateEditorType;
import su.nightexpress.goldencrates.manager.key.CrateKey;

public class KeyEditorList extends NGUI<GoldenCrates> {
	
	private int[] objSlots;
	private String objName;
	private List<String> objLore;
	
	public KeyEditorList(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "");
		this.objSlots = cfg.getIntArray("object-slots");
		this.objName = StringUT.color(cfg.getString("object-name", "%key%"));
		this.objLore = StringUT.color(cfg.getStringList("object-lore"));
		
		GuiClick click = new GuiClick() {
			@Override
			public void click(Player p, @Nullable Enum<?> type, InventoryClickEvent e) {
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
							plugin.openEditor(p);
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
				else if (clazz.equals(CrateEditorType.class)) {
					CrateEditorType type2 = (CrateEditorType) type;
					switch (type2) {
						case KEY_CREATE_NEW: {
							EditorManager.startEdit(p, null, type2);
			    			EditorManager.tipCustom(p, plugin.lang().Editor_Tip_ID.getMsg());
			    			p.closeInventory();
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
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
    	int len = this.objSlots.length;
    	List<CrateKey> list = new ArrayList<>(plugin.getKeyManager().getKeys())
    			.stream().sorted((c1, c2) -> {
    				return c1.getId().compareTo(c2.getId());
    			}).collect(Collectors.toList());
    	
		List<List<CrateKey>> split = CollectionsUT.split(list, len);
    	
    	int pages = split.size();
    	if (pages < 1) list = Collections.emptyList();
    	else list = split.get(page - 1);
    	
        int count = 0;
        for (CrateKey crateKey : list) {
        	List<String> lore = new ArrayList<>(this.objLore);
        	lore.replaceAll(line -> line
        		.replace("%key-name%", crateKey.getName())
        		.replace("%key-virtual%", plugin.lang().getBool(crateKey.isVirtual()))
        	);
        	
        	ItemStack item = crateKey.getItem();
        	item.setAmount(count + 1);
        	
        	JIcon icon = new JIcon(item);
        	icon.setName(this.objName.replace("%key%", crateKey.getId()));
        	icon.setLore(lore);
        	icon.setClick(new GuiClick() {
				@Override
				public void click(@NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {
					if (e.isRightClick() && e.isShiftClick()) {
						plugin.getKeyManager().delete(crateKey);
						open(p, 1);
						return;
					}
					crateKey.getEditor().open(p, 1);
				}
        	});
        	
        	this.addButton(player, icon, this.objSlots[count++]);
        }
        
        this.setUserPage(player, page, pages);
	}
}
