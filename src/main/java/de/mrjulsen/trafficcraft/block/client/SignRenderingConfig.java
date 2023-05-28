package de.mrjulsen.trafficcraft.block.client;

import java.util.Arrays;
import java.util.stream.DoubleStream;

import net.minecraft.client.gui.Font;

public class SignRenderingConfig {

    private int lines = 1;
    /** Default: 90 */
    public int maxLineWidth = 90;
    /** Default: 65 pixels */
    public int textureYOffset = 64;
    /** Calculated from center. Default: 0 */
    public int textureXOffset = 0;
    /** Default: 95 pixels */
    public int textYOffset = 95;    
    /** Offset from center. Default: 0 pixels */
    public int textXOffset = 0;
    /** Default: 0 */
    public int modelRotation = 0;
    /** Default: 100 */
    public int scale = 100;

    /** Default: 1.0D */
    public final double[] lineHeightMultiplier;

    public final IFontScale[] lineFontScales;

    public SignRenderingConfig(int lines) {
        this.lines = lines;
        lineHeightMultiplier = new double[lines];
        lineFontScales = new IFontScale[lines];
        Arrays.fill(lineHeightMultiplier, 1.0D);        
    }

    public int getLines() {
        return this.lines;
    }

    public final int getLineHeightsTo(Font font, int lineIndex, int fontWidth, int lineWidth) {
        if (lineIndex < 0)
            return 0;

        if (lineIndex > lines)
            lineIndex = lines;

        return (int)(font.lineHeight * DoubleStream.of(lineHeightMultiplier).limit(lineIndex).sum() / (lineFontScales[lineIndex] == null ? 1.0D : lineFontScales[lineIndex].getScale(fontWidth, lineWidth)));
    }

    public int width() {
        return scale;
    }

    public int height() {
        return scale;
    }

    public IFontScale getFontScale(int idx) {
        return idx >= 0 && idx < lineFontScales.length ? lineFontScales[idx] : null;
    }

    public void setFontScale(int lineIndex, IFontScale scaleConfig) {
        lineFontScales[lineIndex] = scaleConfig;
    }

    public static interface IFontScale {
        default double getScale(int fontWidth, int lineWidth) {
            return 1.0D;
        }
    }

    public static class FontScaleConfig implements IFontScale {
        /** Default: 1.0D */
        public double scale = 1.0D;

        public FontScaleConfig(double scale) {
            this.scale = scale;
        }

        @Override
        public double getScale(int fontWidth, int lineWidth) {
            return scale;
        }
    }
    
    public static class AutomaticFontScaleConfig implements IFontScale {

        /** Default: 1.0D */
        public double minScale = 1.0D;
        /** Default: 1.0D */
        public double maxScale = 1.0D;

        public AutomaticFontScaleConfig(double minScale, double maxScale) {
            this.minScale = minScale;
            this.maxScale = maxScale;
        }

        private double calcScale(double minScale, double maxScale, double maxWidth, double fontWidth) {
            double scale = Math.min(maxWidth / fontWidth, 1.0D);
            return Math.max(maxScale * scale, minScale);
        }

        @Override
        public double getScale(int fontWidth, int lineWidth) {
            return calcScale(minScale, maxScale, lineWidth / maxScale, fontWidth);
        }
    }
}
