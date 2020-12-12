package su.nightexpress.goldencrates.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.manager.menu.CrateMenu;

public class MenuCommand extends ISubCommand<GoldenCrates> {

	public MenuCommand(@NotNull GoldenCrates plugin) {
		super(plugin, new String[] {"menu"}, Perms.USER);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Menu_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Menu_Desc.getMsg();
	}
	
	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return plugin.getMenuManager().getMenuIds();
		}
		return super.getTab(player, i, args);
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 2) {
			this.printUsage(sender);
			return;
		}
		
		CrateMenu menu = plugin.getMenuManager().getMenuById(args[1]);
		if (menu == null) {
			plugin.lang().Menu_Invalid.replace("%menu%", args[1]).send(sender, true);
			return;
		}
		
		Player player = (Player) sender;
		menu.open(player);
	}
}
