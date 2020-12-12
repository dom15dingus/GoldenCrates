package su.nightexpress.goldencrates.config;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangTemplate;
import su.nightexpress.goldencrates.GoldenCrates;


public class Lang extends ILangTemplate {
	
	public Lang(@NotNull GoldenCrates plugin) {
		super(plugin);
	}
	
	@Override
	protected void setupEnums() {
		
	}
	
	public JLangMsg Command_Give_Usage = new JLangMsg("<player | *> <crate> [amount]");
	public JLangMsg Command_Give_Desc = new JLangMsg("Gives crate(s) to a player.");
	public JLangMsg Command_Give_Done = new JLangMsg("Given &ax%amount% %crate% &7crate(s) to &a%player%");
	public JLangMsg Command_Give_Notify = new JLangMsg("You recieved &ax%amount% &a%crate%");
	public JLangMsg Command_Give_Error = new JLangMsg("The crate must be an &cITEM &7crate!");
	
	public JLangMsg Command_GiveKey_Usage = new JLangMsg("<player | *> <crate> [amount]");
	public JLangMsg Command_GiveKey_Desc = new JLangMsg("Gives crate key(s) to a player.");
	public JLangMsg Command_GiveKey_Done = new JLangMsg("Given &ax%amount% &a%key% &7key(s) to &a%player%");
	public JLangMsg Command_GiveKey_Notify = new JLangMsg("You recieved &ax%amount% %key% &7key(s)!");
	public JLangMsg Command_GiveKey_Error_NoKey = new JLangMsg("&cInvalid key!");
	public JLangMsg Command_GiveKey_Error_NoUser = new JLangMsg("&cSuch user is not registered on the server.");
	
	public JLangMsg Command_CheckKey_Desc = new JLangMsg("Show amount of player keys.");
	public JLangMsg Command_CheckKey_Usage = new JLangMsg("[player]");
	public JLangMsg Command_CheckKey_Format_List = new JLangMsg(
			"&8&m              &8&l[ &e%player% &7Crate Keys &8&l]&8&m              &8"
			+ "\n"
			+ "&f▸ &a%key-name%: &f%amount%");
	public JLangMsg Command_CheckKey_Format_ItemOff = new JLangMsg("&ccould not inspect offline inventory.");
	
	public JLangMsg Command_Menu_Usage = new JLangMsg("[menu]");
	public JLangMsg Command_Menu_Desc = new JLangMsg("Opens crate menu.");
	
	public JLangMsg Crate_Error_Invalid = new JLangMsg("Invalid crate: &c%crate%&7.");
	public JLangMsg Crate_Open_Error_Cooldown = new JLangMsg("You have to wait &6%time% &7before open this crate again!");
	public JLangMsg Crate_Open_Error_AlreadyIn = new JLangMsg("&cYou're already opening a crate, please wait.");
	public JLangMsg Crate_Open_Error_NoKey = new JLangMsg("&cYou don't have a key for this crate!");
	public JLangMsg Crate_Open_Error_NoRewards = new JLangMsg("&cThis crate does not contains any rewards!");
	public JLangMsg Crate_Open_Error_NoMoney = new JLangMsg("&cYou don't have enough money to open this crate!");
	public JLangMsg Crate_Open_Error_NoExp = new JLangMsg("&cYou don't have enough exp to open this crate!");
	public JLangMsg Crate_Open_Reward_Info = new JLangMsg("You have opened a crate and got the reward: &a%reward%");
	public JLangMsg Crate_Open_Reward_Broadcast = new JLangMsg("&aPlayer &e%player% &ajust opened &e%crate% &aand got the reward(s): &c%reward%");
	public JLangMsg Crate_Open_Error_NoSlots = new JLangMsg("&cPlease clean up your inventory for &eat least %slots%&c slots!");
	
	public JLangMsg Crate_Placeholder_Cooldown_Blank = new JLangMsg("Ready to open!");
	
	public JLangMsg Editor_Tip_Commands = new JLangMsg(
			"&7"
			+ "\n"
			+ "&b&lCommand Tips:"
			+ "\n"
			+ "&7"
			+ "\n"
			+ "&2• &a[CONSOLE] <command> &2- Execute from Console."
			+ "\n"
			+ "&2• &a[OP] <command> &2- Execute as an Operator."
			+ "\n"
			+ "&2• &a<command> &2- Execute from a Player."
			+ "\n"
			+ "&2• &a%player% &2- Player name placeholder."
			+ "\n"
			+ "&7");
	
	public JLangMsg Editor_Tip_ID = new JLangMsg("&7Enter an ID name...");
	public JLangMsg Editor_Tip_Name = new JLangMsg("&7Enter a new name...");
	public JLangMsg Editor_Tip_KeyId = new JLangMsg("&7Enter key id...");
	public JLangMsg Editor_Tip_Chance = new JLangMsg("&7Enter a new chance...");
	public JLangMsg Editor_Tip_Command = new JLangMsg("&7Enter a new command...");
	public JLangMsg Editor_Tip_BlockLocation = new JLangMsg("&7Click a block to attach the crate...");
	public JLangMsg Editor_Tip_HologramText = new JLangMsg("&7Enter a new line...");
	public JLangMsg Editor_Tip_Cooldown = new JLangMsg("&7Enter a new value in seconds...");
	public JLangMsg Editor_Tip_NPC = new JLangMsg("&7Enter a NPC ID...");
	public JLangMsg Editor_Tip_Template = new JLangMsg("&7Enter a template name...");
	public JLangMsg Editor_Tip_OpenCost = new JLangMsg("&7Enter a new cost...");
	public JLangMsg Editor_Tip_MenuSlot = new JLangMsg("&7Enter a new slot...");
	public JLangMsg Editor_Tip_MenuName = new JLangMsg("&7Enter a menu name...");
	
	public JLangMsg Editor_Error_CrateExists = new JLangMsg("Crate with such id is already exists!");
	public JLangMsg Editor_Error_Key_Exists = new JLangMsg("Key with such id is already exists!");
	public JLangMsg Editor_Error_Template = new JLangMsg("No such template!");
	public JLangMsg Editor_Error_Menu = new JLangMsg("No such menu!");
	
	public JLangMsg Menu_Invalid = new JLangMsg("Invalid menu: &c'%menu%'&7!");
}
