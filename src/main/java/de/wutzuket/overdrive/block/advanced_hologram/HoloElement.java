package de.wutzuket.overdrive.block.advanced_hologram;

public record HoloElement(
        double x, double y, double z,
        double sizeX, double sizeY, double sizeZ,
        double rotX, double rotY, double rotZ,
        float red, float green, float blue, float alpha,
        String text, double scale, String alignment
) {
    public HoloElement(
            double x, double y, double z,
            double sizeX, double sizeY, double sizeZ,
            double rotX, double rotY, double rotZ,
            float red, float green, float blue, float alpha
    ) {
        this(x, y, z, sizeX, sizeY, sizeZ, rotX, rotY, rotZ, red, green, blue, alpha, null, 1.0D, "left");
    }
}