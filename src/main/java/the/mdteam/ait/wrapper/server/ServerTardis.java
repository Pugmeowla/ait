package the.mdteam.ait.wrapper.server;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.client.renderers.exteriors.ExteriorEnum;
import mdteam.ait.data.AbsoluteBlockPos;
import the.mdteam.ait.*;

import java.util.UUID;


public class ServerTardis extends Tardis {

    public ServerTardis(UUID uuid, AbsoluteBlockPos.Directed pos, TardisDesktopSchema schema, ExteriorEnum exteriorType, ConsoleEnum consoleType, boolean locked) {
        super(uuid, tardis -> new ServerTardisTravel(tardis, pos), tardis -> new ServerTardisDesktop(tardis, schema), tardis -> new ServerTardisExterior(tardis, exteriorType), tardis -> new ServerTardisConsole(tardis, consoleType), locked);
    }

    @Override
    public void setDesktop(TardisDesktop desktop) {
        super.setDesktop(desktop);
        this.sync();
    }

    public void sync() {
        ServerTardisManager.getInstance().sendToSubscribers(this);
    }
}