package de.wutzuket.overdrive.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The looping sound of a running machine.
 *
 * The sound fades in when the machine starts and fades out when it stops, so
 * that it never clicks. It stops itself once the fade out is finished.
 */
@OnlyIn(Dist.CLIENT)
public class MachineSoundInstance extends AbstractTickableSoundInstance {
    /** Ticks that the sound needs for fading in and for fading out. */
    private static final int FADE_TICKS = 8;
    /**
     * Share of the volume that the sound starts with. It may not be zero,
     * because the sound engine refuses to start a sound with a volume of zero.
     */
    private static final float START_VOLUME = 0.05F;

    private float targetVolume;
    private float fade;
    private boolean wanted;

    public MachineSoundInstance(SoundEvent event, BlockPos pos, float volume, float pitch) {
        super(event, SoundSource.BLOCKS, RandomSource.create());
        this.targetVolume = volume;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        this.volume = volume * START_VOLUME;
        this.pitch = pitch;
        this.looping = true;
        this.wanted = true;
        this.fade = 1.0F / FADE_TICKS;
    }

    /** Changes the volume that the sound fades up to. */
    public void setTargetVolume(float targetVolume) {
        this.targetVolume = targetVolume;
    }

    /**
     * The sound fades in from a volume of nearly zero. Without this the sound
     * engine would skip the sound completely, because it refuses to start a
     * sound that has a volume of zero.
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    /** Starts the sound or lets it fade out again. */
    public void setWanted(boolean wanted) {
        this.wanted = wanted;
    }

    /** True while the sound should be playing. */
    public boolean isWanted() {
        return this.wanted;
    }

    /** The current volume of the sound, without the share of the sound file. */
    public float getCurrentVolume() {
        return this.volume;
    }

    @Override
    public void tick() {
        float target = this.wanted ? 1.0F : 0.0F;
        if (this.fade < target) {
            this.fade = Math.min(target, this.fade + 1.0F / FADE_TICKS);
        } else if (this.fade > target) {
            this.fade = Math.max(target, this.fade - 1.0F / FADE_TICKS);
        }
        this.volume = this.targetVolume * this.fade;
        if (!this.wanted && this.fade <= 0.0F) {
            stop();
        }
    }
}
