package su.nightexpress.goldencrates.manager.editor.key;

import java.util.List;

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
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHandler;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHub;
import su.nightexpress.goldencrates.manager.editor.CrateEditorType;
import su.nightexpress.goldencrates.manager.key.CrateKey;

public class KeyEditorKey extends NGUI<GoldenCrates> {

	private CrateKey crateKey;
	
	public KeyEditorKey(@NotNull GoldenCrates plugin, @NotNull CrateKey crateKey) {
		super(plugin, CrateEditorHandler.KEY_MAIN, "");
		this.crateKey = crateKey;
		
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
			    		if (main != null) main.getKeysEditor().open(p, 1);
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
					case KEY_CHANGE_ITEM: {
						if (e.getClick() == ClickType.MIDDLE) {
							ItemUT.addItem(p, crateKey.getItem());
							return;
						}
						
						ItemStack cursor = e.getCursor();
						if (cursor == null || ItemUT.isAir(cursor)) return;
						
						crateKey.setItem(cursor);
						e.getView().setCursor(null);
						
						break;
					}
					case KEY_CHANGE_VIRTUAL: {
						crateKey.setVirtual(!crateKey.isVirtual());
						break;
					}
					case KEY_CHANGE_NAME: {
						EditorManager.startEdit(p, crateKey, type2);
						EditorManager.tipCustom(p, plugin.lang().Editor_Tip_Name.getMsg());
						p.closeInventory();
						return;
					}
					default: {
						return;
					}
				}
				plugin.getKeyManager().save(crateKey);
				crateKey.getEditor().open(p, 1);
			}
		};
		
		JYML cfg = CrateEditorHandler.KEY_MAIN;
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
	
	@Override
	protected void replaceFrame(@NotNull Player player, @NotNull GuiItem guiItem) {
		super.replaceFrame(player, guiItem);
		
		Enum<?> type = guiItem.getType();
		if (type == null) return;
		
		if (type == CrateEditorType.KEY_CHANGE_VIRTUAL) {
			guiItem.setAnimationStartFrame(this.crateKey.isVirtual() ? 1 : 0);
		}
	}

	@Override
	protected void onCreate(@NotNull Player player, Inventory inv, int page) {
		
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
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		List<String> lore = meta.getLore();
		if (lore == null || lore.isEmpty()) return;
		
		lore.replaceAll(line -> line
			.replace("%name%", crateKey.getName())
			.replace("%key-item%", ItemUT.getItemName(crateKey.getItem()))
			.replace("%id%", crateKey.getId())
			.replace("%virtual%", plugin.lang().getBool(crateKey.isVirtual()))
		);
		
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
