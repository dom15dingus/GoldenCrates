package su.nightexpress.goldencrates.manager.crate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.Editable;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.manager.editor.crate.CrateEditorReward;

public class CrateReward implements Editable, Cleanable {

	private final Crate crate;
	private final String id;
	
	private double chance;
	private String name;
	
	private ItemStack item;
	private List<String> cmd;
	private ItemStack preview;
	
	private CrateEditorReward editor;
	
	public CrateReward(@NotNull Crate crate) {
		this(
			crate,										// Holder
			UUID.randomUUID().toString(),				// ID
			
			25D,										// Chance
			"New Reward #" + crate.getRewards().size(), // Name
			
			new ItemStack(Material.APPLE)	,			// Preview
			new ItemStack(Material.APPLE), 				// Item
			new ArrayList<>()							// Commands
		);
	}

	public CrateReward(
			@NotNull Crate crate,
			@NotNull String id,
			
			double chance,
			@NotNull String name,
			
			@Nullable ItemStack preview,
			@Nullable ItemStack item,
			@NotNull List<String> cmd
			) {
		this.crate = crate;
		this.id = id.toLowerCase();
		this.setChance(chance);
		this.setName(name);
		
		this.setItem(item);
		this.setCommands(cmd);
		this.setPreview(preview);
	}
	
	@Override
	@NotNull
	public CrateEditorReward getEditor() {
		if (this.editor == null) {
			this.editor = new CrateEditorReward((GoldenCrates) this.crate.plugin, this);
		}
		return this.editor;
	}

	@Override
	public void clear() {
		if (this.editor != null) {
			this.editor.shutdown();
			this.editor = null;
		}
	}

	@NotNull
	public Crate getCrate() {
		return this.crate;
	}
	
	@NotNull
	public String getId() {
		return this.id;
	}
	
	@NotNull
	public String getName() {
		return this.name;
	}
	
	public void setName(@NotNull String name) {
		this.name = StringUT.color(name);
	}
	
	public double getChance() {
		return this.chance;
	}
	
	public void setChance(double chance) {
		this.chance = chance;
	}
	
	@Nullable
	public ItemStack getItem() {
		return this.item == null ? null : new ItemStack(this.item);
	}
	
	public void setItem(@Nullable ItemStack item) {
		this.item = item == null ? null : new ItemStack(item);
	}
	
	@NotNull
	public List<String> getCommands() {
		return this.cmd;
	}
	
	public void setCommands(@NotNull List<String> cmd) {
		this.cmd = new ArrayList<>(cmd);
	}
	
	@NotNull
	public ItemStack getPreview() {
		return new ItemStack(this.preview);
	}
	
	public void setPreview(@Nullable ItemStack item) {
		if (item == null) {
			this.preview = this.item != null ? this.getItem() : new ItemStack(Material.BARRIER);
		}
		else {
			this.preview = new ItemStack(item);
		}
	}
	
	public void give(@NotNull Player player) {
		ItemStack item = this.getItem();
		if (item != null) {
			ItemUT.addItem(player, item);
		}
		for (String cmd : getCommands()) {
			PlayerUT.execCmd(player, cmd);
		}
	}
}
