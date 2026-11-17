#!/usr/bin/env python3
"""Generates the machine sounds of the Overdrive mod.

Every machine of the mod gets its own sound file:

* nine of them are seamless loops that play while the machine is running,
* the circuit press is a one-shot that is played at the end of a craft cycle.

The samples are synthesised from scratch (no external samples are used) and
written as 16 bit mono WAV. They are then converted to Ogg Vorbis with ffmpeg,
because Minecraft only accepts Ogg Vorbis for sounds.

Run it from the project root:

    python tools/generate_machine_sounds.py

ffmpeg has to be available in the PATH.
"""

import math
import os
import random
import struct
import subprocess
import sys
import wave

SAMPLE_RATE = 44100
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUTPUT_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "overdrive", "sounds", "machine")

# Length of the generated loops in seconds. Every tone frequency has to be a
# multiple of 1 / LOOP_SECONDS so that the loop has no discontinuity.
LOOP_SECONDS = 2.0


# ---------------------------------------------------------------------------
# signal helpers
# ---------------------------------------------------------------------------


def new_buffer(seconds):
    return [0.0] * int(SAMPLE_RATE * seconds)


def add_tone(buffer, freq, amp, harmonics=((1.0, 1.0),), phase=0.0):
    """Adds a harmonic series to the buffer.

    ``freq`` has to be a multiple of 1 / len(buffer) so that the tone fits the
    loop exactly.
    """
    sin = math.sin
    for multiple, harmonic_amp in harmonics:
        step = 2.0 * math.pi * freq * multiple / SAMPLE_RATE
        gain = amp * harmonic_amp
        pos = phase
        for i in range(len(buffer)):
            buffer[i] += gain * sin(pos)
            pos += step
    return buffer


def add_sweep(buffer, start_freq, end_freq, amp, phase=0.0):
    """Adds a tone that glides from start_freq to end_freq over the buffer."""
    sin = math.sin
    n = len(buffer)
    pos = phase
    step = 2.0 * math.pi * start_freq / SAMPLE_RATE
    growth = (2.0 * math.pi * end_freq / SAMPLE_RATE) - step
    for i in range(n):
        buffer[i] += amp * sin(pos)
        pos += step
        step += growth / n
    return buffer


def add_noise(buffer, amp, cutoff_hz, rng, highpass=False):
    """Adds low pass filtered noise to the buffer."""
    n = len(buffer)
    coefficient = min(1.0, 2.0 * math.pi * cutoff_hz / SAMPLE_RATE)
    previous = 0.0
    random_value = rng.random
    for i in range(n):
        raw = random_value() * 2.0 - 1.0
        previous += coefficient * (raw - previous)
        buffer[i] += amp * (raw - previous if highpass else previous)
    return buffer


def add_resonance(buffer, amp, center_hz, resonance, rng):
    """Adds resonant band limited noise to the buffer.

    Uses a Chamberlin state variable filter, which is cheap and stable for the
    frequencies that are used here.
    """
    n = len(buffer)
    frequency = 2.0 * math.sin(math.pi * center_hz / SAMPLE_RATE)
    damping = 1.0 / resonance
    low = 0.0
    band = 0.0
    random_value = rng.random
    for i in range(n):
        raw = random_value() * 2.0 - 1.0
        high = raw - low - damping * band
        band += frequency * high
        low += frequency * band
        buffer[i] += amp * band
    return buffer


def add_crackle(buffer, amp, count, decay, rng, cutoff_hz=4000.0):
    """Adds short noise bursts, used for fire and for electric discharges."""
    n = len(buffer)
    coefficient = min(1.0, 2.0 * math.pi * cutoff_hz / SAMPLE_RATE)
    for _ in range(count):
        start = rng.randrange(0, n)
        length = int(SAMPLE_RATE * (0.004 + rng.random() * 0.02))
        previous = 0.0
        for i in range(length):
            if start + i >= n:
                break
            raw = rng.random() * 2.0 - 1.0
            previous += coefficient * (raw - previous)
            envelope = math.exp(-decay * i / max(1, length))
            buffer[start + i] += amp * previous * envelope
    return buffer


def modulate(buffer, rate_hz, depth, offset=1.0):
    """Applies a periodic amplitude modulation.

    ``rate_hz`` has to be a multiple of 1 / len(buffer) so that the modulation
    fits the loop exactly.
    """
    n = len(buffer)
    step = 2.0 * math.pi * rate_hz / SAMPLE_RATE
    sin = math.sin
    pos = 0.0
    for i in range(n):
        buffer[i] *= offset + depth * sin(pos)
        pos += step
    return buffer


