package mdteam.ait.tardis;

import mdteam.ait.client.renderers.consoles.ConsoleEnum;
import mdteam.ait.tardis.control.ControlTypes;

// me gustaria hacer que estas clases extiendan TardisLink porque creo que es mejor
// pero puede que no sea necesario ya que estos se actualizan mediante el metodo sendToSubscribers
// que piensas?
public class TardisConsole {

    @Exclude
    protected final Tardis tardis;
    private ConsoleEnum console;
    private ControlTypes[] controlTypes;

    public TardisConsole(Tardis tardis, ConsoleEnum console, ControlTypes[] controlTypes) {
        this.tardis = tardis;
        this.console = console;
        this.controlTypes = controlTypes;
    }

    public ControlTypes[] getControlTypes() {
        return controlTypes;
    }

    public void setControlTypes(ControlTypes[] controlTypes) {
        this.controlTypes = controlTypes;
    }

    public ConsoleEnum getType() {
        return console;
    }

    public void setType(ConsoleEnum console) {
        this.console = console;
    }
}
