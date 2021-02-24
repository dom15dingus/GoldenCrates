package su.nightexpress.goldencrates.manager.editor;

import java.io.File;
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

import su.nexmedia.engine.config.api.JYML;
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

	public static JYML CRATE_LIST;
	public static JYML CRATE_MAIN;
	public static JYML CRATE_REWARD_LIST;
	public static JYML CRATE_REWARD_MAIN;
	
	public static JYML KEY_LIST;
	public static JYML KEY_MAIN;
	
	public CrateEditorHandler(@NotNull GoldenCrates plugin, @NotNull NGUI<GoldenCrates> main) {
		super(plugin, CrateEditorType.class, main);
		
    	if (CRATE_LIST == null || !CRATE_LIST.reload()) {
    		CRATE_LIST = new JYML(new File(plugin.getDataFolder() + "/editor/crate_list.yml"));
    	}
    	if (CRATE_MAIN == null || !CRATE_MAIN.reload()) {
    		CRATE_MAIN = new JYML(new File(plugin.getDataFolder() + "/editor/crate_main.yml"));
    	}
    	if (CRATE_REWARD_LIST == null || !CRATE_REWARD_LIST.reload()) {
    		CRATE_REWARD_LIST = new JYML(new File(plugin.getDataFolder() + "/editor/crate_rewards_list.yml"));
    	}
    	if (CRATE_REWARD_MAIN == null || !CRATE_REWARD_MAIN.reload()) {
    		CRATE_REWARD_MAIN = new JYML(new File(plugin.getDataFolder() + "/editor/crate_rewards_reward.yml"));
    	}
    	
    	
    	if (KEY_LIST == null || !KEY_LIST.reload()) {
    		KEY_LIST = new JYML(new File(plugin.getDataFolder() + "/editor/key_list.yml"));
    	}
    	if (KEY_MAIN == null || !KEY_MAIN.reload()) {
    		KEY_MAIN = new JYML(new File(plugin.getDataFolder() + "/editor/key_key.yml"));
    	}
	}

	@Override
	protected boolean onType(
			@NotNull Player player, @Nullable Object obj, 
			@NotNull Enum<?> type2, @NotNull String msg) {
		
    	CrateEditorType type = (CrateEditorType) type2;
    	if (obj instanceof Crate || type == CrateEditorType.CRATE_CREATE_NEW) {
    		return this.onTypeCrate(player, (Crate) obj, type, msg);
    	}
    	else if (obj instanceof CrateKey || type == CrateEditorType.KEY_CREATE_NEW) {
    		return this.onTypeKey(player, (CrateKey) obj, type, msg);
    	}
    	else if (obj instanceof CrateReward) {
    		return this.onTypeReward(player, (CrateReward) obj, type, msg);
    	}
    	return true;
	}

	private boolean onTypeCrate(
			@NotNull Player player, @Nullable Crate crate, 
			@NotNull CrateEditorType type, @NotNull String msg) {
		
    	if (crate == null) {
        	if (type == CrateEditorType.CRATE_CREATE_NEW) {
        		msg = StringUT.colorOff(msg);
        		if (!plugin.getCrateManager().create(msg)) {
        			EditorManager.errorCustom(player, plugin.lang().Editor_Error_CrateExists.getMsg());
        			return false;
        		}
        		CrateEditorHub main = (CrateEditorHub) this.getMainEditor();
        		if (main != null) main.getCratesEditor().open(player, 1);
    			
        	}
        	return true;
    	}
		
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
					EditorManager.errorNumber(player, false);
					return false;
				}
				
				crate.setOpenCooldown(sec);
				break;
			}
			case CRATE_CHANGE_NPC: {
				msg = StringUT.colorOff(msg);
				int s = StringUT.getInteger(msg, -1);
				if (s < 0) {
					EditorManager.errorNumber(player, false);
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
			case CRATE_CHANGE_GUI_TEMPLATE: {
				msg = StringUT.colorOff(msg);
				
				if (!plugin.getTemplateManager().isTemplate(msg)) {
					EditorManager.errorCustom(player, plugin.lang().Editor_Error_Template.getMsg());
					return false;
				}
				
				crate.setTemplate(msg);
				break;
			}
			case CRATE_CHANGE_GUI_PREVIEW: {
				msg = StringUT.colorOff(msg);
				crate.setPreviewId(msg);
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
					EditorManager.errorNumber(player, true);
					return false;
				}
				
				crate.setOpenCostVault(cost);
				break;
			}
			case CRATE_CHANGE_OPEN_COST_EXP: {
				msg = StringUT.colorOff(msg);
				double cost = StringUT.getDouble(msg, -1);
				if (cost < 0) {
					EditorManager.errorNumber(player, true);
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
    		crate.getEditor().openEditorRewards(player);
    	}
    	else {
    		crate.getEditor().open(player, 1);
    	}
    	
    	return true;
	}
	
	private boolean onTypeReward(
			@NotNull Player player, @NotNull CrateReward reward, 
			@NotNull CrateEditorType type, @NotNull String msg) {
    	
		switch (type) {
			case CRATE_CHANGE_REWARD_CHANCE: {
				msg = StringUT.colorOff(msg);
				double d = StringUT.getDouble(msg, -1);
				
				if (d < 0) {
					EditorManager.errorNumber(player, true);
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
    	reward.getEditor().open(player, 1);
    	
    	return true;
	}
	
	private boolean onTypeKey(
			@NotNull Player player, @Nullable CrateKey crateKey, 
			@NotNull CrateEditorType type, @NotNull String msg) {
    	
		if (crateKey == null) {
	    	if (type == CrateEditorType.KEY_CREATE_NEW) {
	    		msg = StringUT.colorOff(msg);
	    		if (!plugin.getKeyManager().create(msg)) {
	    			EditorManager.errorCustom(player, plugin.lang().Editor_Error_Key_Exists.getMsg());
	    			return false;
	    		}
	    		CrateEditorHub main = (CrateEditorHub) this.getMainEditor();
	    		if (main != null) main.getKeysEditor().open(player, 1);
	    	}
	    	return true;
		}
		
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
    	crateKey.getEditor().open(player, 1);
    	
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
