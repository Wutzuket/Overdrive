package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.menu.AcceleratorControllerMenu;
import de.wutzuket.overdrive.menu.AcceleratorOutputMenu;
import de.wutzuket.overdrive.menu.IridiumTargetMenu;
import de.wutzuket.overdrive.menu.VacuumPumpMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ODMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Overdrive.MODID);

    public static final Supplier<MenuType<VacuumPumpMenu>> VACUUM_PUMP_MENU =
            MENUS.register("vacuum_pump", () -> IMenuTypeExtension.create(VacuumPumpMenu::new));

    public static final Supplier<MenuType<IridiumTargetMenu>> IRIDIUM_TARGET_MENU =
            MENUS.register("iridium_target", () -> IMenuTypeExtension.create(IridiumTargetMenu::new));

    public static final Supplier<MenuType<AcceleratorControllerMenu>> ACCELERATOR_CONTROLLER_MENU =
            MENUS.register("accelerator_controller", () -> IMenuTypeExtension.create(AcceleratorControllerMenu::new));

    public static final Supplier<MenuType<AcceleratorOutputMenu>> ACCELERATOR_OUTPUT_MENU =
            MENUS.register("accelerator_output", () -> IMenuTypeExtension.create(AcceleratorOutputMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
