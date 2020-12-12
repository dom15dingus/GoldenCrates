package su.nightexpress.goldencrates.manager.crate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.goldencrates.GoldenCrates;

public enum CrateEffectModel {

	HELIX,
	PULSAR,
	ERUPTION,
	BEACON,
	TORNADO,
	VORTEX,
	SIMPLE,
	NONE,
	;
	
	private static final GoldenCrates PLUGIN = GoldenCrates.getInstance();
    private static final Map<CrateEffectModel, Set<Crate>> CRATES = new HashMap<>();
    
    private MyEffect task;
    
    public static void start() {
    	for (CrateEffectModel model : CrateEffectModel.values()) {
    		if (model.getCrates().isEmpty()) continue;
    		if (model.task != null && !model.task.isCancelled()) continue;
    		
    		MyEffect effect = model.getEffect();
    		if (effect == null) continue;
    		
    		effect.start();
    	}
    }
    
    public static void shutdown() {
    	for (CrateEffectModel model : CrateEffectModel.values()) {
    		if (model.task != null) {
    			model.task.cancel();
    			model.task = null;
    		}
    	}
    	CRATES.clear();
    }
    
    public static void addCrate(@NotNull Crate crate) {
    	CrateEffect effect = crate.getBlockEffect();
    	if (effect.getModel() == NONE) return;
    	
    	effect.getModel().getCrates().add(crate);
    }
    
    public static void removeCrate(@NotNull Crate crate) {
    	CRATES.values().forEach(set -> set.removeIf(c -> c.getId().equalsIgnoreCase(crate.getId())));
    }
    
    @NotNull
    public Set<Crate> getCrates() {
    	Set<Crate> set = CRATES.computeIfAbsent(this, set2 -> new HashSet<>());
    	set.removeIf(crate -> crate == null);
    	return set;
    }
    
    @Nullable
    MyEffect getEffect() {
    	if (this.task != null) return this.task;
    	
        switch (this) {
            case ERUPTION: return (this.task = new Eruption());
            case PULSAR: return (this.task = new Pulsar());
            case HELIX: return (this.task = new Helix());
            case BEACON: return (this.task = new Beacon());
            case TORNADO: return (this.task = new Tornado());
            case VORTEX: return (this.task = new Vortex());
            case SIMPLE: return (this.task = new Simple());
            default: return (this.task = null);
        }
    }
    
    abstract static class MyEffect extends BukkitRunnable {
    	
    	protected CrateEffectModel model;
        protected int step;
        protected long interval;
        protected int duration;
        private int count;
        
        MyEffect(@NotNull CrateEffectModel model, long interval, int duration) {
        	this.model = model;
            this.step = 0;
            this.count = 0;
            this.interval = interval;
            this.duration = duration;
        }
        
        @Override
        public void run() {
        	if (this.step < 0) {
        		this.step++;
        	}
        	
        	// Do not play an effect while paused.
        	if (this.count++ % (int) this.interval() != 0) return;
        	if (this.step < 0) return;
        	
        	this.model.getCrates().forEach(crate -> {
        		CrateEffect effect = crate.getBlockEffect();
        		crate.getBlockLocations().forEach(loc -> {
        			this.doStep(LocUT.getCenter(loc, false), effect.getParticleName(), this.step);
        		});
        	});
        	
        	// Do a 0.5s pause when particle effect is finished.
        	if (this.step++ >= this.getDuration()) {
            	this.step = (int) -(20 / 2);
            	this.count = 0;
                return;
            }
        }

		public final void start() {
        	this.runTaskTimerAsynchronously(PLUGIN, 0L, 1L);
        }
        
        public final long interval() {
        	return this.interval;
        }
        
        public final int getDuration() {
        	return this.duration;
        }
        
        public abstract void doStep(@NotNull Location loc, @NotNull String particle, int step);
    }
    
    static class Simple extends MyEffect {

		Simple() {
			super(CrateEffectModel.SIMPLE, 2L, 1);
		}

		@Override
		public void doStep(@NotNull Location loc, @NotNull String particle, int step) {
			EffectUT.playEffect(loc.clone().add(0,0.5D,0), particle, 0.3f, 0.3f, 0.3f, 0.07f, 40);
		}
    	
    }
    
    static class Helix extends MyEffect {
    	
        Helix() {
            super(CrateEffectModel.HELIX, 1L, 24);
        }
        
        @Override
        public void doStep(@NotNull Location loc2, @NotNull String particle, int step) {
        	Location loc = loc2.clone().add(0, 0.05D, 0);
        	
            double n2 = 0.3141592653589793 * step;
            double n3 = step * 0.1 % 2.5;
            double n4 = 0.75;
            Location pointOnCircle = LocUT.getPointOnCircle(loc, true, n2, n4, n3);
            Location pointOnCircle2 = LocUT.getPointOnCircle(loc, true, n2 - 3.141592653589793, n4, n3);
            EffectUT.playEffect(pointOnCircle, particle, 0.0f, 0.0f, 0.0f, 0.0f, 1);
            EffectUT.playEffect(pointOnCircle2, particle, 0.0f, 0.0f, 0.0f, 0.0f, 1);
        }
    }
    
