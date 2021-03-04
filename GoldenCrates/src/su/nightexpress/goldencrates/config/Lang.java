package su.nightexpress.goldencrates.config;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.core.config.CoreLang;
import su.nightexpress.goldencrates.GoldenCrates;


public class Lang extends CoreLang {
	
	public Lang(@NotNull GoldenCrates plugin) {
		super(plugin);
	}
	
	@Override
	protected void setupEnums() {
		
	}
	
	public ILangMsg Command_Give_Usage = new ILangMsg(this, "<player | *> <crate> [amount]");
	public ILangMsg Command_Give_Desc = new ILangMsg(this, "Gives crate(s) to a player.");
	public ILangMsg Command_Give_Done = new ILangMsg(this, "Given &ax%amount% %crate% &7crate(s) to &a%player%");
	public ILangMsg Command_Give_Notify = new ILangMsg(this, "You recieved &ax%amount% &a%crate%");
	
	public ILangMsg Command_Drop_Usage = new ILangMsg(this, "<crate> <world> <x> <y> <z>");
	public ILangMsg Command_Drop_Desc = new ILangMsg(this, "Drop crate at specified location.");
	public ILangMsg Command_Drop_Done = new ILangMsg(this, "Dropped &a%crate%&7 at &a%x%&7, &a%y%&7, &a%z%&7 in &a%world%&7.");
	
	public ILangMsg Command_GiveKey_Usage = new ILangMsg(this, "<player | *> <crate> [amount]");
	public ILangMsg Command_GiveKey_Desc = new ILangMsg(this, "Gives crate key(s) to a player.");
	public ILangMsg Command_GiveKey_Done = new ILangMsg(this, "Given &ax%amount% &a%key% &7key(s) to &a%player%");
	public ILangMsg Command_GiveKey_Notify = new ILangMsg(this, "You recieved &ax%amount% %key% &7key(s)!");
	public ILangMsg Command_GiveKey_Error_NoKey = new ILangMsg(this, "&cInvalid key!");
	
	public ILangMsg Command_CheckKey_Desc = new ILangMsg(this, "Show amount of player keys.");
	public ILangMsg Command_CheckKey_Usage = new ILangMsg(this, "[player]");
	public ILangMsg Command_CheckKey_Format_List = new ILangMsg(
			this, 
			"&8&m              &8&l[ &e%player% &7Crate Keys &8&l]&8&m              &8"
			+ "\n"
			+ "&f▸ &a%key-name%: &f%amount%");
	public ILangMsg Command_CheckKey_Format_ItemOff = new ILangMsg(this, "&ccould not inspect offline inventory.");
	
	public ILangMsg Command_Menu_Usage = new ILangMsg(this, "[menu]");
	public ILangMsg Command_Menu_Desc = new ILangMsg(this, "Opens crate menu.");
	
	public ILangMsg Crate_Error_Invalid = new ILangMsg(this, "Invalid crate: &c%crate%&7.");
	public ILangMsg Crate_Open_Error_Cooldown = new ILangMsg(this, "You have to wait &6%time% &7before open this crate again!");
	public ILangMsg Crate_Open_Error_AlreadyIn = new ILangMsg(this, "&cYou're already opening a crate, please wait.");
	public ILangMsg Crate_Open_Error_NoKey = new ILangMsg(this, "&cYou don't have a key for this crate!");
	public ILangMsg Crate_Open_Error_NoRewards = new ILangMsg(this, "&cThis crate does not contains any rewards!");
	public ILangMsg Crate_Open_Error_NoMoney = new ILangMsg(this, "&cYou don't have enough money to open this crate!");
	public ILangMsg Crate_Open_Error_NoExp = new ILangMsg(this, "&cYou don't have enough exp to open this crate!");
	public ILangMsg Crate_Open_Reward_Info = new ILangMsg(this, "You have opened a crate and got the reward: &a%reward%");
	public ILangMsg Crate_Open_Reward_Broadcast = new ILangMsg(this, "&aPlayer &e%player% &ajust opened &e%crate% &aand got the reward(s): &c%reward%");
	public ILangMsg Crate_Open_Error_NoSlots = new ILangMsg(this, "&cPlease clean up your inventory for &eat least %slots%&c slots!");
	
	public ILangMsg Crate_Placeholder_Cooldown_Blank = new ILangMsg(this, "Ready to open!");
	
	public ILangMsg Editor_Tip_ID = new ILangMsg(this, "&7Enter an ID name...");
	public ILangMsg Editor_Tip_Name = new ILangMsg(this, "&7Enter a new name...");
	public ILangMsg Editor_Tip_KeyId = new ILangMsg(this, "&7Enter key id...");
	public ILangMsg Editor_Tip_Chance = new ILangMsg(this, "&7Enter a new chance...");
	public ILangMsg Editor_Tip_Command = new ILangMsg(this, "&7Enter a new command...");
	public ILangMsg Editor_Tip_BlockLocation = new ILangMsg(this, "&7Click a block to attach the crate...");
	public ILangMsg Editor_Tip_HologramText = new ILangMsg(this, "&7Enter a new line...");
	public ILangMsg Editor_Tip_Cooldown = new ILangMsg(this, "&7Enter a new value in seconds...");
	public ILangMsg Editor_Tip_NPC = new ILangMsg(this, "&7Enter a NPC ID...");
	public ILangMsg Editor_Tip_Template = new ILangMsg(this, "&7Enter the template id...");
	public ILangMsg Editor_Tip_Preview = new ILangMsg(this, "&7Enter the preview id...");
	public ILangMsg Editor_Tip_OpenCost = new ILangMsg(this, "&7Enter a new cost...");
	public ILangMsg Editor_Tip_MenuSlot = new ILangMsg(this, "&7Enter a new slot...");
	public ILangMsg Editor_Tip_MenuName = new ILangMsg(this, "&7Enter a menu name...");
	
	public ILangMsg Editor_Error_CrateExists = new ILangMsg(this, "Crate with such id is already exists!");
	public ILangMsg Editor_Error_Key_Exists = new ILangMsg(this, "Key with such id is already exists!");
	public ILangMsg Editor_Error_Template = new ILangMsg(this, "No such template!");
	public ILangMsg Editor_Error_Menu = new ILangMsg(this, "No such menu!");
	
	public ILangMsg Menu_Invalid = new ILangMsg(this, "Invalid menu: &c'%menu%'&7!");
}