def crossfade_loop(buffer, fade_seconds):
    """Blends the tail of a buffer into its head so that it loops seamlessly.

    The returned buffer is shorter than the input by ``fade_seconds``.
    """
    fade = int(SAMPLE_RATE * fade_seconds)
    n = len(buffer) - fade
    result = buffer[:n]
    for i in range(fade):
        weight = i / fade
        result[i] = result[i] * weight + buffer[n + i] * (1.0 - weight)
    return result


def loop_noise(seconds, fade_seconds, rng, generator):
    """Creates a noise buffer that can be looped without a click."""
    raw = generator(new_buffer(seconds + fade_seconds))
    return crossfade_loop(raw, fade_seconds)


def soft_clip(buffer, drive=1.0):
    """Limits the buffer without hard clipping."""
    tanh = math.tanh
    for i in range(len(buffer)):
        buffer[i] = tanh(buffer[i] * drive) / tanh(drive)
    return buffer


def normalize(buffer, peak=0.88):
    """Scales the buffer so that the loudest sample reaches ``peak``."""
    highest = 0.0
    for sample in buffer:
        if abs(sample) > highest:
            highest = abs(sample)
    if highest < 1e-9:
        return buffer
    factor = peak / highest
    for i in range(len(buffer)):
        buffer[i] *= factor
    return buffer


def fade_in_out(buffer, seconds):
    """Fades the beginning and the end of a one-shot."""
    fade = min(int(SAMPLE_RATE * seconds), len(buffer) // 2)
    for i in range(fade):
        factor = i / fade
        buffer[i] *= factor
        buffer[len(buffer) - 1 - i] *= factor
    return buffer


def envelope(length, attack, decay):
    """Builds a simple attack/decay envelope for a one-shot."""
    result = [0.0] * length
    attack_samples = max(1, int(SAMPLE_RATE * attack))
    for i in range(length):
        if i < attack_samples:
            result[i] = i / attack_samples
        else:
            result[i] = math.exp(-decay * (i - attack_samples) / SAMPLE_RATE)
    return result


# ---------------------------------------------------------------------------
# the individual machine sounds
# ---------------------------------------------------------------------------


def coal_generator():
    """A burning coal generator: low roar with random crackling."""
    rng = random.Random(101)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 60.0, 0.22, harmonics=((1.0, 1.0), (2.0, 0.45), (3.0, 0.2)))
    add_tone(buffer, 120.0, 0.08, harmonics=((1.0, 1.0), (2.0, 0.3)))
    roar = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_noise(b, 0.5, 420.0, rng))
    for i in range(len(buffer)):
        buffer[i] += roar[i] * 0.55
    crackle = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_crackle(b, 1.0, 26, 9.0, rng, 5200.0))
    for i in range(len(buffer)):
        buffer[i] += crackle[i] * 0.22
    modulate(buffer, 3.0, 0.08)
    return normalize(soft_clip(buffer, 1.1))


def particle_accelerator():
    """The particle accelerator: deep hum with an energetic high whine."""
    rng = random.Random(202)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 60.0, 0.24, harmonics=((1.0, 1.0), (2.0, 0.4), (4.0, 0.15)))
    add_tone(buffer, 240.0, 0.09, harmonics=((1.0, 1.0), (1.5, 0.4), (2.0, 0.25)))
    add_tone(buffer, 480.0, 0.05, harmonics=((1.0, 1.0), (1.5, 0.3)))
    hiss = loop_noise(LOOP_SECONDS, 0.2, rng, lambda b: add_resonance(b, 0.5, 3200.0, 3.0, rng))
    for i in range(len(buffer)):
        buffer[i] += hiss[i] * 0.09
    modulate(buffer, 2.0, 0.12)
    modulate(buffer, 8.0, 0.05, 1.0)
    return normalize(soft_clip(buffer, 1.15))


def ad_ring():
    """The AD ring: a slightly deeper accelerator drone."""
    rng = random.Random(303)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 45.0, 0.26, harmonics=((1.0, 1.0), (2.0, 0.35), (3.0, 0.12)))
    add_tone(buffer, 180.0, 0.08, harmonics=((1.0, 1.0), (1.5, 0.35)))
    add_tone(buffer, 360.0, 0.05, harmonics=((1.0, 1.0),))
    hiss = loop_noise(LOOP_SECONDS, 0.2, rng, lambda b: add_resonance(b, 0.5, 2400.0, 2.5, rng))
    for i in range(len(buffer)):
        buffer[i] += hiss[i] * 0.08
    modulate(buffer, 2.0, 0.1)
    return normalize(soft_clip(buffer, 1.1))


