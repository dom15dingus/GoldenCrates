package su.nightexpress.goldencrates.manager.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.api.event.CrateOpenEvent;
import su.nightexpress.goldencrates.manager.crate.Crate;
import su.nightexpress.goldencrates.manager.crate.CrateReward;
import su.nightexpress.goldencrates.manager.key.CrateKey;

public class CrateTemplate extends LoadableItem implements Cleanable {

	private CrateTemplate.GUI gui;
	private Set<Spinner> spinners;
	
	private static final NamespacedKey TAG_REWARD_ID = new NamespacedKey(GoldenCrates.getInstance(), "REWARD_ID");
	
	public CrateTemplate(@NotNull GoldenCrates plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.spinners = new HashSet<>();
		for (String sId : cfg.getSection("spinners")) {
			Spinner spinner = new Spinner("spinners." + sId + ".");
			this.spinners.add(spinner);
		}
		
		this.gui = new CrateTemplate.GUI(plugin);
	}
	
	@Override
	public void clear() {
		if (this.spinners != null) {
			this.spinners.clear();
			this.spinners = null;
		}
		if (this.gui != null) {
			this.gui.shutdown();
			this.gui = null;
		}
	}
	
	@NotNull
	public Set<Spinner> getSpinners() {
		return spinners;
	}
	
	@NotNull
	public void startSpin(@NotNull Player player, @NotNull Crate crate, @Nullable ItemStack item) {
		this.gui.open(player, 1);
		Inventory inv = player.getOpenInventory().getTopInventory();
		
		this.spinners.forEach(spinner -> {
			spinner.runTask(player, crate, inv, item);
		});
	}
	
	@Override
	protected void save(@NotNull JYML cfg) {
		
	}
	
	static enum TemplateItemType {
		RAINBOW,
		RAINBOW_SYNC,
		;
	}
	
	static enum SpinnerModeType {
		TRAIN,
		HIDDEN,
		;
	}
	
	static enum SpinnerSlotMode {
		FORWARD,
		FORWARD_BACK,
		RANDOM,
		;
	}

	class GUI extends NGUI<GoldenCrates> {

		GUI(@NotNull GoldenCrates plugin) {
			super(plugin, getConfig(), "");
			
			JYML cfg = getConfig();
			for (String id : cfg.getSection("custom-content")) {
				GuiItem gi = cfg.getGuiItem("custom-content." + id + ".", TemplateItemType.class);
				if (gi == null) continue;
				
				this.addButton(gi);
			}
		}
		
		public void update(@NotNull Inventory inv) {
	    	for (GuiItem guiItem : this.getContent().values()) {
	    		if (guiItem.getType() != TemplateItemType.RAINBOW && guiItem.getType() != TemplateItemType.RAINBOW_SYNC) {
	    			continue;
	    		}
	    		
	    		ItemStack item = guiItem.getItem();
	    		if (guiItem.getType() == TemplateItemType.RAINBOW_SYNC) {
	    			item.setType(Rnd.getColoredMaterial(item.getType()));
	    		}
	    		
	    		for (int slot : guiItem.getSlots()) {
	    			
	    			// Prevent to replace reward items.
	    			ItemStack itemHas = inv.getItem(slot);
	    			if (itemHas != null) {
	    				String rewardId = DataUT.getStringData(itemHas, TAG_REWARD_ID);
		    			if (rewardId != null) continue;
	    			}
	    			
	    			if (guiItem.getType() == TemplateItemType.RAINBOW) {
	        			item.setType(Rnd.getColoredMaterial(item.getType()));
	        		}
	    			inv.setItem(slot, item);
	    		}
	    	}
		}
		
		@Override
		protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
			this.update(inv);
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
	}
	
	public class Spinner {
		
		private GoldenCrates plugin;
		
		private int timeStartDelayTicks;
		private int timeUpdateTicks;
		private double timeRollTicks;
		private double timeEndTicks;
		private double timeEndRndOffset;
		private String rollSound;
		
