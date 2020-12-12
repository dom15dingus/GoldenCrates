package su.nightexpress.goldencrates.manager.crate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.Editable;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.manager.editor.crate.CrateEditorCrate;

public class Crate extends LoadableItem implements Cleanable, Editable {

	private String name;
	private String template;
	private boolean isPermissionRequired;
	private int openCooldown;
	private double openCostEco;
	private int openCostExp;
	private int[] npcIds;
	private String keyId;
	
	private ItemStack item;
	
	private Set<Location> blockLocations;
	private boolean blockPushbackEnabled;
	private boolean blockHoloEnabled;
	private List<String> blockHoloText;
	private CrateEffect blockEffect;
	
	private boolean rewardBroadcast;
	private int rewardMinAmount;
	private int rewardMaxAmount;
	private LinkedHashMap<String, CrateReward> rewardMap;
	
	private CratePreview preview;
	private CrateEditorCrate editor;
	
	public Crate(@NotNull GoldenCrates plugin, @NotNull String path) {
		super(plugin, path);
		
		this.setName("&b" + StringUT.capitalizeFully(id + " Crate"));
		this.setPermissionRequired(false);
		this.setTemplate(null); // None
		this.setOpenCooldown(15);
		this.setOpenCostVault(0);
		this.setOpenCostExp(0);
		this.setAttachedNPCs(new int[] {});
		this.setKeyId(null);
		
		this.setItem(null); // Generate new icon
		
		this.setBlockLocations(new HashSet<>());
		this.setBlockPushbackEnabled(true);
		this.setHologramEnabled(false);
		this.setHologramText(Arrays.asList("&c&lMYSTERY CRATE", "&7Buy a key at &cwww.myserver.com"));
		this.setBlockEffect(new CrateEffect(CrateEffectModel.HELIX, Particle.FLAME.name()));
		
		this.setRewardBroadcast(true);
		this.setMinRewards(1);
		this.setMaxRewards(3);
		this.setRewards(new LinkedHashMap<>());
	}
	
	public Crate(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg);

		this.setName(cfg.getString("name", this.getId()));
		this.setPermissionRequired(cfg.getBoolean("permission-required"));
		this.setTemplate(cfg.getString("template", "none"));
		this.setOpenCooldown(cfg.getInt("cooldown"));
		this.setAttachedNPCs(cfg.getIntArray("attached-citizens"));
		this.setKeyId(cfg.getString("key.id"));
		
		// Setup Open Cost
		this.setOpenCostVault(cfg.getDouble("open-cost.vault"));
		this.setOpenCostExp(cfg.getInt("open-cost.exp"));
		
		// ITEM SETTINGS
		this.setItem(cfg.getItem("item."));
		
		// BLOCK SETTINGS
		this.setBlockLocations(new HashSet<>(LocUT.deserialize(cfg.getStringList("block.locations"))));
		this.setBlockPushbackEnabled(cfg.getBoolean("block.pushback.enabled"));
		this.setHologramEnabled(cfg.getBoolean("block.hologram.enabled"));
		this.setHologramText(cfg.getStringList("block.hologram.text"));
		
		CrateEffectModel model = CollectionsUT.getEnum(cfg.getString("block.effects.type", "SIMPLE"), CrateEffectModel.class);
		if (model == null) model = CrateEffectModel.HELIX;
			
		String particle = cfg.getString("block.effects.particle", Particle.FLAME.name());
		CrateEffect crateEffect = new CrateEffect(model, particle);
		this.setBlockEffect(crateEffect);
		
		this.setRewardBroadcast(cfg.getBoolean("rewards.broadcast"));
		this.setMinRewards(cfg.getInt("rewards.min", 1));
		this.setMaxRewards(cfg.getInt("rewards.max", 1));
		this.rewardMap = new LinkedHashMap<>();
		
