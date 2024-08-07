package loqor.ait.tardis.link.v2;

import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.link.v2.block.InteriorLinkableBlockEntity;

import java.util.UUID;

public interface Linkable {

    void link(Tardis tardis);

    void link(UUID id);

    TardisRef tardis();

    /**
     * @implNote This method is called when the TardsRef instance gets created.
     * This means that the ref is no longer null BUT the TARDIS instance still could be missing.
     * Primarily this is true for {@link InteriorLinkableBlockEntity}s
     */
    default void onLinked() { }
}