		private SpinnerModeType modeType;
		private SpinnerSlotMode modeHiddenSlotMode;
		
		private int[] rewardSlots;
		private int[] winSlots;
		
		Spinner(@NotNull String path) {
			this.plugin = (GoldenCrates) CrateTemplate.this.plugin;
			JYML cfg = getConfig();
			
			this.timeStartDelayTicks = cfg.getInt(path + "time.start-delay-ticks");
			this.timeUpdateTicks = cfg.getInt(path + "time.spin-task-ticks");
			this.timeRollTicks = cfg.getDouble(path + "time.roll-every-ticks");
			this.timeEndTicks = Math.ceil(20 * cfg.getDouble(path + "time.finish-second"));
			this.timeEndRndOffset = cfg.getDouble(path + "time.finish-random-offset");
			
			this.rollSound = cfg.getString(path + "roll-sound", "");
			
			this.modeType = CollectionsUT.getEnum(cfg.getString(path + "mode.type", "TRAIN"), SpinnerModeType.class);
			this.modeHiddenSlotMode = CollectionsUT.getEnum(cfg.getString(path + "mode.hidden.slot-mode", "RANDOM"), SpinnerSlotMode.class);
			
			this.rewardSlots = cfg.getIntArray(path + "reward-slots");
			this.winSlots = cfg.getIntArray(path + "win-slots");
		}
		
		public int getStartDelayTicks() {
			return timeStartDelayTicks;
		}
		
		public int getSpinTaskTicks() {
			return this.timeUpdateTicks;
		}
		
		public double getRollEveryTicks() {
			return this.timeRollTicks;
		}
		
		public double getTaskEndTicks() {
			return this.timeEndTicks;
		}
		
		public double getTaskEndRandomOffset() {
			return timeEndRndOffset;
		}
		
		@NotNull
		public String getRollSound() {
			return this.rollSound;
		}
		
		public int[] getRewardSlots() {
			return this.rewardSlots;
		}
		
		public int[] getWinSlots() {
			return this.winSlots;
		}
		
		@NotNull
		public SpinnerModeType getModeType() {
			return modeType;
		}
		
		@NotNull
		public SpinnerSlotMode getHiddenSlotMode() {
			return modeHiddenSlotMode;
		}
		
		@NotNull
		public SpinTask runTask(@NotNull Player player, @NotNull Crate crate, @NotNull Inventory inv, @Nullable ItemStack item) {
	    	SpinTask task = new SpinTask(this, player, crate, inv, item);
	    	task.runTaskTimer(plugin, this.getStartDelayTicks(), this.getSpinTaskTicks());
	    	
	    	TemplateManager.SPIN_TASKS.computeIfAbsent(player, set -> new HashSet<>()).add(task);
	    	
	    	return task;
		}
	}
	
	public class SpinTask extends BukkitRunnable {
		
		private GoldenCrates plugin;
		
		private Player player;
		private Crate crate;
		private Inventory inv;
		private Spinner spinner;
		private ItemStack item;
		
		private int hiddenLastSlotIndex;
		private boolean hiddenMoveBack;
		private int hiddenSlotLoopFixer;
		private Map<Integer, ItemStack> hiddenLastItems;
		
		private int tickCounter;
		private double tickMaximum;
		
		public SpinTask(
				@NotNull Spinner spinner, 
				@NotNull Player player, 
				@NotNull Crate crate, 
				@NotNull Inventory inv,
				@Nullable ItemStack item) {
			this.plugin = spinner.plugin;
			
			this.player = player;
			this.crate = crate;
			this.inv = inv;
			this.spinner = spinner;
			this.item = item;
			
			if (this.getSpinner().getModeType() == SpinnerModeType.HIDDEN) {
				this.hiddenLastSlotIndex = 0;
				this.hiddenSlotLoopFixer = 0;
				this.hiddenMoveBack = false;
				this.hiddenLastItems = new HashMap<>();
			}
			
			double rndOffset = Math.ceil((Rnd.getDoubleNega(0, this.getSpinner().getTaskEndRandomOffset()) * 20));
			this.tickMaximum = (int) this.getSpinner().getTaskEndTicks() + rndOffset;
			this.tickCounter = 0;
		}
		
