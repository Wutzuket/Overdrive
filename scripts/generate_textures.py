"""Generate casing textures (base + sprite) and pipe texture.

Base texture: 16x16 with border/frame and fill color.
Sprite texture: 16x16 pure fill (no border), used on connected faces.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
BLOCK_TEX = ROOT / "src/main/resources/assets/overdrive/textures/block"

CASINGS = {
    "accelerator_casing": {"base": (55, 58, 66), "accent": (70, 115, 160), "border": (40, 42, 48)},
    "ad_casing":          {"base": (55, 58, 66), "accent": (160, 100, 55), "border": (40, 42, 48)},
    "elena_casing":       {"base": (55, 58, 66), "accent": (65, 140, 85),  "border": (40, 42, 48)},
    "pump_casing":        {"base": (55, 58, 66), "accent": (115, 75, 150), "border": (40, 42, 48)},
    "wolfram_casing":     {"base": (55, 58, 66), "accent": (155, 140, 70), "border": (40, 42, 48)},
}


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def blend(c1, c2, t):
    return tuple(int(lerp(c1[i], c2[i], t)) for i in range(3))


def draw_casing_base(name: str, cfg: dict) -> None:
    """Base texture: border frame + fill center + accent strip."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    base = cfg["base"]
    accent = cfg["accent"]
    border = cfg["border"]

    highlight = blend(base, (200, 205, 215), 0.25)
    shadow = blend(base, (20, 22, 28), 0.35)
    inner_border = blend(border, base, 0.4)
    accent_dim = blend(accent, base, 0.55)
    accent_bright = blend(accent, (255, 255, 255), 0.2)

    for y in range(16):
        for x in range(16):
            if x == 0 or y == 0:
                px[x, y] = (*highlight, 255)
            elif x == 15 or y == 15:
                px[x, y] = (*shadow, 255)
            elif x == 1 or y == 1:
                px[x, y] = (*inner_border, 255)
            elif x == 14 or y == 14:
                px[x, y] = (*blend(border, shadow, 0.3), 255)
            elif y == 13:
                t = x / 15.0
                px[x, y] = (*blend(accent_dim, accent_bright, t * 0.4), 255)
            elif y == 12:
                px[x, y] = (*blend(accent_dim, accent, 0.3), 255)
            else:
                gx = 1.0 - (x / 15.0) * 0.08
                gy = 1.0 - (y / 15.0) * 0.08
                c = tuple(int(base[i] * gx * gy) for i in range(3))
                px[x, y] = (*c, 255)

    img.save(BLOCK_TEX / f"{name}.png")


def draw_casing_sprite(name: str, cfg: dict) -> None:
    """Sprite texture: pure fill, no border. Used on connected faces."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    base = cfg["base"]
    accent = cfg["accent"]
    accent_dim = blend(accent, base, 0.55)
    accent_bright = blend(accent, (255, 255, 255), 0.2)

    for y in range(16):
        for x in range(16):
            if y == 13:
                t = x / 15.0
                px[x, y] = (*blend(accent_dim, accent_bright, t * 0.4), 255)
            elif y == 12:
                px[x, y] = (*blend(accent_dim, accent, 0.3), 255)
            else:
                gx = 1.0 - (x / 15.0) * 0.08
                gy = 1.0 - (y / 15.0) * 0.08
                c = tuple(int(base[i] * gx * gy) for i in range(3))
                px[x, y] = (*c, 255)

    img.save(BLOCK_TEX / f"{name}_sprite.png")


def draw_pipe() -> None:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    outer = (42, 44, 50)
    wall = (65, 68, 76)
    wall_hi = (85, 88, 96)
    wall_lo = (50, 52, 58)
    inner = (22, 24, 28)
    inner_edge = (30, 32, 36)

    cx, cy = 7.5, 7.5
    r_outer = 7.0
    r_wall_hi = 5.5
    r_wall_lo = 4.5

    for y in range(16):
        for x in range(16):
            dx = x - cx + 0.5
            dy = y - cy + 0.5
            dist = (dx * dx + dy * dy) ** 0.5

            if dist > r_outer:
                px[x, y] = (0, 0, 0, 0)
            elif dist > r_wall_hi:
                t = (dist - r_wall_hi) / (r_outer - r_wall_hi)
                c = blend(wall, outer, t)
                shade = 1.0 + 0.12 * (1.0 - (x + y) / 30.0)
                c = tuple(min(255, int(v * shade)) for v in c)
                px[x, y] = (*c, 255)
            elif dist > r_wall_lo:
                t = (dist - r_wall_lo) / (r_wall_hi - r_wall_lo)
                c = blend(wall_lo, wall_hi, t)
                px[x, y] = (*c, 255)
            elif dist > r_wall_lo - 0.8:
                c = blend(inner, inner_edge, 0.5)
                px[x, y] = (*c, 255)
            else:
                px[x, y] = (*inner, 255)

    img.save(BLOCK_TEX / "vacuum_pipe.png")


if __name__ == "__main__":
    BLOCK_TEX.mkdir(parents=True, exist_ok=True)

    for name, cfg in CASINGS.items():
        draw_casing_base(name, cfg)
        draw_casing_sprite(name, cfg)

    draw_pipe()
    print("Textures written to", BLOCK_TEX)
