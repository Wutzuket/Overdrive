package de.wutzuket.overdrive.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

/**
 * Kombinierter Fluid-Zugriff fuer Controller: Befuellen geht in den Input-Tank,
 * Entnehmen kommt aus dem Output-Tank (Pipes sehen beide Tanks).
 */
public class InOutFluidHandler implements IFluidHandler {
    private final FluidTank input;
    private final FluidTank output;

    public InOutFluidHandler(FluidTank input, FluidTank output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? input.getFluid() : output.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? input.getCapacity() : output.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 ? input.isFluidValid(stack) : output.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return input.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        FluidStack contained = output.getFluid();
        if (contained.isEmpty() || !contained.is(resource.getFluid())) return FluidStack.EMPTY;
        return output.drain(Math.min(resource.getAmount(), contained.getAmount()), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return output.drain(maxDrain, action);
    }
}
