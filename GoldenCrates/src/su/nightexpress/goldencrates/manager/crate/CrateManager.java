package su.nightexpress.goldencrates.manager.crate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.api.event.CrateOpenEvent;
import su.nightexpress.goldencrates.api.event.CratePreOpenEvent;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.hooks.external.HologramsHook;
import su.nightexpress.goldencrates.manager.key.CrateKey;
import su.nightexpress.goldencrates.manager.template.CrateTemplate;
import su.nightexpress.goldencrates.manager.template.TemplateManager;

public class CrateManager extends IManager<GoldenCrates> {

	public static final NamespacedKey TAG_CRATE = new NamespacedKey(GoldenCrates.getInstance(), "GCRATES_CRATE");
	private static final String META_VISUAL_REWARD = "GCRATES_VISUAL_REWARD";
	
	private Map<String, Crate> crates;
	
	private HologramsHook holoHook;
	
	public CrateManager(@NotNull GoldenCrates plugin) {
		super(plugin);
	}
	
	@Override
	public void setup() {
		this.crates = new HashMap<>();
		this.holoHook = plugin.getHook(HologramsHook.class);
		this.plugin.getConfigManager().extract(Config.DIR_CRATES);
		this.plugin.getConfigManager().extract(Config.DIR_PREVIEWS);
		
		for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + Config.DIR_CRATES, true)) {
			try {
				Crate crate = new Crate(plugin, cfg);
				this.crates.put(crate.getId(), crate);
				this.updateHologram(crate);
				CrateEffectModel.addCrate(crate); // Add crate block effects.
			}
			catch (Exception ex) {
				plugin.error("Could not load crate: " + cfg.getFile().getName());
				ex.printStackTrace();
			}
		}
		this.plugin.info("Crates Loaded: " + crates.size());
		this.registerListeners();
		
		CrateEffectModel.start();
	}
	
	@Override
	public void shutdown() {
		CrateEffectModel.shutdown();
		
		if (this.crates != null) {
			this.crates.values().forEach(crate -> crate.clear());
			this.crates.clear();
			this.crates = null;
		}
		
		this.unregisterListeners();
	}
	
	public boolean create(@NotNull String id) {
    	if (this.getCrateById(id) != null) {
    		return false;
    	}
    	
		this.save(new Crate(plugin, plugin.getDataFolder() + "/crates/" + id + ".yml"));
		return true;
	}
	
	public void save(@NotNull Crate crate) {
		crate.save();
    	this.updateHologram(crate);
    	this.crates.put(crate.getId(), crate);
    	
    	CrateEffectModel.removeCrate(crate);
    	CrateEffectModel.addCrate(crate); // Add crate block effects.
    	CrateEffectModel.start();
	}
	
	public void delete(@NotNull Crate crate) {
    	File file = crate.getFile();
    	file.delete();
    	
    	crate.clear(); // Unregister GUI editors
    	CrateEffectModel.removeCrate(crate);
    	
		this.removeBlockHologram(crate);
    	this.crates.remove(crate.getId());
	}

	private void removeBlockHologram(@NotNull Crate crate) {
		if (this.holoHook != null) {
			this.holoHook.remove(crate);
		}
	}
	
	private void addBlockHologram(@NotNull Crate crate) {
		if (this.holoHook != null && crate.isHologramEnabled()) {
			this.holoHook.create(crate);
		}
	}
	
	public void updateHologram(@NotNull Crate crate) {
		this.removeBlockHologram(crate);
		this.addBlockHologram(crate);
	}
	
	public boolean isCrate(@NotNull ItemStack item) {
		return this.getCrateByItem(item) != null;
	}
	
	@NotNull
	public List<String> getCrateIds(boolean keyOnly) {
		List<String> list = new ArrayList<>();
		for (Crate crate : this.getCrates()) {
			if (plugin.getKeyManager().getKeyByCrate(crate) == null && keyOnly) continue;
			list.add(crate.getId());
		}
		return list;
	}

	@NotNull
	public Collection<Crate> getCrates() {
		return crates.values();
	}

	@Nullable
	public Crate getCrateById(@NotNull String id) {
		return crates.get(id.toLowerCase());
	}

	@Nullable
	public Crate getCrateByNPC(int id) {
		Optional<Crate> opt = this.getCrates().stream()
				.filter(crate -> crate.isAttachedNPC(id)).findFirst();
		
		return opt.isPresent() ? opt.get() : null;
	}
	
	@Nullable
	public Crate getCrateByItem(@NotNull ItemStack item) {
		String id = DataUT.getStringData(item, TAG_CRATE);
		return id != null ? this.getCrateById(id) : null;
	}
	
	@Nullable
	public Crate getCrateByBlock(@NotNull Block block) {
		return this.getCrateByLocation(block.getLocation());
	}
	
	@Nullable
	public Crate getCrateByLocation(@NotNull Location loc) {
		Optional<Crate> opt = this.getCrates().stream()
				.filter(crate -> crate.getBlockLocations().contains(loc)).findFirst();
		
		return opt.isPresent() ? opt.get() : null;
	}
	
	public void giveCrate(@NotNull Player player, @NotNull Crate crate, int amount) {
		if (amount < 1) return;
		
		ItemStack crateItem = crate.getItem();
		crateItem.setAmount(Math.min(64, amount));
		ItemUT.addItem(player, crateItem);
		
		plugin.lang().Command_Give_Notify
			.replace("%amount%", amount).replace("%crate%", crate.getName())
			.send(player);
	}
	
	public boolean openCrate(@NotNull Player player, @NotNull Crate crate, @Nullable ItemStack item, @Nullable Block block) {
		if (TemplateManager.isSpinning(player)) {
			plugin.lang().Crate_Open_Error_AlreadyIn.send(player);
			return false;
		}
		
		if (!crate.hasPermission(player)) {
			plugin.lang().Error_NoPerm.send(player);
			return false;
		}
		
		int rewAmount = crate.rollRewardsAmount();
		if (crate.getRewards().isEmpty() || (!crate.hasTemplate() && rewAmount < 1)) {
			plugin.lang().Crate_Open_Error_NoRewards.send(player);
			return false;
		}
		
		CrateUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user != null && user.isCrateOnCooldown(crate)) {
			long end = user.getCrateCooldown(crate);
			plugin.lang().Crate_Open_Error_Cooldown
				.replace("%time%", TimeUT.formatTimeLeft(end))
				.send(player);
			return false;
		}
		
		CrateTemplate template = this.plugin.getTemplateManager().getTemplateById(crate.getTemplate());
		int emptySlots = 0;
		int maxSlots = template != null ? template.getSpinners().size() : crate.getMaxRewards();
		for (ItemStack item2 : player.getInventory().getStorageContents()) {
			if (ItemUT.isAir(item2)) emptySlots++;
		}
		if (emptySlots < maxSlots) {
			plugin.lang().Crate_Open_Error_NoSlots.replace("%slots%", maxSlots).send(player);
			return false;
		}
		
		CrateKey crateKey = this.plugin.getKeyManager().getKeyByCrate(crate);
		if (crateKey != null) {
			if (!this.plugin.getKeyManager().hasKey(player, crateKey)) {
				if (block != null && crate.isBlockPushbackEnabled()) {
					player.setVelocity(player.getEyeLocation().getDirection().setY(-0.4D).multiply(-1.25));
				}
				plugin.lang().Crate_Open_Error_NoKey.send(player);
				return false;
			}
		}
		
		VaultHK vaultHook = plugin.getVault();
		double openCostEco = player.hasPermission(Perms.BYPASS_OPEN_COST) ? 0 : crate.getOpenCostVault();
		if (openCostEco > 0 && vaultHook != null) {
			double balance = vaultHook.getBalance(player);
			if (balance < openCostEco) {
				plugin.lang().Crate_Open_Error_NoMoney.send(player);
				return false;
			}
		}
		
		double openCostExp = player.hasPermission(Perms.BYPASS_OPEN_COST) ? 0 : crate.getOpenCostExp();
		if (openCostExp > 0) {
			double balance = PlayerUT.getTotalExperience(player);
			if (balance < openCostExp) {
				plugin.lang().Crate_Open_Error_NoExp.send(player);
				return false;
			}
		}
		
		CratePreOpenEvent preOpenEvent = new CratePreOpenEvent(crate, player);
		plugin.getPluginManager().callEvent(preOpenEvent);
		if (preOpenEvent.isCancelled()) return false;
		
		// Take costs
		if (vaultHook != null && openCostEco > 0) vaultHook.take(player, openCostEco);
		if (openCostExp > 0) PlayerUT.setExp(player, (long) -openCostExp);
		
		// Open via GUI if crate has Template
		if (template != null) {
	    	template.startSpin(player, crate, item);
		}
		// Instant open in other case
		else {
			CrateOpenEvent openEvent = new CrateOpenEvent(crate, player);
			
			StringBuilder rewNameBuild = new StringBuilder("");
			Set<Item> visual = new HashSet<>(); // List for visual drop
			for (int rewCount = 0; rewCount < rewAmount; rewCount++) {
				CrateReward reward = crate.rollReward();
				
				// Visual reward drop, but unpickable
				if (!Config.CRATE_BLOCK_NO_VISUAL_DROP && block != null) {
					Location center = LocUT.getCenter(block.getLocation().add(0, 1.1, 0), false);
					ItemStack visualItem = reward.getPreview();
					DataUT.setData(visualItem, TAG_CRATE, (double)Rnd.get()); // Make it unstackable
					Item drop = block.getWorld().dropItem(center, visualItem);
					drop.setInvulnerable(true);
					drop.setCustomNameVisible(true);
					drop.setCustomName(reward.getName());
					drop.setMetadata(META_VISUAL_REWARD, new FixedMetadataValue(plugin, Rnd.get()));
					
					visual.add(drop);
				}
				
				// Add reward to event
				openEvent.getRewards().add(reward);
			}
			
			// Call CrateOpenEvent
			plugin.getPluginManager().callEvent(openEvent);
			
			// Clean visual crate block drops.
			if (!visual.isEmpty() && block != null) { 
				// Playing chest open animation
				// and auto-close after 3 sec.
				plugin.getNMS().openChestAnimation(block, true);
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
					plugin.getNMS().openChestAnimation(block, false); // Close
					for (Item drop : visual) { // Remove all visual drop
						if (drop != null && drop.isValid()) {
							drop.remove();
							EffectUT.playEffect(drop.getLocation(), Particle.FIREWORKS_SPARK.name(), 0.05f, 0.05f, 0.05f, 0.1f, 15);
						}
					}
				}, 60L);
			}
			
			// Give crate rewards and broadcast message.
			openEvent.getRewards().forEach(reward -> {
				reward.give(player);
				if (rewNameBuild.length() > 0) {
					rewNameBuild.append(", ");
				}
				rewNameBuild.append(reward.getName());
			});
			this.broadcastReward(player, crate, rewNameBuild.toString());
		}
		
		if (crateKey != null) {
			this.plugin.getKeyManager().takeKey(player, crateKey);
		}
		if (item != null) {
			item.setAmount(item.getAmount()  -1);
		}
		
		this.setCooldown(player, crate);
		return true;
	}
	
	public void broadcastReward(@NotNull Player player, @NotNull Crate crate, @NotNull String rewName) {
		plugin.lang().Crate_Open_Reward_Info.replace("%reward%", rewName).send(player);
		
		if (crate.isRewardBroadcast()) {
			plugin.lang().Crate_Open_Reward_Broadcast
				.replace("%crate%", crate.getName())
				.replace("%reward%", rewName)
				.replace("%player%", player.getName())
				.broadcast();
		}
	}
	
	public void setCooldown(@NotNull Player player, @NotNull Crate crate) {
		if (player.hasPermission(Perms.BYPASS_COOLDOWN) || crate.getOpenCooldown() <= 0) return;
		
		CrateUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return;
		
		user.setCrateCooldown(crate);
		
		if (plugin.cfg().dataSaveInstant) {
			plugin.getUserManager().save(user);
		}
	}
    
    @EventHandler(priority = EventPriority.LOW)
    public void onCrateUse(PlayerInteractEvent e) {
    	Player player = e.getPlayer();
    	Crate crate = null;
    	
    	ItemStack item = e.getItem();
    	Block block = null;
    	
    	if (item != null) {
    		if (e.useItemInHand() == Result.DENY) return;
	    	crate = this.getCrateByItem(item);
    	}
    	if (crate == null) {
    		item = null;
    		block = e.getClickedBlock();
        	if (block == null || e.useInteractedBlock() == Result.DENY) return;
        	
        	crate = this.getCrateByBlock(block);
    	}
    	if (crate == null) {
    		return;
    	}
    	
    	e.setUseItemInHand(Result.DENY);
    	e.setUseInteractedBlock(Result.DENY);
    	e.setCancelled(true);
    	
    	if (e.getHand() != EquipmentSlot.HAND) return;
    	
    	Action action = e.getAction();
    	if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
    		crate.openPreview(player);
    	}
    	else {
	    	this.openCrate(player, crate, item, block);
    	}
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCratePlace(BlockPlaceEvent e) {
    	ItemStack item = e.getItemInHand();
    	if (this.isCrate(item)) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVisualPickupEntity(EntityPickupItemEvent e) {
    	Item item = e.getItem();
    	if (item.hasMetadata(META_VISUAL_REWARD)) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVisualPickupInvent(InventoryPickupItemEvent e) {
    	Item item = e.getItem();
    	if (item.hasMetadata(META_VISUAL_REWARD)) {
    		e.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCrateAnvilStop(PrepareAnvilEvent e) {
		AnvilInventory inv = e.getInventory();
		
		ItemStack item = inv.getItem(0);
		ItemStack book = inv.getItem(1);
		
		if (item != null && (this.plugin.getKeyManager().isKey(item) || this.isCrate(item))) {
			e.setResult(null);
			return;
		}
		if (book != null && (this.plugin.getKeyManager().isKey(book) || this.isCrate(book))) {
			e.setResult(null);
			return;
		}
    }
}
