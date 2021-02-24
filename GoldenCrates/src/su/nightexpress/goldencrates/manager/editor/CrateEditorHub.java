package su.nightexpress.goldencrates.manager.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.editor.crate.CrateEditorList;
import su.nightexpress.goldencrates.manager.editor.key.KeyEditorList;

public class CrateEditorHub extends NGUI<GoldenCrates> {

	private CrateEditorList crateEditorList;
	private KeyEditorList keyEditorKeys;
	
	public CrateEditorHub(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg, "");
		
		GuiClick click = (p, type, e) -> {
			if (type == null) return;
			Class<?> clazz = type.getClass();
			
			if (clazz.equals(ContentType.class)) {
				ContentType type2 = (ContentType) type;
				if (type2 == ContentType.EXIT) {
					p.closeInventory();
				}
				return;
			}
			
			if (clazz.equals(CrateEditorType.class)) {
				CrateEditorType type2 = (CrateEditorType) type;
				if (type2 == CrateEditorType.EDITOR_CRATES) {
					this.getCratesEditor().open(p, 1);
					return;
				}
				if (type2 == CrateEditorType.EDITOR_KEYS) {
					this.getKeysEditor().open(p, 1);
					return;
				}
				return;
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
		
		for (String id : cfg.getSection("editor")) {
			GuiItem guiItem = cfg.getGuiItem("editor." + id, CrateEditorType.class);
			if (guiItem == null) continue;
			
			if (guiItem.getType() != null) {
				guiItem.setClick(click);
			}
			this.addButton(guiItem);
		}
	}
	
	@NotNull
	public CrateEditorList getCratesEditor() {
		if (this.crateEditorList == null) {
			this.crateEditorList = new CrateEditorList(this.plugin);
		}
		return this.crateEditorList;
	}
	
	@NotNull
	public KeyEditorList getKeysEditor() {
		if (this.keyEditorKeys == null) {
			this.keyEditorKeys = new KeyEditorList(this.plugin);
		}
		return this.keyEditorKeys;
	}

	@Override
	protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
		
	}

	@Override
	protected boolean ignoreNullClick() {
		return true;
	}

	@Override
	protected boolean cancelClick(int slot) {
		return true;
	}

	@Override
	protected boolean cancelPlayerClick() {
		return true;
	}
}
