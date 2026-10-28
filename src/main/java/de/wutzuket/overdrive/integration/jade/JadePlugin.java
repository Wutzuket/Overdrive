package de.wutzuket.overdrive.integration.jade;

import de.wutzuket.overdrive.block.accelerator.AcceleratorVacuumInputPortBlock;
import de.wutzuket.overdrive.block.ad.ADVacuumPortBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationControllerBlock;
import de.wutzuket.overdrive.block.annihilation.AnnihilationVacuumPortBlock;
import de.wutzuket.overdrive.block.annihilation.CoolantInputPortBlock;
import de.wutzuket.overdrive.block.annihilation.SteamOutputPortBlock;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlock;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import de.wutzuket.overdrive.block.antimatter.AntimatterTankBlock;
import de.wutzuket.overdrive.block.elena.ELENAVacuumPortBlock;
import de.wutzuket.overdrive.block.tungsten.PositronVacuumOutputPortBlock;
import de.wutzuket.overdrive.block.vacuum.EnergyInputPortBlock;
import de.wutzuket.overdrive.block.vacuum.PumpCasingBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumOutputPortBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPipeBlock;
import de.wutzuket.overdrive.block.vacuum.VacuumPumpBlock;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.List;

/**
 * Jade integration for the vacuum and antimatter systems. It mirrors the information that is
 * already shown by The One Probe, but uses Jade's client side component providers.
 * <p>
 * The class is picked up by Jade's {@link WailaPlugin} annotation scanner, so it is only loaded
 * when Jade is actually installed.
 */
@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    private static final List<Class<? extends Block>> VACUUM_BLOCKS = List.of(
            VacuumPumpBlock.class,
            PumpCasingBlock.class,
            EnergyInputPortBlock.class,
            VacuumOutputPortBlock.class,
            VacuumPipeBlock.class,
            AcceleratorVacuumInputPortBlock.class,
            ADVacuumPortBlock.class,
            ELENAVacuumPortBlock.class,
            PositronVacuumOutputPortBlock.class,
            AnnihilationVacuumPortBlock.class,
            AnnihilationControllerBlock.class,
            CoolantInputPortBlock.class,
            SteamOutputPortBlock.class);

    @Override
    public void register(IWailaCommonRegistration registration) {
        // The antimatter network and the vacuum level only exist on the server, therefore the
        // values have to be sent to the client with Jade's server data.
        registration.registerBlockDataProvider(AntimatterServerDataProvider.INSTANCE, AntimatterPipeBlockEntity.class);
        for (Class<? extends Block> block : VACUUM_BLOCKS) {
            registration.registerBlockDataProvider(VacuumServerDataProvider.INSTANCE, block);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        for (Class<? extends Block> block : VACUUM_BLOCKS) {
            registration.registerBlockComponent(VacuumComponentProvider.INSTANCE, block);
        }

        registration.registerBlockComponent(AntimatterComponentProvider.INSTANCE, AntimatterPipeBlock.class);
        registration.registerBlockComponent(AntimatterComponentProvider.INSTANCE, AntimatterTankBlock.class);
    }
}
