package su.nightexpress.goldencrates.manager.key;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class KeyManager extends IManager<GoldenCrates> {

	private Map<String, CrateKey> keysMap;
	private JYML cfgKeysTemp;
	
	public static final NamespacedKey TAG_KEY = new NamespacedKey(GoldenCrates.getInstance(), "GCRATES_KEY");
	
	public KeyManager(@NotNull GoldenCrates plugin) {
		super(plugin);
	}

	@Override
	public void setup() {
		this.keysMap = new HashMap<>();
		this.cfgKeysTemp = new JYML(plugin.getDataFolder().getAbsolutePath(), "keys.tmp");
		this.plugin.getConfigManager().extract(Config.DIR_KEYS);
		
		for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + Config.DIR_KEYS, true)) {
			try {
				CrateKey crateKey = new CrateKey(plugin, cfg);
				this.keysMap.put(crateKey.getId(), crateKey);
			}
			catch (Exception ex) {
				plugin.error("Could not load '" + cfg.getFile().getName() +"' crate key!");
				ex.printStackTrace();
			}
		}
		this.plugin.info("Loaded " + this.keysMap.size() + " crate keys!");
		
		this.registerListeners();
	}

	@Override
	public void shutdown() {
		this.unregisterListeners();
		
		if (this.keysMap != null) {
			this.keysMap.clear();
			this.keysMap = null;
		}
	}
	
	public boolean create(@NotNull String id) {
    	if (this.getKeyById(id) != null) {
    		return false;
    	}
    	
		this.save(new CrateKey(plugin, id));
		return true;
	}
	
	public void save(@NotNull CrateKey crateKey) {
		crateKey.save();
    	this.keysMap.put(crateKey.getId(), crateKey);
	}
	
	public void delete(@NotNull CrateKey crateKey) {
    	File file = crateKey.getFile();
    	file.delete();
    	
    	crateKey.clear(); // Unregister GUI editors
    	this.keysMap.remove(crateKey.getId());
	}

	@NotNull
	public Collection<CrateKey> getKeys() {
		return this.keysMap.values();
	}
	
	@NotNull
	public List<String> getKeyIds() {
		return new ArrayList<>(this.keysMap.keySet());
	}

	@Nullable
	public CrateKey getKeyById(@NotNull String id) {
		return this.keysMap.get(id.toLowerCase());
	}
	
	@Nullable
	public CrateKey getKeyByItem(@NotNull ItemStack item) {
		String id = DataUT.getStringData(item, TAG_KEY);
		return id == null ? null : this.getKeyById(id);
	}
	
	@Nullable
	public CrateKey getKeyByCrate(@NotNull Crate crate) {
		String keyId = crate.getKeyId();
		return keyId == null ? null : this.getKeyById(keyId);
	}
	
	@Nullable
	public ItemStack getFirstKeyStack(@NotNull Player player, @NotNull CrateKey crateKey) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null || ItemUT.isAir(item)) continue;
			
			CrateKey crateKey2 = this.getKeyByItem(item);
			if (crateKey2 != null && crateKey2.equals(crateKey)) {
				return item;
			}
		}
		return null;
	}

	public boolean isKey(@NotNull ItemStack item) {
		return this.getKeyByItem(item) != null;
	}
	
	public int getKeys(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = plugin.getKeyManager().getKeyByCrate(crate);
		return key == null ? -1 : this.getKeys(player, key);
	}
	
	public int getKeys(@NotNull Player player, @NotNull CrateKey crateKey) {
		if (crateKey.isVirtual()) {
			CrateUser user = plugin.getUserManager().getOrLoadUser(player);
			return user != null ? user.getKeys(crateKey.getId()) : 0;
		}
		
		int total = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			CrateKey keyItem = ItemUT.isAir(item) ? null : plugin.getKeyManager().getKeyByItem(item);
			if (keyItem != null && keyItem.getId().equalsIgnoreCase(crateKey.getId())) {
				total += item.getAmount();
			}
		}
		return total;
	}
	
	public boolean hasKey(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = this.getKeyByCrate(crate);
		return key == null ? true : this.hasKey(player, key);
	}
	
	public boolean hasKey(@NotNull Player player, @NotNull CrateKey crateKey) {
		if (crateKey.isVirtual()) {
			CrateUser user = plugin.getUserManager().getOrLoadUser(player);
			return user != null ? user.getKeys(crateKey.getId()) > 0 : false;
		}
		
		ItemStack keyItem = crateKey.getItem();
		return player.getInventory().containsAtLeast(keyItem, 1);
	}
	
	public void giveOfflineKeys(@NotNull Player player) {
		for (String keyId : this.cfgKeysTemp.getSection(player.getName())) {
			int amount = this.cfgKeysTemp.getInt(player.getName() + "." + keyId);
			if (amount == 0) continue;
			
			CrateKey crateKey = this.getKeyById(keyId);
			if (crateKey == null) continue;
			
			this.giveKey(player, crateKey, amount);
		}
		this.cfgKeysTemp.remove(player.getName());
		this.cfgKeysTemp.saveChanges();
	}

	public boolean giveKey(@NotNull String pName, @NotNull Crate crate, int amount) {
		CrateKey key = this.getKeyByCrate(crate);
		return key == null ? false : this.giveKey(pName, key, amount);
	}
	
	public boolean giveKey(@NotNull String pName, @NotNull CrateKey key, int amount) {
		Player player = plugin.getServer().getPlayer(pName);
		if (player != null) {
			return this.giveKey(player, key, amount);
		}
		
		// Store key for offline player to give it later.
		if (plugin.getData().isUserExists(pName, false)) {
			String path = pName + "." + key.getId();
			int stored = this.cfgKeysTemp.getInt(path, 0) + amount;
			this.cfgKeysTemp.set(path, stored);
			this.cfgKeysTemp.saveChanges();
			return true;
		}
		
		return false;
	}
	
	public boolean giveKey(@NotNull Player player, @NotNull Crate crate, int amount) {
		CrateKey key = this.getKeyByCrate(crate);
		return key == null ? false : this.giveKey(player, key, amount);
	}
	
	public boolean giveKey(@NotNull Player player, @NotNull CrateKey key, int amount) {
		if (key.isVirtual()) {
			CrateUser user = plugin.getUserManager().getOrLoadUser(player);
			if (user == null) return false;
			
			user.addKeys(key.getId(), amount);
			
			if (plugin.cfg().dataSaveInstant) {
				plugin.getUserManager().save(user, true);
			}
		}
		else {
			ItemStack keyItem = key.getItem();
			keyItem.setAmount(amount < 0 ? Math.abs(amount) : amount);
			if (amount < 0) {
				player.getInventory().removeItem(keyItem);
			}
			else {
				ItemUT.addItem(player, keyItem);
			}
		}
		
		plugin.lang().Command_GiveKey_Notify
				.replace("%amount%", amount)
				.replace("%key%", key.getName())
				.send(player, true);
		return true;
	}
	
	public boolean takeKey(@NotNull Player player, @NotNull Crate crate) {
		CrateKey key = this.getKeyByCrate(crate);
		return key != null ? this.takeKey(player, key) : false;
	}
	
	public boolean takeKey(@NotNull Player player, @NotNull CrateKey crateKey) {
		if (crateKey.isVirtual()) {
			CrateUser user = plugin.getUserManager().getOrLoadUser(player);
			if (user == null || user.getKeys(crateKey.getId()) < 1) return false;
			
			user.takeKeys(crateKey.getId(), 1);
			
			if (plugin.cfg().dataSaveInstant) {
				plugin.getUserManager().save(user, true);
			}
			
			return true;
		}
		
		ItemStack keyStack = this.getFirstKeyStack(player, crateKey);
		if (keyStack == null) return false;
		
		keyStack.setAmount(keyStack.getAmount() - 1);
		return true;
	}
	
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onKeyPlace(BlockPlaceEvent e) {
    	ItemStack item = e.getItemInHand();
    	e.setCancelled(this.isKey(item));
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKeyUse(PlayerInteractEvent e) {
    	if (e.useItemInHand() == Result.DENY) return;
    	if (e.useInteractedBlock() == Result.DENY) return;
    	
    	ItemStack item = e.getItem();
    	if (item != null && this.isKey(item)) {
    		
    		Player player = e.getPlayer();
        	Block clickedBlock = e.getClickedBlock();
        	if (clickedBlock != null && clickedBlock.getType().isInteractable() && !player.isSneaking()) {
        		return;
        	}
    		
	    	e.setUseItemInHand(Result.DENY);
	    	e.setUseInteractedBlock(Result.DENY);
    	}
    }
}
