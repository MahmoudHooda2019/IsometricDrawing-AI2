package me.aemo.isometricdrawing;

public class ColorUtils {

    /**
     * Extracts the red component from a color.
     *
     * @param color The color in ARGB format.
     * @return The red component (0-255).
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from a color.
     *
     * @param color The color in ARGB format.
     * @return The green component (0-255).
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from a color.
     *
     * @param color The color in ARGB format.
     * @return The blue component (0-255).
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    /**
     * Extracts the alpha component from a color.
     *
     * @param color The color in ARGB format.
     * @return The alpha component (0-255).
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
}
