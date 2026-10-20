package de.wutzuket.overdrive.antimatter;

import org.jetbrains.annotations.Nullable;

/**
 * Combines an input and an output tank into a single handler. Inserting fills the input tank,
 * extracting takes from the output tank. It is the antimatter counterpart of the
 * {@code InOutFluidHandler}.
 */
public class InOutAntimatterHandler implements IAntimatterHandler {

    private final AntimatterTank inputTank;
    private final AntimatterTank outputTank;

    public InOutAntimatterHandler(AntimatterTank inputTank, AntimatterTank outputTank) {
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @Nullable Antimatter getAntimatterInTank(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getAntimatterInTank(0);
            case 1 -> outputTank.getAntimatterInTank(0);
            default -> null;
        };
    }

    @Override
    public int getAmount(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getAmount(0);
            case 1 -> outputTank.getAmount(0);
            default -> 0;
        };
    }

    @Override
    public int getCapacity(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getCapacity(0);
            case 1 -> outputTank.getCapacity(0);
            default -> 0;
        };
    }

    @Override
    public int insertAntimatter(int tank, @Nullable Antimatter type, int amount, boolean simulate) {
        if (tank != 0) {
            return 0;
        }
        return inputTank.insertAntimatter(0, type, amount, simulate);
    }

    @Override
    public int extractAntimatter(int tank, int amount, boolean simulate) {
        if (tank != 1) {
            return 0;
        }
        return outputTank.extractAntimatter(0, amount, simulate);
    }
}
