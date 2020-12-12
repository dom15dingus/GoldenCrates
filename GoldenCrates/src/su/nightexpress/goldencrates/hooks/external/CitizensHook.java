package su.nightexpress.goldencrates.hooks.external;

import org.jetbrains.annotations.NotNull;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import su.nexmedia.engine.hooks.external.citizens.CitizensListener;
import su.nightexpress.goldencrates.api.GoldenCratesAPI;
import su.nightexpress.goldencrates.manager.crate.Crate;

public class CitizensHook implements CitizensListener {

	@Override
	public void onLeftClick(NPCLeftClickEvent e) {
		this.process(e);
	}

	@Override
	public void onRightClick(NPCRightClickEvent e) {
		this.process(e);
	}
	
	private void process(@NotNull NPCClickEvent e) {
		int id = e.getNPC().getId();
		Crate crate = GoldenCratesAPI.getCrateManager().getCrateByNPC(id);
		if (crate == null) return;
		
		if (e instanceof NPCLeftClickEvent) {
			crate.openPreview(e.getClicker());
		}
		else {
			GoldenCratesAPI.getCrateManager().openCrate(e.getClicker(), crate, null, null);
		}
	}
}
