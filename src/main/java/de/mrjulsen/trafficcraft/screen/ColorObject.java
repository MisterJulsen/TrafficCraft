package de.mrjulsen.trafficcraft.screen;

import java.awt.Color;

import net.minecraft.util.Mth;

public class ColorObject {
    private int r, g, b, a;

    public ColorObject(int r, int g, int b, int a) {
        this.r = Mth.clamp(r, 0, 255);
        this.g = Mth.clamp(g, 0, 255);
        this.b = Mth.clamp(b, 0, 255);
        this.a = Mth.clamp(a, 0, 255);
    }

    public ColorObject(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public ColorObject(float r, float g, float b, float a) {
        this(colorFloatToInt(r), colorFloatToInt(g), colorFloatToInt(b), colorFloatToInt(a));
    }

    public ColorObject(float r, float g, float b) {
        this(r, g, b, 1f);
    }

    private static float colorIntToFloat(int i) {
        i = Mth.clamp(i, 0, 255);
        return 1.0f / i;
    }
    
    private static int colorFloatToInt(float f) {
        f = Mth.clamp(f, 0, 1);
        return (int)(255 * f);
    }

    public static ColorObject fromHSV(float h, float s, float b) {
        Color c = Color.getHSBColor(h, s, b);
        return new ColorObject(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    public static ColorObject fromHSV(double h, double s, double b) {
        return fromHSV((float)h, (float)s, (float)b);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getA() {
        return a;
    }

    public float getRFloat() {
        return colorIntToFloat(r);
    }

    public float getGFloat() {
        return colorIntToFloat(g);
    }

    public float getBFloat() {
        return colorIntToFloat(b);
    }

    public float getAFloat() {
        return colorIntToFloat(a);
    }

    public int toInt() {
        r = Math.max(0, Math.min(255, this.getR()));
        g = Math.max(0, Math.min(255, this.getG()));
        b = Math.max(0, Math.min(255, this.getB()));
        a = Math.max(0, Math.min(255, this.getA()));

        int rgba = (a << 24) | (r << 16) | (g << 8) | b;
        return rgba;
    }

    public static ColorObject fromInt(int rgba) {
        int a = (rgba >> 24) & 0xFF;
        int r = (rgba >> 16) & 0xFF;
        int g = (rgba >> 8) & 0xFF;
        int b = rgba & 0xFF;
        return new ColorObject(r, g, b, a);
    }

    public float[] toHSV() {
        float[] hsb = new float[4]; // Hier verwenden wir float anstelle von double

        Color.RGBtoHSB(r, g, b, hsb);
        return hsb;
    }
}