		for (String rewId : cfg.getSection("rewards.list")) {
			String path = "rewards.list." + rewId + ".";
			
			// Update reward's ID.
			try {
				UUID.fromString(rewId);
			}
			catch (IllegalArgumentException ex) {
				rewId = UUID.randomUUID().toString();
			}
			
			double rewChance = cfg.getDouble(path + "chance");
			String rewName = cfg.getString(path + "name");
			List<String> rewCmds = cfg.getStringList(path + "cmds");
			ItemStack rewItem = cfg.getItem64(path + "item");
			ItemStack rewPreview = cfg.getItem64(path + "preview");
			
			CrateReward reward = new CrateReward(this, rewId, rewChance, rewName, rewItem, rewCmds, rewPreview);
			this.rewardMap.put(rewId, reward);
		}
	}
	
	@Override
	protected void save(@NotNull JYML cfg) {
    	cfg.set("name", this.getName());
    	cfg.set("permission-required", this.isPermissionRequired());
    	cfg.set("template", this.getTemplate());
    	cfg.set("cooldown", this.getOpenCooldown());
    	cfg.setIntArray("attached-citizens", this.getAttachedNPCs());
    	cfg.setItem("item", this.getItem());
    	cfg.set("key.id", this.getKeyId());
    	
    	cfg.set("open-cost.vault", this.getOpenCostVault());
    	cfg.set("open-cost.exp", this.getOpenCostExp());
    	
    	cfg.set("block.locations", LocUT.serialize(new ArrayList<>(this.getBlockLocations())));
    	cfg.set("block.pushback.enabled", this.blockPushbackEnabled);
    	cfg.set("block.hologram.enabled", this.blockHoloEnabled);
    	cfg.set("block.hologram.text", this.blockHoloText);
    	cfg.set("block.effects.type", this.blockEffect.getModel().name());
    	cfg.set("block.effects.particle", this.blockEffect.getParticleName());
    	
    	cfg.set("rewards.broadcast", this.isRewardBroadcast());
    	cfg.set("rewards.min", this.getMinRewards());
    	cfg.set("rewards.max", this.getMaxRewards());
    	cfg.set("rewards.list", null);
    	
    	for (Entry<String, CrateReward> e : this.getRewardsMap().entrySet()) {
    		CrateReward reward = e.getValue();
    		String path = "rewards.list." + e.getKey() + ".";
    		
    		cfg.set(path + "name", reward.getName());
    		cfg.set(path + "chance", reward.getChance());
    		cfg.set(path + "cmds", reward.getCommands());
    		cfg.setItem64(path + "item", reward.getItem());
    		cfg.setItem64(path + "preview", reward.getPreview());
    	}
	}
	
	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.shutdown();
			this.editor = null;
		}
		if (this.preview != null) {
			this.preview.shutdown();
			this.preview = null;
		}
		if (this.rewardMap != null) {
			this.rewardMap.values().forEach(reward -> reward.clear());
			this.rewardMap.clear();
			this.rewardMap = null;
		}
	}

	@Override
	@NotNull
	public CrateEditorCrate getEditor() {
		if (this.editor == null) {
			this.editor = new CrateEditorCrate((GoldenCrates) plugin, this);
		}
		return this.editor;
	}

	@NotNull
	public String getName() {
		return this.name;
	}
	
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}
	
	public boolean isPermissionRequired() {
		return isPermissionRequired;
	}
	
	public void setPermissionRequired(boolean isPermissionRequired) {
		this.isPermissionRequired = isPermissionRequired;
	}
	
	public boolean hasPermission(@NotNull Player player) {
		return !this.isPermissionRequired() || (player.hasPermission(Perms.OPEN + this.getId())
				|| player.hasPermission(Perms.OPEN + JStrings.MASK_ANY));
	}
	
	@NotNull
	public String getTemplate() {
		return this.template;
	}
	
	public void setTemplate(@Nullable String template) {
		if (template == null) {
			this.template = JStrings.NONE;
		}
		else {
			this.template = template.toLowerCase();
		}
	}
	
	public boolean hasTemplate() {
		return !this.getTemplate().equalsIgnoreCase(JStrings.NONE);
	}
	
	public double getOpenCostVault() {
		return this.openCostEco;
	}
	
	public void setOpenCostVault(double cost) {
		this.openCostEco = cost;
	}
	
	public int getOpenCostExp() {
		return openCostExp;
	}
	
	public void setOpenCostExp(int openCostExp) {
		this.openCostExp = openCostExp;
	}
	
	public int getOpenCooldown() {
		return this.openCooldown;
	}
	
	public void setOpenCooldown(int openCooldown) {
		this.openCooldown = openCooldown;
	}
	
	public int[] getAttachedNPCs() {
		return this.npcIds;
	}
	
	public void setAttachedNPCs(int[] npcIds) {
		this.npcIds = npcIds;
	}
	
	public boolean isAttachedNPC(int id) {
		return ArrayUtils.contains(this.getAttachedNPCs(), id);
	}
	
	@Nullable
	public String getKeyId() {
		return keyId;
	}
	
	public void setKeyId(@Nullable String keyId) {
		this.keyId = keyId == null || keyId.isEmpty() ? null : keyId;
	}
	
	@NotNull
	public ItemStack getItem() {
		return new ItemStack(this.item);
	}
	
	public void setItem(@Nullable ItemStack item) {
		if (item == null) {
			JIcon icon= new JIcon(Material.ENDER_CHEST);
			icon.setName(this.getName());
			icon.addLore("&aRight-Click &bto open crate!");
			item = icon.build();
		}
		this.item = new ItemStack(item);
		DataUT.setData(this.item, CrateManager.TAG_CRATE, this.getId());
	}
	
	@NotNull
	public Set<Location> getBlockLocations() {
		return blockLocations;
	}
	
	public void setBlockLocations(@NotNull Set<Location> blockLocations) {
		blockLocations.removeIf(loc -> loc.getBlock().isEmpty());
		this.blockLocations = blockLocations;
	}
	
	@Nullable
	public Location getBlockHologramLocation(@NotNull Location loc) {
		double offset = 1 + (0.25 * this.getHologramText().size());
		return LocUT.getCenter(loc.clone()).add(0D, offset, 0D);
	}
	
	public boolean isBlockPushbackEnabled() {
		return this.blockPushbackEnabled;
	}
	
	public void setBlockPushbackEnabled(boolean blockPushback) {
		this.blockPushbackEnabled = blockPushback;
	}
	
	public boolean isHologramEnabled() {
		return this.blockHoloEnabled;
	}
	
	public void setHologramEnabled(boolean blockHologram) {
		this.blockHoloEnabled = blockHologram;
	}
	
	@NotNull
	public List<String> getHologramText() {
		return new ArrayList<>(this.blockHoloText);
	}
	
	public void setHologramText(@NotNull List<String> holoText) {
		this.blockHoloText = StringUT.color(holoText);
	}
	
	@NotNull
	public CrateEffect getBlockEffect() {
		return this.blockEffect;
	}
	
	public void setBlockEffect(@NotNull CrateEffect blockEffect) {
		this.blockEffect = blockEffect;
	}
	
	public boolean isRewardBroadcast() {
		return this.rewardBroadcast;
	}
	
	public void setRewardBroadcast(boolean broadcast) {
		this.rewardBroadcast = broadcast;
	}
	
	public int getMinRewards() {
		return this.rewardMinAmount;
	}
	
	public void setMinRewards(int min) {
		this.rewardMinAmount = Math.max(1, min);
	}
	
	public int getMaxRewards() {
		return this.rewardMaxAmount;
	}
	
	public void setMaxRewards(int max) {
		this.rewardMaxAmount = Math.max(1, max);
	}
	
	public int rollRewardsAmount() {
		if (this.getMinRewards() > this.getMaxRewards()) {
			return this.getMinRewards();
		}
		return Rnd.get(this.getMinRewards(), this.getMaxRewards());
	}
	
	@NotNull
	public CrateReward createReward() {
		CrateReward reward = new CrateReward(this);
		this.getRewardsMap().put(reward.getId(), reward);
		return reward;
	}
	
	@Nullable
	public CrateReward getReward(@NotNull String id) {
		return this.rewardMap.get(id.toLowerCase());
	}
	
	@NotNull
	public Collection<CrateReward> getRewards() {
		return this.rewardMap.values();
	}
	
	@NotNull
	public LinkedHashMap<String, CrateReward> getRewardsMap() {
		return this.rewardMap;
	}
	
	public void setRewards(@NotNull List<CrateReward> rewards) {
		this.setRewards(rewards.stream().collect(
				Collectors.toMap(CrateReward::getId, Function.identity(), (has, add) -> add, LinkedHashMap::new)));
	}
	
	public void setRewards(@NotNull LinkedHashMap<String, CrateReward> rewards) {
		this.rewardMap = rewards;
	}
	
	public void deleteReward(@NotNull String id) {
		this.rewardMap.remove(id.toLowerCase());
	}
	
	public void openPreview(@NotNull Player player) {
		if (!Config.REWARD_PREVIEW_ENABLED) {
			return;
		}
		if (this.preview == null) {
			this.preview = new CratePreview((GoldenCrates) plugin, this);
		}
		this.preview.open(player, 1);
	}
	
	@NotNull
	public CrateReward rollReward() {
		Map<CrateReward, Double> map = new HashMap<>();
		for (CrateReward reward : this.getRewards()) {
			map.put(reward, reward.getChance());
		}
		CrateReward crate = Rnd.get(map);
		if (crate == null) {
			throw new IllegalStateException("Unable to roll crate reward for: " + this.getId());
		}
		return crate;
	}
}
