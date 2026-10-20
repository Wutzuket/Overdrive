package de.wutzuket.overdrive.integration.top;

import mcjty.theoneprobe.api.ITheOneProbe;
import java.util.function.Function;

public class TOPRegistration implements Function<ITheOneProbe, Void> {
    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(new VacuumPumpProbeInfoProvider());
        probe.registerProvider(new AntimatterPipeProbeInfoProvider());
        return null;
    }
}
