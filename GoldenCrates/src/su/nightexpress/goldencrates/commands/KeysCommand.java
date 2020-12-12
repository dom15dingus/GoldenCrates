package su.nightexpress.goldencrates.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.goldencrates.GoldenCrates;
import su.nightexpress.goldencrates.Perms;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.manager.key.CrateKey;

public class KeysCommand extends ISubCommand<GoldenCrates> {

	public KeysCommand(@NotNull GoldenCrates plugin) {
		super(plugin, new String[] {"keys","checkkey"}, Perms.CMD_KEYS);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_CheckKey_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_CheckKey_Desc.getMsg();
	}
	
	@Override
	public boolean playersOnly() {
		return false;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return PlayerUT.getPlayerNames();
		}
		if (i == 2) {
			return plugin.getKeyManager().getKeyIds();
		}
		return super.getTab(player, i, args);
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length >= 2 && !sender.hasPermission(Perms.CMD_KEYS_OTHERS)) {
			this.errPerm(sender);
			return;
		}
		
		String pName = args.length >= 2 ? args[1] : sender.getName();
		Player player = plugin.getServer().getPlayer(pName);
		CrateUser user = plugin.getUserManager().getOrLoadUser(pName, false);
		if (user == null) {
			this.errPlayer(sender);
			return;
		}
		
		Map<CrateKey, Integer> keys = new HashMap<>();
		
		for (CrateKey key : plugin.getKeyManager().getKeys()) {
			int has = 0;
			if (!key.isVirtual()) {
				has = player != null ? plugin.getKeyManager().getKeys(player, key) : -2;
			}
			else {
				has = user.getKeys(key.getId());
			}
			keys.put(key, has);
		}
		
		plugin.lang().Command_CheckKey_Format_List.replace("%player%", user.getName()).asList()
		.forEach(line -> {
			if (line.contains("%amount%")) {
				keys.forEach((key, amount) -> {
					sender.sendMessage(line
						.replace("%key-name%", key.getName())
						.replace("%amount%", amount == -2 ? plugin.lang().Command_CheckKey_Format_ItemOff.getMsg() : String.valueOf(amount))
					);
				});
				return;
			}
			sender.sendMessage(line);
		});
	}
}
