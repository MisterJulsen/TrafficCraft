package de.mrjulsen.legacydragonlib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public final class Math {
    public static double round(double value, int decimals) {
        if (decimals < 0)
            throw new IllegalArgumentException();

        long factor = (long) java.lang.Math.pow(10, decimals);
        value = value * factor;
        long tmp = java.lang.Math.round(value);
        return (double) tmp / factor;
    }

    public static Vec3i blockPosToVec3i(BlockPos pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3 blockPosToVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static double slope(Vec3 a, Vec3 b) {
        double heightDiff = java.lang.Math.max(a.y, b.y) - java.lang.Math.min(a.y, b.y);
        Vec3 vec = b.subtract(a);
        double distance = vec.horizontalDistance();
        return distance / heightDiff;
    }

    public static double lerp(double pDelta, double pStart, double pEnd) {
        return pStart + pDelta * (pEnd - pStart);    
    }

    public static byte clamp(byte pValue, byte pMin, byte pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static int clamp(int pValue, int pMin, int pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static long clamp(long pValue, long pMin, long pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }

    public static double clamp(double pValue, double pMin, double pMax) {
        if (pValue < pMin) {
            return pMin;
        } else {
            return pValue > pMax ? pMax : pValue;
        }
    }
}
