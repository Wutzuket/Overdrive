package de.wutzuket.overdrive.integration.top;

import net.neoforged.fml.InterModComms;

public class TOPCompatibility {
    public static void register() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPRegistration::new);
    }
}
