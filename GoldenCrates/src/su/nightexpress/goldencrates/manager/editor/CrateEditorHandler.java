package su.nightexpress.goldencrates.manager.editor;

import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateEffect;
import su.nightexpress.goldencrates.manager.crate.CrateReward;
import su.nightexpress.goldencrates.manager.key.CrateKey;

public class CrateEditorHandler extends EditorHandler<GoldenCrates> {

	public CrateEditorHandler(@NotNull GoldenCrates plugin, @NotNull NGUI<GoldenCrates> main) {
		super(plugin, CrateEditorType.class, main);
	}

	@Override
	protected boolean onType(
			@NotNull Player p, 
			@Nullable Object obj, 
			@NotNull Enum<?> type2, 
			@NotNull String msg) {
		
    	CrateEditorType type = (CrateEditorType) type2;
    	Crate crate = null;
    	CrateKey crateKey = null;
    	CrateReward reward = null;
    	
    	if (obj instanceof Crate) {
    		crate = (Crate) obj;
    	}
    	else if (obj instanceof CrateKey) {
    		crateKey = (CrateKey) obj;
    	}
    	else if (obj instanceof CrateReward) {
    		reward = (CrateReward) obj;
    	}
    	
    	if (type == CrateEditorType.CRATE_CREATE_NEW) {
    		msg = StringUT.colorOff(msg);
    		if (!plugin.getCrateManager().create(msg)) {
    			EditorManager.errorCustom(p, plugin.lang().Editor_Error_CrateExists.getMsg());
    			return false;
    		}
    		CrateEditorHub main = (CrateEditorHub) this.getMainEditor();
    		if (main != null) main.getCratesEditor().open(p, 1);
			return true;
    	}
    	if (type == CrateEditorType.KEY_CREATE_NEW) {
    		msg = StringUT.colorOff(msg);
    		if (!plugin.getKeyManager().create(msg)) {
    			EditorManager.errorCustom(p, plugin.lang().Editor_Error_Key_Exists.getMsg());
    			return false;
    		}
    		CrateEditorHub main = (CrateEditorHub) this.getMainEditor();
    		if (main != null) main.getKeysEditor().open(p, 1);
			return true;
    	}
    	
    	
    	if (crate != null) {
    		return this.onTypeCrate(p, crate, type, msg);
    	}
    	if (crateKey != null) {
    		return this.onTypeKey(p, crateKey, type, msg);
    	}
    	if (reward != null) {
    		return this.onTypeReward(p, reward, type, msg);
    	}
    	
    	return true;
	}

	private boolean onTypeCrate(@NotNull Player p, @NotNull Crate crate, @NotNull CrateEditorType type, @NotNull String msg) {
    	switch (type) {
			case CRATE_CHANGE_BLOCK_HOLOGRAM: {
				List<String> list = crate.getHologramText();
				list.add(msg);
				crate.setHologramText(list);
				break;
			}
			case CRATE_CHANGE_COOLDOWN: {
				msg = StringUT.colorOff(msg);
				int sec = StringUT.getInteger(msg, -1);
				if (sec < 0) {
					EditorManager.errorNumber(p, false);
					return false;
				}
				
				crate.setOpenCooldown(sec);
				break;
			}
			case CRATE_CHANGE_NPC: {
				msg = StringUT.colorOff(msg);
				int s = StringUT.getInteger(msg, -1);
				if (s < 0) {
					EditorManager.errorNumber(p, false);
					return false;
				}
				int[] cur = crate.getAttachedNPCs();
				if (ArrayUtils.contains(cur, s)) break;
				
				int[] npc = new int[cur.length + 1];
				int j = 0;
				for (int id : cur) {
					npc[j++] = id;
				}
				npc[j] = s;
				
				crate.setAttachedNPCs(npc);
				break;
			}
			case CRATE_CHANGE_TEMPLATE: {
				msg = StringUT.colorOff(msg);
				
				if (!plugin.getTemplateManager().isTemplate(msg)) {
					EditorManager.errorCustom(p, plugin.lang().Editor_Error_Template.getMsg());
					return false;
				}
				
				crate.setTemplate(msg);
				break;
			}
			case CRATE_CHANGE_NAME: {
				crate.setName(msg);
				break;
			}
			case CRATE_CHANGE_KEY: {
				crate.setKeyId(msg);
				break;
			}
			case CRATE_CHANGE_OPEN_COST_VAULT: {
				msg = StringUT.colorOff(msg);
				double cost = StringUT.getDouble(msg, -1);
				if (cost < 0) {
					EditorManager.errorNumber(p, true);
					return false;
				}
				
				crate.setOpenCostVault(cost);
				break;
			}
			case CRATE_CHANGE_OPEN_COST_EXP: {
				msg = StringUT.colorOff(msg);
				double cost = StringUT.getDouble(msg, -1);
				if (cost < 0) {
					EditorManager.errorNumber(p, true);
					return false;
				}
				
				crate.setOpenCostExp((int) cost);
				break;
			}
			case CRATE_CHANGE_BLOCK_EFFECTS_PARTICLE: {
				msg = StringUT.colorOff(msg);
				
				CrateEffect effect = crate.getBlockEffect();
				effect.setParticleName(msg);
				break;
			}
			default: {
				break;
			}
		}
    	
    	plugin.getCrateManager().save(crate);
    	if (type == CrateEditorType.CRATE_CHANGE_REWARD_BROADCAST) {
    		crate.getEditor().openEditorRewards(p);
    	}
    	else {
    		crate.getEditor().open(p, 1);
    	}
    	
    	return true;
	}
	
	private boolean onTypeReward(@NotNull Player p, @NotNull CrateReward reward, @NotNull CrateEditorType type, @NotNull String msg) {
    	switch (type) {
			case CRATE_CHANGE_REWARD_CHANCE: {
				msg = StringUT.colorOff(msg);
				double d = StringUT.getDouble(msg, -1);
				
				if (d < 0) {
					EditorManager.errorNumber(p, true);
					return false;
				}
				
				reward.setChance(d);
				break;
			}
			case CRATE_CHANGE_REWARD_COMMANDS: {
				reward.getCommands().add(msg);
				break;
			}
			case CRATE_CHANGE_REWARD_NAME: {
				reward.setName(msg);
				break;
			}
			default: {
				break;
			}
		}
    	
    	plugin.getCrateManager().save(reward.getCrate());
    	reward.getEditor().open(p, 1);
    	
    	return true;
	}
	
	private boolean onTypeKey(@NotNull Player p, @NotNull CrateKey crateKey, @NotNull CrateEditorType type, @NotNull String msg) {
    	switch (type) {
			case KEY_CHANGE_NAME: {
				crateKey.setName(msg);
				break;
			}
			default: {
				break;
			}
		}
    	
    	plugin.getKeyManager().save(crateKey);
    	crateKey.getEditor().open(p, 1);
    	
    	return true;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCrateBlockClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Map.Entry<Enum<?>, Object> editor = EditorManager.getEditor(p);
		if (editor == null) return;
		
		Block block = e.getClickedBlock();
		if (block == null) return;
		
		if (editor.getKey() == CrateEditorType.CRATE_CHANGE_BLOCK_LOCATION) {
			if (plugin.getCrateManager().getCrateByBlock(block) != null) return;
			
			Crate crate = (Crate) editor.getValue();
			crate.getBlockLocations().add(block.getLocation());
			e.setUseInteractedBlock(Result.DENY);
			e.setUseItemInHand(Result.DENY);
			
			plugin.getCrateManager().save(crate);
			this.endEdit(p);
			crate.getEditor().open(p, 1);
		}
	}
}