		@NotNull
		public Spinner getSpinner() {
			return spinner;
		}
		
		@Nullable
		public ItemStack getCrateItem() {
			return item;
		}
		
		public double getTickMaximum() {
			return tickMaximum;
		}
		
		@Override
	    public void run() {
			if (this.player == null) {
				this.cancel();
				return;
			}
	        
			this.tickCounter++;
			
	        // One second pause before reward give.
	        if (this.tickCounter > this.getTickMaximum() - 20) {
	            if (this.tickCounter >= this.getTickMaximum()) {
	                this.finish(false);
	            }
	            return;
	        }
	        
	        if (this.tickCounter > this.getTickMaximum() * 0.66) {
		        if (this.tickCounter % 7 != 0) {
		            return;
		        }
		    }
	        else if (this.tickCounter > this.getTickMaximum() * 0.55) {
		        if (this.tickCounter % 5 != 0) {
		            return;
		        }
		    }
	        else if (this.tickCounter > this.getTickMaximum() * 0.33) {
		        if (this.tickCounter % 3 != 0) {
		            return;
		        }
		    }
	        
	        this.spinReward();
	    }
		
	    private void spinReward() {
	    	gui.update(this.inv);
	    	
	    	if (this.tickCounter % this.getSpinner().getRollEveryTicks() != 0) {
	    		return;
	    	}
	    	MsgUT.sound(this.player, this.getSpinner().getRollSound());
	    	
	    	CrateReward reward = this.crate.rollReward();
	    	ItemStack preview = reward.getPreview();
	    	DataUT.setData(preview, TAG_REWARD_ID, reward.getId());
	    	
	    	if (this.getSpinner().getModeType() == SpinnerModeType.TRAIN) {
	    		this.spinRewardTrain(reward, preview);
	    	}
	    	else {
	    		this.spinRewardHidden(reward, preview);
	    	}
	    	
	    	if (this.inv.getViewers().isEmpty()) {
	    		this.player.openInventory(this.inv);
	    	}
	    }
	    
	    private void spinRewardTrain(@NotNull CrateReward reward, @NotNull ItemStack preview) {
	    	if (this.getSpinner().getRewardSlots().length == 1) {
	    		int slot = this.getSpinner().getRewardSlots()[0];
	    		this.inv.setItem(slot, preview);
	    	}
	    	else {
		    	for (int i = this.getSpinner().getRewardSlots().length - 1; i > -1; i--) {
		    		int slot = this.getSpinner().getRewardSlots()[i];
		    		if (i == 0) {
		    			this.inv.setItem(slot, preview);
		    		}
		    		else {
		    			int slot2 = this.getSpinner().getRewardSlots()[i-1];
		    			this.inv.setItem(slot, this.inv.getItem(slot2));
		    		}
		    	}
	    	}
	    }
	    
