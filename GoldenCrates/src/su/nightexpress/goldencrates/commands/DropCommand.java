package su.nightexpress.goldencrates.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class DropCommand extends ISubCommand<GoldenCrates> {

	public DropCommand(@NotNull GoldenCrates plugin) {
		super(plugin, new String[] {"drop"}, Perms.ADMIN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Drop_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Drop_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return plugin.getCrateManager().getCrateIds(false);
		}
		if (i == 2) {
			return LocUT.getWorldNames();
		}
		if (i == 3) {
			return Arrays.asList("<x>");
		}
		if (i == 4) {
			return Arrays.asList("<y>");
		}
		if (i == 5) {
			return Arrays.asList("<z>");
		}
		return super.getTab(player, i, args);
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 6) {
			this.printUsage(sender);
			return;
		}
		
		Crate crate = plugin.getCrateManager().getCrateById(args[1]);
		if (crate == null) {
			plugin.lang().Crate_Error_Invalid.replace("%id%", args[1]).send(sender);
			return;
		}
		
		World world = plugin.getServer().getWorld(args[2]);
		if (world == null) {
			plugin.lang().Error_NoWorld.replace("%world%", args[2]).send(sender);
			return;
		}
		
		double x = this.getNumD(sender, args[3], 0, true);
		double y = this.getNumD(sender, args[4], 0, true);
		double z = this.getNumD(sender, args[5], 0, true);
		Location location = new Location(world, x, y, z);
		world.dropItemNaturally(location, crate.getItem());
		
		plugin.lang().Command_Drop_Done
			.replace("%crate%", crate.getName())
			.replace("%x%", NumberUT.format(x))
			.replace("%y%", NumberUT.format(y))
			.replace("%z%", NumberUT.format(z))
			.replace("%world%", CoreConfig.getWorldName(world.getName()))
			.send(sender);
	}
}
