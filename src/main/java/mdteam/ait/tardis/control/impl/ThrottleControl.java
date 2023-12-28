package mdteam.ait.tardis.control.impl;

import mdteam.ait.core.AITSounds;
import mdteam.ait.tardis.control.Control;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import net.minecraft.sound.SoundEvent;

public class ThrottleControl extends Control {
    public ThrottleControl() {
        super("throttle");
    }

    @Override
    public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {
        // fixme this is not right, but its okay for temporary. also see remoteitem where this is done again
        TardisTravel travel = tardis.getTravel();

        if (travel.getState() == TardisTravel.State.LANDED) {
            if (tardis.getFuel() <= 0) {
                return false;
            }
            travel.dematerialise(PropertiesHandler.willAutoPilot(tardis.getHandlers().getProperties()));
        } else if (travel.getState() == TardisTravel.State.FLIGHT) {
            travel.materialise();
        }

        return true;
    }

    @Override
    public SoundEvent getSound() {
        return AITSounds.DEMAT_LEVER_PULL;
    }
}