	    private void spinRewardHidden(@NotNull CrateReward reward, @NotNull ItemStack preview) {
	    	SpinnerSlotMode slotMode = this.getSpinner().getHiddenSlotMode();
	    	int[] slots = this.getSpinner().getRewardSlots();
	    	int length = slots.length - 1;
	    	
	    	int slot;
	    	if (slotMode == SpinnerSlotMode.RANDOM) {
	    		slot = Rnd.get(slots);
	    	}
	    	else {
	    		if (slotMode == SpinnerSlotMode.FORWARD) {
	    			if (this.hiddenLastSlotIndex >= length + 1) {
	    				this.hiddenLastSlotIndex = 0;
	    			}
    			}
	    		else if (slotMode == SpinnerSlotMode.FORWARD_BACK) {
	    			if (this.hiddenLastSlotIndex >= length) {
	    				this.hiddenLastSlotIndex = length;
	    				this.hiddenMoveBack = true;
	    			}
	    			else if (this.hiddenLastSlotIndex <= 0) {
	    				this.hiddenLastSlotIndex = 0;
	    				this.hiddenMoveBack = false;
	    			}
	    		}
	    		
	    		slot = slots[this.hiddenLastSlotIndex];
	    		
	    		if (this.hiddenMoveBack) {
	    			this.hiddenLastSlotIndex--;
	    		}
	    		else {
	    			this.hiddenLastSlotIndex++;
	    		}
	    	}
	    	
	    	this.hiddenLastItems.forEach((slotStored, itemStored) -> {
	    		this.inv.setItem(slotStored, itemStored);
	    	});
	    	this.hiddenLastItems.clear();
	    	
	    	ItemStack itemHas = this.inv.getItem(slot);
	    	if (itemHas == null) itemHas = new ItemStack(Material.AIR);
	    	
	    	// If rolled slot already contains some reward (ex: from other spinners),
	    	// then we should roll it again until it find a free slot.
	    	String rewardId = DataUT.getStringData(itemHas, TAG_REWARD_ID);
	    	if (rewardId != null) {
	    		// Prevent infinite loops.
	    		if (this.hiddenSlotLoopFixer++ < length) {
	    			this.spinRewardHidden(reward, preview);
	    		}
	    		else {
	    			this.plugin.warn("Infinite loop interruped for '" + getId() + "' crate template! A fatal error when trying to find a free reward slot (Rewards amount is greater than reward slots).");
	    		}
	    		return;
	    	}
	    	
	    	this.hiddenLastItems.put(slot, itemHas);
	    	this.inv.setItem(slot, preview);
	    }
	    
	    public void finish(boolean force) {
	    	this.cancel();
	    	if (this.player == null) return;
	    	
	    	Set<SpinTask> spinTasks = TemplateManager.SPIN_TASKS.get(player);
	    	if (spinTasks == null) return;
	    	
	    	boolean isAllStopped = spinTasks.stream().allMatch(task -> task.isCancelled());
	    	if (!isAllStopped) return;
	    	
	    	TemplateManager.SPIN_TASKS.remove(this.player);
	    	this.player.closeInventory();
	    	
	    	if (force) {
			    if (this.getCrateItem() != null) {
			    	plugin.getCrateManager().giveCrate(this.player, this.crate, 1);
			    }
			    
			    CrateKey crateKey = plugin.getKeyManager().getKeyByCrate(this.crate);
			    if (crateKey != null) {
			    	plugin.getKeyManager().giveKey(player, crateKey, 1);
			    }
	    		return;
	    	}
	    	
	    	StringBuilder rewards = new StringBuilder();
	    	
	    	CrateOpenEvent openEvent = new CrateOpenEvent(crate, player);
	    	
	    	for (SpinTask task : spinTasks) {
	    		for (int winSlot : task.getSpinner().getWinSlots()) {
				    ItemStack win = this.inv.getItem(winSlot);
				    if (win == null) continue;
				      
				    String id = DataUT.getStringData(win, TAG_REWARD_ID);
				    if (id == null) continue;
				    
				    CrateReward reward = this.crate.getReward(id);
				    if (reward == null) continue;
				       
				    openEvent.getRewards().add(reward);
				    
				    this.inv.setItem(winSlot, null); // Clear reward to avoid duplication.
	    		}
	    	}
	    	
	    	plugin.getPluginManager().callEvent(openEvent);
	    	
	    	openEvent.getRewards().forEach(reward -> {
	    		reward.give(this.player);
			    
			    if (rewards.length() > 0) {
			    	rewards.append(", ");
			    }
			    rewards.append(reward.getName());
	    	});
	    	
	    	plugin.getCrateManager().broadcastReward(this.player, this.crate, rewards.toString());
	    }
	}
}
