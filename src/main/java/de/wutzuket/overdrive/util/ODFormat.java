package de.wutzuket.overdrive.util;

import java.util.Locale;

/** Zentrale Einheiten-Formatierung fuer GUI/JEI/TOP: KFE/MFE/GFE(/TFE) und mB/B/KB/MB/GB. */
public final class ODFormat {
    private ODFormat() {}

    private static String num(double v) {
        // max. 2 Nachkommastellen, Nullen trimmen: 400 -> "400", 285.71 -> "285.71", 1.5 -> "1.5"
        String s = String.format(Locale.ROOT, "%.2f", v);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    /** z.B. 999 -> "999 FE", 285714 -> "285.71 KFE", 400000000 -> "400 MFE", 8000000000 -> "8 GFE" */
    public static String formatEnergy(long fe) {
        double v = fe;
        if (v < 1000) return fe + " FE";
        v /= 1000;
        if (v < 1000) return num(v) + " KFE";
        v /= 1000;
        if (v < 1000) return num(v) + " MFE";
        v /= 1000;
        if (v < 1000) return num(v) + " GFE";
        return num(v / 1000) + " TFE";
    }

    public static String formatEnergyPerTick(long fePerTick) {
        return formatEnergy(fePerTick) + "/t";
    }

    /** mB-Basis, ab 1000 mB in Buckets mit K/M/G: 999 -> "999 mB", 14000 -> "14 B", 280000000 -> "280 KB" */
    public static String formatFluid(long mb) {
        if (mb < 1000) return mb + " mB";
        double b = mb / 1000.0;
        if (b < 1000) return num(b) + " B";
        b /= 1000;
        if (b < 1000) return num(b) + " KB";
        b /= 1000;
        if (b < 1000) return num(b) + " MB";
        return num(b / 1000) + " GB";
    }

    /** Raten in mB/t, ab 1000 als Buckets: 0.1 -> "0.1 mB/t", 5744 -> "5.74 B/t", 40000 -> "40 B/t" */
    public static String formatFluidPerTick(double mbPerTick) {
        if (mbPerTick < 1000) return num(mbPerTick) + " mB/t";
        double b = mbPerTick / 1000.0;
        if (b < 1000) return num(b) + " B/t";
        b /= 1000;
        if (b < 1000) return num(b) + " KB/t";
        b /= 1000;
        if (b < 1000) return num(b) + " MB/t";
        return num(b / 1000) + " GB/t";
    }

    /** z.B. "14 B / 500 KB" */
    public static String formatFluidPair(long amountMb, long capacityMb) {
        return formatFluid(amountMb) + " / " + formatFluid(capacityMb);
    }

    /** z.B. "400 MFE / 800 MFE" */
    public static String formatEnergyPair(long storedFe, long maxFe) {
        return formatEnergy(storedFe) + " / " + formatEnergy(maxFe);
    }
}
