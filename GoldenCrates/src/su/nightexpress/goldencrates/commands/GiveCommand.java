package su.nightexpress.goldencrates.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class GiveCommand extends ISubCommand<GoldenCrates> {

	public GiveCommand(@NotNull GoldenCrates plugin) {
		super(plugin, new String[] {"give"}, Perms.ADMIN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Give_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Give_Desc.getMsg();
	}
	
	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		switch (i) {
			case 1: {
				List<String> list = PlayerUT.getPlayerNames();
				list.add(0, JStrings.MASK_ANY);
				return list;
			}
			case 2: {
				return plugin.getCrateManager().getCrateIds(false);
			}
			case 3: {
				return Arrays.asList("1", "5", "10");
			}
		}
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 3) {
			this.printUsage(sender);
			return;
		}
		
		String pName = args[1];
		String crateId = args[2];
		int amount = args.length >= 4 ? this.getNumI(sender, args[3], 1) : 1;
		
		Crate crate = plugin.getCrateManager().getCrateById(crateId);
		if (crate == null) {
			plugin.lang().Crate_Error_Invalid.replace("%crate%", crateId).send(sender);
			return;
		}
		
		if (pName.equalsIgnoreCase(JStrings.MASK_ANY)) {
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (player == null) continue;
				
				plugin.getCrateManager().giveCrate(player, crate, amount);
			}
		}
		else {
			Player player = plugin.getServer().getPlayer(pName);
			if (player == null) {
				this.errPlayer(sender);
				return;
			}
			plugin.getCrateManager().giveCrate(player, crate, amount);
		}
		
		plugin.lang().Command_Give_Done
			.replace("%player%", pName)
			.replace("%amount%", String.valueOf(amount))
			.replace("%crate%", crate.getName())
			.send(sender);
	}
}