def elena_ring():
    """The ELENA ring: a brighter accelerator drone."""
    rng = random.Random(404)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 90.0, 0.2, harmonics=((1.0, 1.0), (2.0, 0.4), (3.0, 0.18)))
    add_tone(buffer, 360.0, 0.08, harmonics=((1.0, 1.0), (1.5, 0.4), (2.0, 0.2)))
    add_tone(buffer, 600.0, 0.045, harmonics=((1.0, 1.0),))
    hiss = loop_noise(LOOP_SECONDS, 0.2, rng, lambda b: add_resonance(b, 0.5, 4200.0, 3.5, rng))
    for i in range(len(buffer)):
        buffer[i] += hiss[i] * 0.09
    modulate(buffer, 4.0, 0.1)
    return normalize(soft_clip(buffer, 1.1))


def electron_gun():
    """The electron gun: sharp electric discharge around a buzz."""
    rng = random.Random(505)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 150.0, 0.16, harmonics=((1.0, 1.0), (2.0, 0.5), (3.0, 0.25)))
    add_tone(buffer, 1200.0, 0.045, harmonics=((1.0, 1.0),))
    buzz = loop_noise(LOOP_SECONDS, 0.2, rng, lambda b: add_resonance(b, 0.5, 5200.0, 6.0, rng))
    for i in range(len(buffer)):
        buffer[i] += buzz[i] * 0.07
    crackle = loop_noise(LOOP_SECONDS, 0.2, rng, lambda b: add_crackle(b, 1.0, 22, 14.0, rng, 7000.0))
    for i in range(len(buffer)):
        buffer[i] += crackle[i] * 0.3
    modulate(buffer, 16.0, 0.18)
    return normalize(soft_clip(buffer, 1.2))


def cryo_distiller():
    """The cryo distiller: cold air hiss over a slow pulse."""
    rng = random.Random(606)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 80.0, 0.16, harmonics=((1.0, 1.0), (2.0, 0.3)))
    add_tone(buffer, 320.0, 0.04, harmonics=((1.0, 1.0),))
    air = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_noise(b, 0.5, 6000.0, rng, highpass=True))
    for i in range(len(buffer)):
        buffer[i] += air[i] * 0.3
    breath = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_resonance(b, 0.5, 1400.0, 1.6, rng))
    for i in range(len(buffer)):
        buffer[i] += breath[i] * 0.16
    modulate(buffer, 1.0, 0.28)
    modulate(buffer, 5.0, 0.07)
    return normalize(soft_clip(buffer, 1.1))


def vacuum_pump():
    """The vacuum pump: a motor with a slow piston rhythm."""
    rng = random.Random(707)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 70.0, 0.22, harmonics=((1.0, 1.0), (2.0, 0.5), (3.0, 0.2)))
    add_tone(buffer, 210.0, 0.05, harmonics=((1.0, 1.0),))
    rumble = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_noise(b, 0.5, 300.0, rng))
    for i in range(len(buffer)):
        buffer[i] += rumble[i] * 0.22
    air = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_resonance(b, 0.5, 1800.0, 2.0, rng))
    for i in range(len(buffer)):
        buffer[i] += air[i] * 0.13
    modulate(buffer, 2.5, 0.3)
    modulate(buffer, 10.0, 0.06)
    return normalize(soft_clip(buffer, 1.1))


def annihilation_reactor():
    """The annihilation reactor: a deep, heavy reactor rumble."""
    rng = random.Random(808)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 40.0, 0.3, harmonics=((1.0, 1.0), (2.0, 0.4), (3.0, 0.18), (5.0, 0.08)))
    add_tone(buffer, 100.0, 0.1, harmonics=((1.0, 1.0), (2.0, 0.3)))
    roar = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_noise(b, 0.5, 260.0, rng))
    for i in range(len(buffer)):
        buffer[i] += roar[i] * 0.4
    surge = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_resonance(b, 0.5, 900.0, 1.4, rng))
    for i in range(len(buffer)):
        buffer[i] += surge[i] * 0.14
    modulate(buffer, 1.0, 0.22)
    return normalize(soft_clip(buffer, 1.15))


