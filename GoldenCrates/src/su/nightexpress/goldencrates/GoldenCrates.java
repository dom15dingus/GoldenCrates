package su.nightexpress.goldencrates;

import java.io.File;
import java.sql.SQLException;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nightexpress.goldencrates.commands.GiveCommand;
import su.nightexpress.goldencrates.commands.GivekeyCommand;
import su.nightexpress.goldencrates.commands.KeysCommand;
import su.nightexpress.goldencrates.commands.MenuCommand;
import su.nightexpress.goldencrates.config.Config;
import su.nightexpress.goldencrates.config.Lang;
import su.nightexpress.goldencrates.data.CrateUser;
import su.nightexpress.goldencrates.data.CrateUserData;
import su.nightexpress.goldencrates.data.UserManager;
import su.nightexpress.goldencrates.hooks.EHook;
import su.nightexpress.goldencrates.hooks.external.CitizensHook;
import su.nightexpress.goldencrates.hooks.external.HologramsHook;
import su.nightexpress.goldencrates.hooks.external.PlaceholderHK;
import su.nightexpress.goldencrates.manager.crate.CrateManager;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHandler;
import su.nightexpress.goldencrates.manager.editor.CrateEditorHub;
import su.nightexpress.goldencrates.manager.key.KeyManager;
import su.nightexpress.goldencrates.manager.menu.MenuManager;
import su.nightexpress.goldencrates.manager.template.TemplateManager;

public class GoldenCrates extends NexDataPlugin<GoldenCrates, CrateUser> {

	private static GoldenCrates instance;
	
	private Config config;
	private Lang lang;
	
	public static JYML EDITOR_MAIN;
	
	public static JYML EDITOR_CRATE_LIST;
	public static JYML EDITOR_CRATE;
	public static JYML EDITOR_REWARD_LIST;
	public static JYML EDITOR_REWARD;
	public static JYML EDITOR_EFFECTS;
	
	public static JYML EDITOR_KEY_LIST;
	public static JYML EDITOR_KEY_KEY;
	
	private CrateUserData dataHandler;
	private KeyManager keyManager;
	private TemplateManager templateManager;
    private CrateManager crateManager;
    private MenuManager menuManager;
    
    public static GoldenCrates getInstance() {
    	return instance;
    }
    
	public GoldenCrates() {
	    instance = this;
	}
	
	@Override
	public void enable() {
		this.templateManager = new TemplateManager(this);
		this.templateManager.setup();
		
		this.keyManager = new KeyManager(this);
		this.keyManager.setup();
		
	    this.crateManager = new CrateManager(this);
	    this.crateManager.setup();
	    
	    this.menuManager = new MenuManager(this);
	    this.menuManager.setup();
	}

	@Override
	public void disable() {
		if (this.templateManager != null) {
			this.templateManager.shutdown();
			this.templateManager = null;
		}
		if (this.keyManager != null) {
			this.keyManager.shutdown();
			this.keyManager = null;
		}
		if (this.crateManager != null) {
			this.crateManager.shutdown();
			this.crateManager = null;
		}
		if (this.menuManager != null) {
			this.menuManager.shutdown();
			this.menuManager = null;
		}
	}

	@Override
	public void setConfig() {
		this.config = new Config(this);
		this.config.setup();
		
		this.lang = new Lang(this);
		this.lang.setup();
	}

	@Override
	protected boolean setupDataHandlers() {
		try {
			this.dataHandler = CrateUserData.getInstance(this);
			this.dataHandler.setup();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		
		this.userManager = new UserManager(this);
		this.userManager.setup();
		
		return true;
	}

	@Override
	@NotNull
	public Config cfg() {
		return this.config;
	}
	
	@Override
	@NotNull
	public Lang lang() {
		return this.lang;
	}

	@Override
	public void registerHooks() {
		this.registerHook(EHook.HOLOGRAPHIC_DISPLAYS, HologramsHook.class);
		if (Hooks.hasPlaceholderAPI()) {
			this.registerHook(Hooks.PLACEHOLDER_API, PlaceholderHK.class);
		}
		CitizensHK citizensHK = this.getCitizens();
		if (citizensHK != null) {
			citizensHK.addListener(this, new CitizensHook());
		}
	}

	@Override
	public void registerCmds(@NotNull IGeneralCommand<GoldenCrates> mainCommand) {
		mainCommand.addSubCommand(new GiveCommand(this));
		mainCommand.addSubCommand(new GivekeyCommand(this));
		mainCommand.addSubCommand(new MenuCommand(this));
		mainCommand.addSubCommand(new KeysCommand(this));
	}
    
    @Override
	public void registerEditor() {
    	this.getConfigManager().extract("editor");
    	
    	if (EDITOR_MAIN == null || !EDITOR_MAIN.reload()) {
    		EDITOR_MAIN = new JYML(new File(this.getDataFolder() + "/editor/editor_main.yml"));
    	}
    	
    	
    	if (EDITOR_CRATE_LIST == null || !EDITOR_CRATE_LIST.reload()) {
    		EDITOR_CRATE_LIST = new JYML(new File(this.getDataFolder() + "/editor/crate_list.yml"));
    	}
    	if (EDITOR_CRATE == null || !EDITOR_CRATE.reload()) {
    		EDITOR_CRATE = new JYML(new File(this.getDataFolder() + "/editor/crate_main.yml"));
    	}
    	if (EDITOR_REWARD_LIST == null || !EDITOR_REWARD_LIST.reload()) {
    		EDITOR_REWARD_LIST = new JYML(new File(this.getDataFolder() + "/editor/crate_rewards_list.yml"));
    	}
    	if (EDITOR_REWARD == null || !EDITOR_REWARD.reload()) {
    		EDITOR_REWARD = new JYML(new File(this.getDataFolder() + "/editor/crate_rewards_reward.yml"));
    	}
    	if (EDITOR_EFFECTS == null || !EDITOR_EFFECTS.reload()) {
    		EDITOR_EFFECTS = new JYML(new File(this.getDataFolder() + "/editor/crate_effects.yml"));
    	}
    	
    	
    	if (EDITOR_KEY_LIST == null || !EDITOR_KEY_LIST.reload()) {
    		EDITOR_KEY_LIST = new JYML(new File(this.getDataFolder() + "/editor/key_list.yml"));
    	}
    	if (EDITOR_KEY_KEY == null || !EDITOR_KEY_KEY.reload()) {
    		EDITOR_KEY_KEY = new JYML(new File(this.getDataFolder() + "/editor/key_key.yml"));
    	}
    	
		CrateEditorHub main = new CrateEditorHub(this, EDITOR_MAIN);
		this.editorHandler = new CrateEditorHandler(this, main);
	}

	@Override
	public CrateUserData getData() {
		return this.dataHandler;
	}
	
	@NotNull
	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	@NotNull
	public KeyManager getKeyManager() {
		return keyManager;
	}
	
	@NotNull
    public CrateManager getCrateManager() {
    	return this.crateManager;
    }
	
	@NotNull
	public MenuManager getMenuManager() {
		return menuManager;
	}
}
