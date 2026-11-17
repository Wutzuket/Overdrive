"""Generates the seamless plasma texture of the annihilation reactor.

The texture is a grayscale pattern of swirling veins. It is drawn with the
vanilla energy_swirl shader, which scrolls the UV coordinates, so the veins
flow over the plasma torus just like the plasma of the Mekanism fusion reactor.

Usage:  python tools/generate_plasma_texture.py
"""

import math
import os
import random
import struct
import zlib

SIZE = 128
OUT = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "src", "main", "resources", "assets", "overdrive",
    "textures", "entity", "plasma.png",
)


def lattice(n, seed):
    """Random grid that repeats seamlessly (tileable value noise)."""
    rnd = random.Random(seed)
    return [[rnd.random() for _ in range(n)] for _ in range(n)]


def smooth(t):
    return t * t * (3.0 - 2.0 * t)


def value_noise(grid, size):
    """Tileable value noise sampled at every pixel of a size x size image."""
    n = len(grid)
    out = [[0.0] * size for _ in range(size)]
    for y in range(size):
        fy = y / size * n
        y0 = int(math.floor(fy)) % n
        y1 = (y0 + 1) % n
        ty = smooth(fy - math.floor(fy))
        row0 = grid[y0]
        row1 = grid[y1]
        for x in range(size):
            fx = x / size * n
            x0 = int(math.floor(fx)) % n
            x1 = (x0 + 1) % n
            tx = smooth(fx - math.floor(fx))
            a = row0[x0] + (row0[x1] - row0[x0]) * tx
            b = row1[x0] + (row1[x1] - row1[x0]) * tx
            out[y][x] = a + (b - a) * ty
    return out


def build_pattern():
    octaves = [
        (lattice(4, 11), 0.46),
        (lattice(8, 22), 0.27),
        (lattice(16, 33), 0.16),
        (lattice(32, 44), 0.11),
    ]
    fields = [(value_noise(grid, SIZE), weight) for grid, weight in octaves]
    pixels = []
    for y in range(SIZE):
        row = bytearray()
        for x in range(SIZE):
            n = 0.0
            for field, weight in fields:
                n += field[y][x] * weight
            # Ridged noise -> hell leuchtende Adern auf dunklem Grund.
            vein = 1.0 - abs(2.0 * n - 1.0)
            v = 0.22 + 0.78 * (vein ** 1.7)
            # Leichte Kontrastanhebung, damit die Adern klar herausstechen.
            v = min(1.0, max(0.0, (v - 0.18) / 0.82))
            level = int(round(v * 255.0))
            row += bytes((level, level, level, 255))
        pixels.append(row)
    return pixels


def write_png(path, pixels, size):
    raw = b"".join(b"\x00" + bytes(row) for row in pixels)

    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    header = struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", header)
           + chunk(b"IDAT", zlib.compress(raw, 9))
           + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)


def main():
    write_png(OUT, build_pattern(), SIZE)
    print("wrote", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
