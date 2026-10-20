package de.wutzuket.overdrive.gametest;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.antimatter.ODAntimatters;
import de.wutzuket.overdrive.antimatter.PipeSideMode;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlock;
import de.wutzuket.overdrive.block.antimatter.AntimatterPipeBlockEntity;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.block.antimatter.AntimatterTankEntity;
import de.wutzuket.overdrive.block.antimatter.PipeConnection;
import de.wutzuket.overdrive.block.antimatter.PipeShape;
import de.wutzuket.overdrive.fluid.ODFluids;
import de.wutzuket.overdrive.index.ODBlocks;
import de.wutzuket.overdrive.index.ODItems;
import de.wutzuket.overdrive.item.AntimatterCellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Game tests for the custom antimatter transport and storage system.
 */
@GameTestHolder(Overdrive.MODID)
@PrefixGameTestTemplate(false)
public class AntimatterGameTests {

    /**
     * A tank that is connected to a second tank by a row of pipes has to transport the antimatter
     * through the pipe network.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 400)
    public static void pipeNetworkTransportsAntimatter(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(3, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(4, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            // the first pipe takes the antimatter out of the source tank
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity first) {
                first.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
            // the last pipe pushes the antimatter into the target tank
            if (helper.getBlockEntity(new BlockPos(3, 1, 0)) instanceof AntimatterPipeBlockEntity last) {
                last.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
        });

        AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
        source.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 1000, false);

        helper.runAfterDelay(120, () -> {
            AntimatterTankEntity target = helper.getBlockEntity(new BlockPos(4, 1, 0));
            if (target.getAmount(0) <= 0) {
                helper.fail("No antimatter was transported through the pipe network");
                return;
            }
            if (target.getAntimatterInTank(0) == null) {
                helper.fail("The target tank does not know which antimatter it holds");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * A disabled side must block the transport and has to be rendered with a cap.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 400)
    public static void disabledSideBlocksTransport(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(3, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(10, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity first) {
                // the first pipe takes the antimatter out of the tank
                first.setSideMode(Direction.WEST, PipeSideMode.PULL);
                // but the side to the second pipe is blocked
                first.setSideMode(Direction.EAST, PipeSideMode.DISABLED);
                first.refreshNetwork();
            }
            if (helper.getBlockEntity(new BlockPos(2, 1, 0)) instanceof AntimatterPipeBlockEntity second) {
                second.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
            AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
            source.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 1000, false);
        });

        helper.runAfterDelay(120, () -> {
            AntimatterTankEntity target = helper.getBlockEntity(new BlockPos(3, 1, 0));
            if (target.getAmount(0) > 0) {
                helper.fail("Antimatter was transported through a disabled side");
                return;
            }
            BlockState state = helper.getBlockState(new BlockPos(1, 1, 0));
            if (state.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.EAST)) != PipeConnection.DISABLED) {
                helper.fail("The disabled side is not rendered with a cap");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * A straight pipe is rendered as one continuous tube, every other combination uses the closed
     * center cube.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 200)
    public static void straightPipeUsesTubeWithoutCenter(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 1, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(3, 1, 1, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());

        helper.runAfterDelay(10, () -> {
            // the middle pipe is connected on both sides and therefore has to be rendered as one
            // continuous tube without the center cube
            BlockState state = helper.getBlockState(new BlockPos(2, 1, 1));
            if (state.getValue(AntimatterPipeBlock.SHAPE) != PipeShape.STRAIGHT_X) {
                helper.fail("A pipe with two opposite connections is not a straight pipe");
                return;
            }
            if (state.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.EAST)) != PipeConnection.OPEN) {
                helper.fail("The pipe did not connect to its neighbour");
                return;
            }
            if (state.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.WEST)) != PipeConnection.OPEN) {
                helper.fail("The pipe did not connect to its neighbour");
                return;
            }
            if (state.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.NORTH)) != PipeConnection.NONE) {
                helper.fail("The pipe connected to a block that is not a pipe");
                return;
            }

            // the outer pipes are dead ends and therefore use the center cube
            BlockState deadEnd = helper.getBlockState(new BlockPos(1, 1, 1));
            if (deadEnd.getValue(AntimatterPipeBlock.SHAPE) != PipeShape.CENTER) {
                helper.fail("A dead end pipe does not use the center cube");
                return;
            }

            // adding a third connection has to switch the middle pipe back to the center cube
            helper.setBlock(2, 2, 1, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
            helper.runAfterDelay(5, () -> {
                BlockState corner = helper.getBlockState(new BlockPos(2, 1, 1));
                if (corner.getValue(AntimatterPipeBlock.SHAPE) != PipeShape.CENTER) {
                    helper.fail("A pipe with three connections does not use the center cube");
                    return;
                }
                helper.succeed();
            });
        });
    }

    /**
     * An empty cell that is put into the input slot of a tank has to be filled with the antimatter
     * of the tank and moved into the output slot. A full cell has to be emptied into the tank.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 400)
    public static void tankFillsAndEmptiesCells(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        AntimatterTankEntity tank = helper.getBlockEntity(new BlockPos(1, 1, 1));
        tank.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 500, false);
        tank.getItemHandler().setStackInSlot(AntimatterTankEntity.INPUT_SLOT,
                new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get()));

        helper.runAfterDelay(20, () -> {
            ItemStack output = tank.getOutputStack();
            if (output.isEmpty()) {
                helper.fail("The filled cell did not appear in the output slot");
                return;
            }
            if (!(output.getItem() instanceof AntimatterCellItem cell)) {
                helper.fail("The output slot does not contain a cell");
                return;
            }
            if (cell.getAmount(output) <= 0) {
                helper.fail("The cell was not filled");
                return;
            }

            // put the filled cell back into the input slot, it has to be emptied into the tank
            tank.getItemHandler().setStackInSlot(AntimatterTankEntity.OUTPUT_SLOT, ItemStack.EMPTY);
            tank.getItemHandler().setStackInSlot(AntimatterTankEntity.INPUT_SLOT, output);

            helper.runAfterDelay(20, () -> {
                ItemStack emptied = tank.getOutputStack();
                if (emptied.isEmpty()) {
                    helper.fail("The empty cell did not appear in the output slot");
                    return;
                }
                if (((AntimatterCellItem) emptied.getItem()).getAmount(emptied) != 0) {
                    helper.fail("The cell was not emptied into the tank");
                    return;
                }
                helper.succeed();
            });
        });
    }

    /**
     * The output port of the particle accelerator has to provide the antimatter that it produced
     * so that it can be transported with the antimatter pipe network.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 400)
    public static void machinePortProvidesAntimatter(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ACCELERATOR_OUTPUT.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                // the pipe takes the antimatter out of the machine and feeds it into the tank
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
                pipe.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
        });

        helper.runAfterDelay(10, () -> {
            if (!(helper.getBlockEntity(new BlockPos(0, 1, 0))
                    instanceof de.wutzuket.overdrive.block.accelerator.AcceleratorOutputEntity output)) {
                helper.fail("The accelerator output has no block entity");
                return;
            }
            // the accelerator fills its output tank with antiprotons
            output.getAntimatterTank().insertAntimatter(0, ODAntimatters.ANTIPROTONS, 2000, false);
        });

        helper.runAfterDelay(120, () -> {
            AntimatterTankEntity target = helper.getBlockEntity(new BlockPos(2, 1, 0));
            if (target.getAmount(0) <= 0) {
                helper.fail("No antimatter was taken from the machine port");
                return;
            }
            if (!ODAntimatters.ANTIPROTONS.equals(target.getAntimatterInTank(0))) {
                helper.fail("The tank does not hold antiprotons");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * When the transfer is done, the source tank has to be empty, the target tank has to hold the
     * whole amount and no antimatter may be left over in the pipes.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 600)
    public static void nothingStaysInThePipes(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(3, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity first) {
                first.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
            if (helper.getBlockEntity(new BlockPos(2, 1, 0)) instanceof AntimatterPipeBlockEntity second) {
                second.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
        });

        AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
        source.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 1000, false);

        helper.runAfterDelay(400, () -> {
            AntimatterTankEntity target = helper.getBlockEntity(new BlockPos(3, 1, 0));
            // ein kleiner Rest bleibt immer in der Leitung, das ist der Teil, der gerade unterwegs
            // ist
            if (target.getAmount(0) < 990) {
                helper.fail("The target tank holds " + target.getAmount(0) + " instead of 1000");
                return;
            }
            if (source.getAmount(0) != 0) {
                helper.fail("The source tank still holds " + source.getAmount(0) + " u");
                return;
            }
            for (int x = 1; x <= 2; x++) {
                if (helper.getBlockEntity(new BlockPos(x, 1, 0)) instanceof AntimatterPipeBlockEntity pipe
                        && pipe.getLocalAmount() > 10) {
                    helper.fail("The pipe at " + x + " still holds " + pipe.getLocalAmount() + " u");
                    return;
                }
            }
            helper.succeed();
        });
    }

    /**
     * A pulling side always takes the antimatter out of the machine. If nothing in the network can
     * take it again, the antimatter waits in the pipes.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 400)
    public static void pullTakesAntimatterIntoThePipes(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
        });

        AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
        source.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 1000, false);

        helper.runAfterDelay(200, () -> {
            if (source.getAmount(0) != 0) {
                helper.fail("The tank still holds " + source.getAmount(0) + " u");
                return;
            }
            if (!(helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe)) {
                helper.fail("The pipe has no block entity");
                return;
            }
            if (pipe.getLocalAmount() != 1000) {
                helper.fail("The pipe holds " + pipe.getLocalAmount() + " u instead of 1000");
                return;
            }
            helper.succeed();
        });
    }

    
    
    
    
    /**
     * A creative tank always has a full amount of antimatter and never runs out.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 200)
    public static void creativeTankNeverRunsOut(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(10, () -> {
            if (!(helper.getBlockEntity(new BlockPos(0, 1, 0)) instanceof AntimatterTankEntity tank)) {
                helper.fail("The tank has no block entity");
                return;
            }
            tank.setCreative(true, ODAntimatters.ANTIPROTONS);
        });

        helper.runAfterDelay(20, () -> {
            AntimatterTankEntity tank = helper.getBlockEntity(new BlockPos(0, 1, 0));
            if (!tank.isCreative()) {
                helper.fail("The tank is not creative");
                return;
            }
            if (tank.getAmount(0) != tank.getCapacity(0)) {
                helper.fail("The creative tank is not full");
                return;
            }
            // der Inhalt darf nicht zurueckgehen
            int taken = tank.extractAntimatter(0, 1000, false);
            if (taken != 1000) {
                helper.fail("The creative tank did not give anything away");
                return;
            }
            if (tank.getAmount(0) != tank.getCapacity(0)) {
                helper.fail("The creative tank lost antimatter");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * The cell docking port takes a cell and puts the antimatter into its tank.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 200)
    public static void cellDockingPortTakesCells(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.CELL_DOCKING_PORT.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (!(helper.getBlockEntity(new BlockPos(0, 1, 0))
                    instanceof de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity port)) {
                helper.fail("The port has no block entity");
                return;
            }
            net.minecraft.world.item.ItemStack cell = new net.minecraft.world.item.ItemStack(
                    de.wutzuket.overdrive.index.ODItems.getCellFor(ODAntimatters.POSITRONS));
            ((de.wutzuket.overdrive.item.AntimatterCellItem) cell.getItem())
                    .setContent(cell, ODAntimatters.POSITRONS, 1000);
            port.getItemHandler().setStackInSlot(0, cell);
        });

        helper.runAfterDelay(30, () -> {
            if (!(helper.getBlockEntity(new BlockPos(0, 1, 0))
                    instanceof de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity port)) {
                helper.fail("The port has no block entity");
                return;
            }
            if (port.getTank().getAmount(0) != 1000) {
                helper.fail("The port holds " + port.getTank().getAmount(0) + " u instead of 1000");
                return;
            }
            if (port.getTank().getAntimatterInTank(0) == null
                    || !port.getTank().getAntimatterInTank(0).equals(ODAntimatters.POSITRONS)) {
                helper.fail("The port does not hold positrons");
                return;
            }
            if (!port.hasPositronFluid()) {
                helper.fail("The port does not recognize the positrons");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * Measures the throughput of a pipe with a creative tank as the source, so the source is never
     * the limit. The test runs with a line of three pipes.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 300)
    public static void pipeThroughputWithCreativeSource(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().defaultBlockState());
        helper.setBlock(3, 1, 0, ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().defaultBlockState());
        helper.setBlock(4, 1, 0, ODBlocks.CELL_DOCKING_PORT.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
            if (helper.getBlockEntity(new BlockPos(3, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
            AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
            source.setCreative(true, ODAntimatters.ANTIPROTONS);
        });

        helper.runAfterDelay(35, () -> {
            int amount = 0;
            if (helper.getBlockEntity(new BlockPos(4, 1, 0))
                    instanceof de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity port) {
                amount = port.getTank().getAmount(0);
            }
            helper.succeed();
            System.out.println("### Ultimate-Pipes (3 in einer Reihe, creative Quelle): " + amount
                    + " u in 30 Ticks = " + (amount / 30) + " mB/t");
            // die Rohre muessen die Rate auch wirklich schaffen
            int expected = ODConfig.ANTIMATTER_PIPE_ULTIMATE_RATE.get() * 30;
            if (amount < expected * 8 / 10) {
                helper.fail("Die Rohre schaffen nur " + amount + " statt " + expected + " u");
            }
        });
    }

    /**
     * Shows how the antimatter is spread over a long line of pipes and how much arrives at the
     * end of it.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 300)
    public static void longLineDistribution(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        for (int x = 1; x <= 8; x++) {
            helper.setBlock(x, 1, 0, ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().defaultBlockState());
        }
        helper.setBlock(9, 1, 0, ODBlocks.CELL_DOCKING_PORT.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
            if (helper.getBlockEntity(new BlockPos(8, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
            AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
            source.setCreative(true, ODAntimatters.ANTIPROTONS);
        });

        helper.runAfterDelay(65, () -> {
            StringBuilder line = new StringBuilder();
            for (int x = 1; x <= 8; x++) {
                if (helper.getBlockEntity(new BlockPos(x, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                    line.append(pipe.getLocalAmount()).append(" ");
                }
            }
            int arrived = 0;
            if (helper.getBlockEntity(new BlockPos(9, 1, 0))
                    instanceof de.wutzuket.overdrive.block.annihilation.CellDockingPortEntity port) {
                arrived = port.getTank().getAmount(0);
            }
            helper.succeed();
            System.out.println("### Lange Linie: Rohre = [" + line.toString().trim() + "]  angekommen = "
                    + arrived + " u in 60 Ticks = " + (arrived / 60) + " mB/t");
        });
    }

    /**
     * The configuration of a side is part of the block state, because the model of the pipe shows
     * if a side pulls or pushes the antimatter.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 200)
    public static void sideModeIsPartOfTheBlockState(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_PIPE_BASIC.get().defaultBlockState());
        helper.setBlock(3, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
            }
            if (helper.getBlockEntity(new BlockPos(2, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
        });

        helper.runAfterDelay(20, () -> {
            BlockState pulling = helper.getBlockState(new BlockPos(1, 1, 0));
            if (pulling.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.WEST)) != PipeConnection.PULL) {
                helper.fail("The pulling side is not in the block state");
                return;
            }
            if (pulling.getValue(AntimatterPipeBlock.SHAPE) != PipeShape.CENTER) {
                helper.fail("A pipe with a pulling side does not use the center model");
                return;
            }
            BlockState pushing = helper.getBlockState(new BlockPos(2, 1, 0));
            if (pushing.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.EAST)) != PipeConnection.PUSH) {
                helper.fail("The pushing side is not in the block state");
                return;
            }
            // the side between the two pipes is a normal connection
            if (pushing.getValue(AntimatterPipeBlock.CONNECTIONS.get(Direction.WEST)) != PipeConnection.OPEN) {
                helper.fail("The normal side is not in the block state");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * Several empty cells in the input slot must be processed one after the other. Only one cell
     * may be filled per cycle, otherwise antimatter would be created out of nothing.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 300)
    public static void severalCellsDoNotDuplicateAntimatter(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        AntimatterTankEntity tank = helper.getBlockEntity(new BlockPos(1, 1, 1));
        int cellCapacity = ODConfig.ANTIMATTER_CELL_CAPACITY.get();
        tank.insertAntimatter(0, ODAntimatters.ANTIPROTONS, cellCapacity + 2000, false);

        ItemStack cells = new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get());
        cells.setCount(3);
        tank.getItemHandler().setStackInSlot(AntimatterTankEntity.INPUT_SLOT, cells);

        helper.runAfterDelay(20, () -> {
            ItemStack output = tank.getOutputStack();
            if (output.isEmpty()) {
                helper.fail("No filled cell appeared in the output slot");
                return;
            }
            if (output.getCount() != 1) {
                helper.fail("The output slot holds " + output.getCount() + " cells instead of one");
                return;
            }
            // one cell was filled, so exactly one cell worth of antimatter left the tank
            if (tank.getAmount(0) != 2000) {
                helper.fail("The tank holds " + tank.getAmount(0) + " u instead of 2000");
                return;
            }            // the two remaining empty cells are still in the input slot
            ItemStack input = tank.getInputStack();
            if (input.getCount() != 2) {
                helper.fail("The input slot holds " + input.getCount() + " cells instead of two");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * The ultimate pipe has to move 32 buckets per tick, so a full tank has to be transferred
     * within a few ticks.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 300)
    public static void ultimatePipeIsFast(GameTestHelper helper) {
        helper.setBlock(0, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());
        helper.setBlock(1, 1, 0, ODBlocks.ANTIMATTER_PIPE_ULTIMATE.get().defaultBlockState());
        helper.setBlock(2, 1, 0, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (helper.getBlockEntity(new BlockPos(1, 1, 0)) instanceof AntimatterPipeBlockEntity pipe) {
                pipe.setSideMode(Direction.WEST, PipeSideMode.PULL);
                pipe.setSideMode(Direction.EAST, PipeSideMode.PUSH);
            }
            AntimatterTankEntity source = helper.getBlockEntity(new BlockPos(0, 1, 0));
            source.insertAntimatter(0, ODAntimatters.ANTIPROTONS, source.getCapacity(0), false);
        });

        helper.runAfterDelay(20, () -> {
            AntimatterTankEntity target = helper.getBlockEntity(new BlockPos(2, 1, 0));
            if (target.getAmount(0) < 5000) {
                helper.fail("The ultimate pipe only moved " + target.getAmount(0) + " u");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * The tank needs a gui that can be used to load and unload the cells.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 200)
    public static void tankHasACellGui(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        helper.runAfterDelay(5, () -> {
            if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof MenuProvider provider)) {
                helper.fail("The tank does not provide a menu");
                return;
            }
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            AbstractContainerMenu menu = provider.createMenu(1, player.getInventory(), player);
            if (menu == null) {
                helper.fail("The tank could not create its menu");
                return;
            }
            if (menu.slots.size() < 2) {
                helper.fail("The tank menu has no input and output slot");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * A cell that holds a different type of antimatter must not interact with the tank.
     */
    @GameTest(template = "antimatter_test", timeoutTicks = 300)
    public static void tankRejectsMismatchedCell(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, ODBlocks.ANTIMATTER_TANK.get().defaultBlockState());

        AntimatterTankEntity tank = helper.getBlockEntity(new BlockPos(1, 1, 1));
        tank.insertAntimatter(0, ODAntimatters.ANTIPROTONS, 500, false);

        ItemStack cell = new ItemStack(ODItems.EMPTY_ANTIMATTER_CELL.get());
        ((AntimatterCellItem) cell.getItem()).setContent(cell, ODAntimatters.POSITRONS, 100);
        tank.getItemHandler().setStackInSlot(AntimatterTankEntity.INPUT_SLOT, cell);

        helper.runAfterDelay(20, () -> {
            ItemStack output = tank.getOutputStack();
            if (!output.isEmpty()) {
                helper.fail("The tank processed a cell although the content did not match");
                return;
            }
            if (tank.getAmount(0) != 500) {
                helper.fail("The tank lost antimatter");
                return;
            }
            helper.succeed();
        });
    }
}
