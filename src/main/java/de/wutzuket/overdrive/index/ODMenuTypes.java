package de.wutzuket.overdrive.index;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.menu.AcceleratorControllerMenu;
import de.wutzuket.overdrive.menu.AcceleratorOutputMenu;
import de.wutzuket.overdrive.menu.ADControllerMenu;
import de.wutzuket.overdrive.menu.ADInputPortMenu;
import de.wutzuket.overdrive.menu.ADOutputPortMenu;
import de.wutzuket.overdrive.menu.ELENAControllerMenu;
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



    public static final Supplier<MenuType<de.wutzuket.overdrive.menu.WolframTargetMenu>> WOLFRAM_TARGET_MENU =
            MENUS.register("wolfram_target", () -> IMenuTypeExtension.create(de.wutzuket.overdrive.menu.WolframTargetMenu::new));

    public static final Supplier<MenuType<de.wutzuket.overdrive.menu.ElectronGunMenu>> ELECTRON_GUN_MENU =
            MENUS.register("electron_gun", () -> IMenuTypeExtension.create(de.wutzuket.overdrive.menu.ElectronGunMenu::new));

    public static final Supplier<MenuType<ADControllerMenu>> AD_CONTROLLER_MENU =
            MENUS.register("ad_controller", () -> IMenuTypeExtension.create(ADControllerMenu::new));

    public static final Supplier<MenuType<ELENAControllerMenu>> ELENA_CONTROLLER_MENU =
            MENUS.register("elena_controller", () -> IMenuTypeExtension.create(ELENAControllerMenu::new));

    public static final Supplier<MenuType<ADInputPortMenu>> AD_INPUT_PORT_MENU =
            MENUS.register("ad_input_port", () -> IMenuTypeExtension.create(ADInputPortMenu::new));

    public static final Supplier<MenuType<ADOutputPortMenu>> AD_OUTPUT_PORT_MENU =
            MENUS.register("ad_output_port", () -> IMenuTypeExtension.create(ADOutputPortMenu::new));

    public static final Supplier<MenuType<de.wutzuket.overdrive.menu.ELENAInputPortMenu>> ELENA_INPUT_PORT_MENU =
            MENUS.register("elena_input_port", () -> IMenuTypeExtension.create(de.wutzuket.overdrive.menu.ELENAInputPortMenu::new));

    public static final Supplier<MenuType<de.wutzuket.overdrive.menu.ELENAOutputPortMenu>> ELENA_OUTPUT_PORT_MENU =
            MENUS.register("elena_output_port", () -> IMenuTypeExtension.create(de.wutzuket.overdrive.menu.ELENAOutputPortMenu::new));

    public static final Supplier<MenuType<de.wutzuket.overdrive.menu.PositronVacuumOutputPortMenu>> POSITRON_VACUUM_OUTPUT_PORT_MENU =
            MENUS.register("positron_vacuum_output_port", () -> IMenuTypeExtension.create(de.wutzuket.overdrive.menu.PositronVacuumOutputPortMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
