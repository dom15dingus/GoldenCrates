package su.nightexpress.goldencrates.manager.crate;

import org.jetbrains.annotations.NotNull;

public class CrateEffect {

	private CrateEffectModel model;
	private String particle;
	
	public CrateEffect(
			@NotNull CrateEffectModel model,
			@NotNull String particle
			) {
		this.setModel(model);
		this.setParticleName(particle);
	}
	
	@NotNull
	public CrateEffectModel getModel() {
		return this.model;
	}
	
	public void setModel(@NotNull CrateEffectModel model) {
		this.model = model;
	}
	
	@NotNull
	public String getParticleName() {
		return this.particle;
	}
	
	public void setParticleName(@NotNull String particle) {
		this.particle = particle;
	}
}