    static class Beacon extends MyEffect {
    	
        Beacon() {
            super(CrateEffectModel.BEACON, 3L, 40);
        }
        
        @Override
        public void doStep(@NotNull Location loc, @NotNull String particle, int step) {
            double n2 = 0.8975979010256552 * step;
            for (int i = step; i > Math.max(0, step - 25); --i) {
            	EffectUT.playEffect(LocUT.getPointOnCircle(loc, n2, 0.55, i * 0.75), particle, 0.0f, 0.15f, 0.0f, 0.0f, 4);
            }
        }
    }
    
    static class Eruption extends MyEffect {
    	
        private Vector[] shots;
        
        Eruption() {
            super(CrateEffectModel.ERUPTION, 2L, 30);
            this.shots = new Vector[4];
        }
        
        @Override
        public void doStep(@NotNull Location loc, @NotNull String particle, int step) {
        	if (step == 0) {
	            for (int i = 0; i < 4; ++i) {
	                this.shots[i] = new Vector(Rnd.nextDouble() * 2.0 - 1.0, Rnd.nextDouble() * 0.5 + 0.4, Rnd.nextDouble() * 2.0 - 1.0);
	            }
        	}
        	
        	double n2 = step * 0.3;
            for (Vector v : shots) {
                Location add = loc.add(v.clone().multiply(n2));
                EffectUT.playEffect(add, particle, 0.0f, 0.0f, 0.0f, 0.0f, 1);
            }
        }
    }
    
    static class Pulsar extends MyEffect {
    	
        Pulsar() {
            super(CrateEffectModel.PULSAR, 2L, 38);
        }
        
        @Override
        public void doStep(@NotNull Location loc2, @NotNull String particle, int step) {
        	Location loc = loc2.clone().add(0, -0.8D, 0);
            double n2 = (0.5 + step * 0.15) % 3.0;
            for (int n3 = 0; n3 < n2 * 10.0; ++n3) {
                double n4 = 6.283185307179586 / (n2 * 10.0) * n3;
                EffectUT.playEffect(LocUT.getPointOnCircle(loc.clone(), false, n4, n2, 1.0), particle, 0.1f, 0.1f, 0.1f, 0.0f, 2);
            }
        }
    }
    
    static class Tornado extends MyEffect {
    	
    	private double yOffset = 0.15D;
		private float tornadoHeight = 3.15F;
		private float maxTornadoRadius = 2.25F;
		private double distance = 0.375D;
    	
    	Tornado() {
    		super(CrateEffectModel.TORNADO, 4L, 7);
		}
		
		@Override
		public void doStep(@NotNull Location loc2, @NotNull String particle, int step) {
			Location loc = loc2.clone().add(0.0D, 0.5D, 0.0D);
			double offset = 0.25D * (this.maxTornadoRadius * (2.35D / this.tornadoHeight));
			double vertical = this.tornadoHeight - this.distance * step;
			  
			double radius = offset * vertical;
			if (radius > this.maxTornadoRadius) {
				radius = this.maxTornadoRadius;
			}
			for (Vector vector : this.createCircle(vertical, radius)) {
				EffectUT.playEffect(loc.add(vector), particle, 0.1f, 0.1f, 0.1f, 0.0f, 3);
				loc.subtract(vector);
			}
			loc.subtract(0.0D, this.yOffset, 0.0D);
		}
		  
		private List<Vector> createCircle(double vertical, double radius) {
			double amount = radius * 64.0D;
			double d2 = 6.283185307179586D / amount;
			List<Vector> vectors = new ArrayList<>();
			for (int i = 0; i < amount; i++) {
				double d3 = i * d2;
				double cos = radius * Math.cos(d3);
				double sin = radius * Math.sin(d3);
				Vector vector = new Vector(cos, vertical, sin);
				vectors.add(vector);
			}
			return vectors;
		}
    }
    
    static class Vortex extends MyEffect {

    	private int strands = 2;
		private int particles = 170;
		private float radius = 1.5F;
		private float curve = 2.0F;
		private double rotation = 0.7853981633974483D;
    	
		Vortex() {
			super(CrateEffectModel.VORTEX, 1L, 170);
		}
		
		@Override
		public void doStep(@NotNull Location loc, @NotNull String particle, int step) {
			for (int boost = 0; boost < 5; boost++) {
		        for (int strand = 1; strand <= this.strands; ++strand) {
		            float progress = step / (float) this.particles;
		            double point = this.curve * progress * 2.0f * Math.PI / this.strands + 6.283185307179586 * strand / this.strands + this.rotation;
		            double addX = Math.cos(point) * progress * this.radius;
		            double addZ = Math.sin(point) * progress * this.radius;
		            double addY = 3.5D - 0.02 * step;
		            Location location = loc.clone().add(addX, addY, addZ);
		            EffectUT.playEffect(location, particle, 0.1f, 0.1f, 0.1f, 0.0f, 1);
		        }
		        step++;
			}
			this.step = step;
		}
    }
}
