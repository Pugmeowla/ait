package loqor.ait.registry.unlockable;

import loqor.ait.registry.datapack.Identifiable;
import loqor.ait.tardis.data.loyalty.Loyalty;

public interface Unlockable extends Identifiable {
    UnlockType unlockType();
    Loyalty getRequirement();

    enum UnlockType {
        EXTERIOR,
        CONSOLE,
        SONIC,
        DESKTOP
    }
}
