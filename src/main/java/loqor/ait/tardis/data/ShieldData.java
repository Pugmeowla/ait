package loqor.ait.tardis.data;

import loqor.ait.core.entities.BaseControlEntity;
import loqor.ait.core.entities.FallingTardisEntity;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

public class ShieldData extends TardisLink {
	public static String IS_SHIELDED = "is_shielded";
	public static String IS_VISUALLY_SHIELDED = "is_visually_shielded";

	public ShieldData(Tardis tardis) {
		super(tardis, "shield");
	}

	public void enable() {
		if (this.findTardis().isEmpty()) return;

		PropertiesHandler.set(this.findTardis().get(), IS_SHIELDED, true);
	}

	public void disable() {
		if (this.findTardis().isEmpty()) return;

		PropertiesHandler.set(this.findTardis().get(), IS_SHIELDED, false);
	}

	public void enableVisuals() {
		if (this.findTardis().isEmpty()) return;

		PropertiesHandler.set(this.findTardis().get(), IS_VISUALLY_SHIELDED, true);
	}

	public void disableVisuals() {
		if (this.findTardis().isEmpty()) return;

		PropertiesHandler.set(this.findTardis().get(), IS_VISUALLY_SHIELDED, false);
	}

	public void disableAll() {
		this.disableVisuals();
		this.disable();
	}

	public boolean areShieldsActive() {
		if (this.findTardis().isEmpty()) return false;

		return PropertiesHandler.getBool(this.findTardis().get().getHandlers().getProperties(), IS_SHIELDED);
	}

	public boolean areVisualShieldsActive() {
		if (this.findTardis().isEmpty()) return false;

		return PropertiesHandler.getBool(this.findTardis().get().getHandlers().getProperties(), IS_VISUALLY_SHIELDED);
	}

	@Override
	public void tick(MinecraftServer server) {
		super.tick(server);

		if (this.findTardis().isEmpty() || this.areShieldsActive() && !this.findTardis().get().hasPower()) {
			this.disableAll();
			return;
		}

		if (this.findTardis().get().getExterior().getExteriorPos() == null) return;

		if (!this.areShieldsActive()) return;

		Tardis tardis = this.findTardis().get();

		tardis.removeFuel(2 * (tardis.tardisHammerAnnoyance + 1)); // idle drain of 2 fuel per tick
		World world = tardis.getExterior().getExteriorPos().getWorld();
		world.getOtherEntities(null,
						new Box(getExteriorPos()).expand(4f),
						EntityPredicates.EXCEPT_SPECTATOR)
				.stream()
				.filter(entity -> !(entity instanceof BaseControlEntity)) // Exclude control entities
				.filter(entity -> !(entity instanceof ServerPlayerEntity player && player.isSpectator())) // Exclude players in spectator
				.filter(entity -> !(entity instanceof FallingTardisEntity falling && falling.getTardis() == tardis))
				.filter(entity -> !(entity instanceof PlayerEntity && Objects.equals(((PlayerEntity) entity).getOffHandStack().getOrCreateNbt().getString("tardis"), tardis.getUuid().toString()))) // Exclude players
				.forEach(entity -> {
					if(entity instanceof ServerPlayerEntity && entity.isSubmergedInWater()) {
						((ServerPlayerEntity) entity).addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 20, 3, true, false, false));
					}
					if(this.areVisualShieldsActive()) {
						if (entity.squaredDistanceTo(this.getExteriorPos().toCenterPos()) <= 6f) {
							Vec3d motion = entity.getBlockPos().toCenterPos().subtract(this.getExteriorPos().toCenterPos()).normalize().multiply(0.1f);
							entity.setVelocity(entity.getVelocity().add(motion));
							entity.velocityDirty = true;
							entity.velocityModified = true;
						}
					}
				});
	}
}
