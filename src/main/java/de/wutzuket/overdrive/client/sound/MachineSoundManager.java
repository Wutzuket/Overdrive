package de.wutzuket.overdrive.client.sound;

import de.wutzuket.overdrive.Overdrive;
import de.wutzuket.overdrive.config.ODConfig;
import de.wutzuket.overdrive.sound.IMachineSound;
import de.wutzuket.overdrive.sound.ODSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Plays the running sound of every machine that is close to the player.
 *
 * The client looks for the block entities of the loaded chunks around the
 * player and asks them whether they are running. Every machine that reports
 * that it is running gets a looping sound that is faded in and out.
 */
@EventBusSubscriber(modid = Overdrive.MODID, value = Dist.CLIENT)
public final class MachineSoundManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Client ticks between two searches for running machines. */
    private static final int SCAN_INTERVAL = 4;
    /**
     * Extra blocks that are searched around the player. The controller of a big
     * multiblock can be far away from the structure, but the sound is played at
     * the structure.
     */
    private static final int STRUCTURE_MARGIN = 30;
    /**
     * How many sounds of machines may play at the same time. The sound engine
     * has only a limited number of channels, so the closest machines win.
     */
    private static final int MAX_SIMULTANEOUS_SOUNDS = 24;

    /** The running sounds of the machines, by block position. */
    private static final Map<BlockPos, MachineSoundInstance> SOUNDS = new HashMap<>();
    /** The positions of the machines that are running in the current scan. */
    private static final Set<BlockPos> RUNNING = new HashSet<>();
    /** The sounds of the current scan, sorted by their distance to the player. */
    private static final List<Candidate> candidates = new ArrayList<>();

    private static int ticks;
    private static int lastRunningCount = -1;

    /** Ein Klang, der gerade laufen soll. */
    private record Candidate(BlockPos pos, SoundEvent event, float volume, double distance) {
    }

    private MachineSoundManager() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !ODConfig.ENABLE_MACHINE_SOUNDS.get()) {
            stopAll(mc);
            return;
        }
        if (++ticks % SCAN_INTERVAL != 0) {
            return;
        }
        scan(mc);
    }

    /**
     * Searches the chunks around the player for machines that are running and
     * starts or stops their sounds.
     */
    private static void scan(Minecraft mc) {
        ClientLevel level = mc.level;
        BlockPos player = mc.player.blockPosition();
        double range = ODConfig.MACHINE_SOUND_RANGE.get();
        // Die Config skaliert nur die Grundlautstaerke, damit der Pegel auch mit
        // einer alten Config-Datei gleich bleibt.
        float volume = ODSounds.BASE_VOLUME * ODConfig.MACHINE_SOUND_VOLUME.get().floatValue();
        int blockRange = (int) Math.ceil(range);
        double rangeSquared = range * range;

        candidates.clear();

        // Die Maschine wird ueber ihren Controller gefunden, der Klang kommt aber
        // von der Struktur. Gesucht wird deshalb etwas weiter als hoerbar.
        ClientChunkCache chunks = level.getChunkSource();
        int minChunkX = chunkOf(player.getX() - blockRange - STRUCTURE_MARGIN);
        int maxChunkX = chunkOf(player.getX() + blockRange + STRUCTURE_MARGIN);
        int minChunkZ = chunkOf(player.getZ() - blockRange - STRUCTURE_MARGIN);
        int maxChunkZ = chunkOf(player.getZ() + blockRange + STRUCTURE_MARGIN);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = chunks.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof IMachineSound machine)) {
                        continue;
                    }
                    if (!machine.isMachineRunning()) {
                        continue;
                    }
                    BlockPos controller = blockEntity.getBlockPos();
                    var positions = machine.getSoundPositions(controller, blockEntity.getBlockState());
                    if (positions.isEmpty()) {
                        continue;
                    }
                    // Jede Stelle der Struktur traegt einen Teil des Klangs, damit
                    // die Maschine insgesamt nicht lauter wird.
                    float positionVolume = volume / (float) Math.sqrt(positions.size());
                    for (BlockPos pos : positions) {
                        double dx = pos.getX() + 0.5D - player.getX();
                        double dy = pos.getY() + 0.5D - player.getY();
                        double dz = pos.getZ() + 0.5D - player.getZ();
                        double distance = dx * dx + dy * dy + dz * dz;
                        if (distance > rangeSquared) {
                            continue;
                        }
                        candidates.add(new Candidate(pos, machine.getMachineSound(), positionVolume, distance));
                    }
                }
            }
        }

        // Der Kanal-Pool der Sound-Engine ist begrenzt, deshalb kommen die
        // naechsten Maschinen zuerst dran.
        candidates.sort(Comparator.comparingDouble(candidate -> candidate.distance));

        RUNNING.clear();
        int count = Math.min(candidates.size(), MAX_SIMULTANEOUS_SOUNDS);
        for (int i = 0; i < count; i++) {
            Candidate candidate = candidates.get(i);
            RUNNING.add(candidate.pos);
            play(mc, candidate.pos, candidate.event, candidate.volume);
        }

        // Machines that stopped running let their sound fade out.
        Iterator<Map.Entry<BlockPos, MachineSoundInstance>> iterator = SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, MachineSoundInstance> entry = iterator.next();
            if (RUNNING.contains(entry.getKey())) {
                continue;
            }
            entry.getValue().setWanted(false);
            if (entry.getValue().isStopped()) {
                iterator.remove();
            }
        }

        if (RUNNING.size() != lastRunningCount) {
            lastRunningCount = RUNNING.size();
            LOGGER.debug("Overdrive machine sounds: {} sound source(s) nearby, {} sound(s) playing",
                    lastRunningCount, SOUNDS.size());
        }
    }

    /** Starts the sound of a machine or keeps the running one alive. */
    private static void play(Minecraft mc, BlockPos pos, SoundEvent event, float volume) {
        MachineSoundInstance instance = SOUNDS.get(pos);
        if (instance != null) {
            if (instance.getLocation().equals(event.getLocation())) {
                instance.setWanted(true);
                instance.setTargetVolume(volume);
                return;
            }
            // The machine changed its sound, the old one fades out.
            instance.setWanted(false);
        }
        MachineSoundInstance created = new MachineSoundInstance(event, pos, volume, 1.0F);
        SOUNDS.put(pos, created);
        mc.getSoundManager().play(created);
    }

    /** Stops all running sounds, used when the world is left or disabled. */
    private static void stopAll(Minecraft mc) {
        for (MachineSoundInstance instance : SOUNDS.values()) {
            instance.setWanted(false);
            mc.getSoundManager().stop(instance);
        }
        SOUNDS.clear();
        RUNNING.clear();
        candidates.clear();
    }

    private static int chunkOf(int block) {
        return block >> 4;
    }
}