def cooling_tower():
    """The cooling tower: a big fan moving a lot of air."""
    rng = random.Random(909)
    buffer = new_buffer(LOOP_SECONDS)
    add_tone(buffer, 100.0, 0.14, harmonics=((1.0, 1.0), (2.0, 0.3)))
    whoosh = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_resonance(b, 0.5, 700.0, 1.3, rng))
    for i in range(len(buffer)):
        buffer[i] += whoosh[i] * 0.42
    air = loop_noise(LOOP_SECONDS, 0.25, rng, lambda b: add_noise(b, 0.5, 2600.0, rng, highpass=True))
    for i in range(len(buffer)):
        buffer[i] += air[i] * 0.12
    modulate(buffer, 1.0, 0.42)
    modulate(buffer, 4.0, 0.1)
    return normalize(soft_clip(buffer, 1.1))


def circuit_press():
    """The circuit press: a one-shot that plays at the end of a craft cycle.

    Hydraulic release, the press slams down, a short metallic ring and then
    silence again.
    """
    rng = random.Random(1010)
    total = 1.2
    buffer = new_buffer(total)

    # hydraulic release at the beginning
    release = new_buffer(0.35)
    add_noise(release, 0.5, 2500.0, rng, highpass=True)
    release_env = envelope(len(release), 0.01, 7.0)
    for i in range(len(release)):
        buffer[i] += release[i] * release_env[i] * 0.5

    # motor spin up
    spin = new_buffer(0.35)
    add_sweep(spin, 110.0, 230.0, 0.5)
    spin_env = envelope(len(spin), 0.02, 3.0)
    for i in range(len(spin)):
        buffer[i] += spin[i] * spin_env[i] * 0.35

    # the press hits the workpiece
    hit_start = int(SAMPLE_RATE * 0.33)
    thud = new_buffer(0.25)
    add_tone(thud, 85.0, 0.9, harmonics=((1.0, 1.0), (1.6, 0.3)))
    add_noise(thud, 0.5, 900.0, rng)
    thud_env = envelope(len(thud), 0.002, 16.0)
    for i in range(len(thud)):
        if hit_start + i < len(buffer):
            buffer[hit_start + i] += thud[i] * thud_env[i] * 0.9

    # short metallic ring
    ring_start = int(SAMPLE_RATE * 0.35)
    ring = new_buffer(0.55)
    add_tone(ring, 780.0, 0.5, harmonics=((1.0, 1.0), (2.4, 0.25)))
    add_tone(ring, 1240.0, 0.3, harmonics=((1.0, 1.0),))
    add_noise(ring, 0.4, 6000.0, rng, highpass=True)
    ring_env = envelope(len(ring), 0.001, 11.0)
    for i in range(len(ring)):
        if ring_start + i < len(buffer):
            buffer[ring_start + i] += ring[i] * ring_env[i] * 0.4

    fade_in_out(buffer, 0.01)
    return normalize(soft_clip(buffer, 1.3))


# ---------------------------------------------------------------------------
# output
# ---------------------------------------------------------------------------

SOUNDS = {
    "coal_generator": coal_generator,
    "particle_accelerator": particle_accelerator,
    "ad_ring": ad_ring,
    "elena_ring": elena_ring,
    "electron_gun": electron_gun,
    "cryo_distiller": cryo_distiller,
    "vacuum_pump": vacuum_pump,
    "annihilation_reactor": annihilation_reactor,
    "cooling_tower": cooling_tower,
    "circuit_press": circuit_press,
}


def write_wav(path, samples):
    data = bytearray()
    for sample in samples:
        value = int(max(-1.0, min(1.0, sample)) * 32767.0)
        data += struct.pack("<h", value)
    with wave.open(path, "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        handle.writeframes(bytes(data))


def encode(source, target):
    command = [
        "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
        "-i", source,
        "-c:a", "libvorbis", "-q:a", "5",
        "-ar", str(SAMPLE_RATE),
        "-ac", "1",
        target,
    ]
    try:
        subprocess.run(command, check=True)
    except FileNotFoundError:
        print("ffmpeg was not found in the PATH, cannot encode the ogg files.")
        return False
    except subprocess.CalledProcessError as error:
        print("ffmpeg failed for {}: {}".format(target, error))
        return False
    return True


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    for name, generator in SOUNDS.items():
        samples = generator()
        wav_path = os.path.join(OUTPUT_DIR, name + ".wav")
        ogg_path = os.path.join(OUTPUT_DIR, name + ".ogg")
        write_wav(wav_path, samples)
        if not encode(wav_path, ogg_path):
            return 1
        os.remove(wav_path)
        print("{}: {:.2f} s".format(name, len(samples) / SAMPLE_RATE))
    return 0


if __name__ == "__main__":
    sys.exit(main())
