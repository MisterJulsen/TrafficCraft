package de.mrjulsen.trafficcraft.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class VoxelShapeRotator {

    private static final double PIXEL = 1.0 / 16.0;
    private static final double HALF_PIXEL = PIXEL * 0.5;
    private static final double ANGLE_EPSILON = 0.01;

    private VoxelShapeRotator() {
    }

    // -------------------------------------------------------------------------
    // Public API – single-axis
    // -------------------------------------------------------------------------

    public static VoxelShape rotateY(VoxelShape shape, float yawDeg) {
        return rotateY(shape, yawDeg, new Vec3(0.5, 0.5, 0.5));
    }

    /**
     * Rotates around the Y axis (yaw).
     * Convention: NORTH=0°, WEST=90°, SOUTH=180°, EAST=270° (CW from above).
     */
    public static VoxelShape rotateY(VoxelShape shape, float yawDeg, Vec3 pivot) {
        double angle = normalizeAngle(yawDeg);
        if (isNear(angle, 0.0)) return shape;
        if (isNear(angle, 90.0)) return transformExactY(shape, pivot, 0, 1, -1, 0);
        if (isNear(angle, 180.0)) return transformExactY(shape, pivot, -1, 0, 0, -1);
        if (isNear(angle, 270.0)) return transformExactY(shape, pivot, 0, -1, 1, 0);
        return rasterizeY(shape, angle, pivot);
    }

    public static VoxelShape rotateX(VoxelShape shape, float pitchDeg) {
        return rotateX(shape, pitchDeg, new Vec3(0.5, 0.5, 0.5));
    }

    /**
     * Rotates around the X axis (pitch).
     * Positive angle tilts the top of the shape toward SOUTH (away from viewer).
     */
    public static VoxelShape rotateX(VoxelShape shape, float pitchDeg, Vec3 pivot) {
        double angle = normalizeAngle(pitchDeg);
        if (isNear(angle, 0.0)) return shape;
        if (isNear(angle, 90.0)) return transformExactX(shape, pivot, 0, 1, -1, 0);
        if (isNear(angle, 180.0)) return transformExactX(shape, pivot, -1, 0, 0, -1);
        if (isNear(angle, 270.0)) return transformExactX(shape, pivot, 0, -1, 1, 0);
        return rasterizeX(shape, angle, pivot);
    }

    public static VoxelShape rotateZ(VoxelShape shape, float rollDeg) {
        return rotateZ(shape, rollDeg, new Vec3(0.5, 0.5, 0.5));
    }

    /**
     * Rotates around the Z axis (roll).
     * Positive angle tilts the top of the shape toward EAST (right).
     */
    public static VoxelShape rotateZ(VoxelShape shape, float rollDeg, Vec3 pivot) {
        double angle = normalizeAngle(rollDeg);
        if (isNear(angle, 0.0)) return shape;
        if (isNear(angle, 90.0)) return transformExactZ(shape, pivot, 0, 1, -1, 0);
        if (isNear(angle, 180.0)) return transformExactZ(shape, pivot, -1, 0, 0, -1);
        if (isNear(angle, 270.0)) return transformExactZ(shape, pivot, 0, -1, 1, 0);
        return rasterizeZ(shape, angle, pivot);
    }

    // -------------------------------------------------------------------------
    // Public API – combined Euler (yaw → pitch → roll)
    // -------------------------------------------------------------------------

    public static VoxelShape rotate(VoxelShape shape, float yawDeg, float pitchDeg, float rollDeg) {
        return rotate(shape, yawDeg, pitchDeg, rollDeg, new Vec3(0.5, 0.5, 0.5));
    }

    /**
     * Applies yaw (Y), then pitch (X), then roll (Z) around the given pivot.
     * Each step uses the pixelated rasterizer so the result is always voxel-aligned.
     */
    public static VoxelShape rotate(VoxelShape shape, float yawDeg, float pitchDeg, float rollDeg, Vec3 pivot) {
        VoxelShape result = shape;
        if (!isNear(normalizeAngle(yawDeg), 0.0)) result = rotateY(result, yawDeg, pivot);
        if (!isNear(normalizeAngle(pitchDeg), 0.0)) result = rotateX(result, pitchDeg, pivot);
        if (!isNear(normalizeAngle(rollDeg), 0.0)) result = rotateZ(result, rollDeg, pivot);
        return result;
    }

    // -------------------------------------------------------------------------
    // Quaternion
    // -------------------------------------------------------------------------

    public static VoxelShape rotateByQuaternion(VoxelShape shape, Quaternionf quaternion) {
        return rotateByQuaternion(shape, quaternion, new Vec3(0.5, 0.5, 0.5));
    }

    public static VoxelShape rotateByQuaternion(VoxelShape shape, Quaternionf quaternion, Vec3 pivot) {
        Quaternionf q = new Quaternionf(quaternion).normalize();

        // Fast-path: pure Y-rotation with 90° steps
        if (isNearZero(q.x) && isNearZero(q.z)) {
            float yawRad = 2.0f * (float) Math.atan2(q.y, q.w);
            float yawDeg = (float) Math.toDegrees(yawRad);
            double n = normalizeAngle(yawDeg);
            if (isNear(n, 0.0)) return shape;
            if (isNear(n, 90.0)) return transformExactY(shape, pivot, 0, 1, -1, 0);
            if (isNear(n, 180.0)) return transformExactY(shape, pivot, -1, 0, 0, -1);
            if (isNear(n, 270.0)) return transformExactY(shape, pivot, 0, -1, 1, 0);
        }

        return rasterizeQuaternion(shape, q, pivot);
    }

    // -------------------------------------------------------------------------
    // Exact 90°-step transforms (zero floating-point error)
    // -------------------------------------------------------------------------

    /**
     * CW rotation in the XZ plane (Y-axis).
     * Matrix columns: (a,c) for X input, (b,d) for Z input.
     */
    private static VoxelShape transformExactY(VoxelShape shape, Vec3 pivot, int a, int b, int c, int d) {
        List<AABB> result = new ArrayList<>();
        double px = pivot.x, pz = pivot.z;
        for (AABB box : shape.toAabbs()) {
            double dx0 = box.minX - px, dz0 = box.minZ - pz;
            double dx1 = box.maxX - px, dz1 = box.maxZ - pz;
            double nx0 = px + a * dx0 + b * dz0, nz0 = pz + c * dx0 + d * dz0;
            double nx1 = px + a * dx1 + b * dz1, nz1 = pz + c * dx1 + d * dz1;
            result.add(new AABB(
                    Math.min(nx0, nx1), box.minY, Math.min(nz0, nz1),
                    Math.max(nx0, nx1), box.maxY, Math.max(nz0, nz1)
            ));
        }
        return fromAabbs(result);
    }

    /**
     * CW rotation in the YZ plane (X-axis).
     * (a,b,c,d) maps: ny = a*dy + b*dz, nz = c*dy + d*dz
     */
    private static VoxelShape transformExactX(VoxelShape shape, Vec3 pivot, int a, int b, int c, int d) {
        List<AABB> result = new ArrayList<>();
        double py = pivot.y, pz = pivot.z;
        for (AABB box : shape.toAabbs()) {
            double dy0 = box.minY - py, dz0 = box.minZ - pz;
            double dy1 = box.maxY - py, dz1 = box.maxZ - pz;
            double ny0 = py + a * dy0 + b * dz0, nz0 = pz + c * dy0 + d * dz0;
            double ny1 = py + a * dy1 + b * dz1, nz1 = pz + c * dy1 + d * dz1;
            result.add(new AABB(
                    box.minX, Math.min(ny0, ny1), Math.min(nz0, nz1),
                    box.maxX, Math.max(ny0, ny1), Math.max(nz0, nz1)
            ));
        }
        return fromAabbs(result);
    }

    /**
     * CW rotation in the XY plane (Z-axis).
     * (a,b,c,d) maps: nx = a*dx + b*dy, ny = c*dx + d*dy
     */
    private static VoxelShape transformExactZ(VoxelShape shape, Vec3 pivot, int a, int b, int c, int d) {
        List<AABB> result = new ArrayList<>();
        double px = pivot.x, py = pivot.y;
        for (AABB box : shape.toAabbs()) {
            double dx0 = box.minX - px, dy0 = box.minY - py;
            double dx1 = box.maxX - px, dy1 = box.maxY - py;
            double nx0 = px + a * dx0 + b * dy0, ny0 = py + c * dx0 + d * dy0;
            double nx1 = px + a * dx1 + b * dy1, ny1 = py + c * dx1 + d * dy1;
            result.add(new AABB(
                    Math.min(nx0, nx1), Math.min(ny0, ny1), box.minZ,
                    Math.max(nx0, nx1), Math.max(ny0, ny1), box.maxZ
            ));
        }
        return fromAabbs(result);
    }

    // -------------------------------------------------------------------------
    // Pixel-perfect rasterizers
    // -------------------------------------------------------------------------

    /**
     * Y-axis rasterizer (original implementation, XZ plane only).
     */
    private static VoxelShape rasterizeY(VoxelShape shape, double angleDeg, Vec3 pivot) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        double px = pivot.x, pz = pivot.z;
        List<AABB> boxes = shape.toAabbs();

        double rMinX = Double.MAX_VALUE, rMinZ = Double.MAX_VALUE;
        double rMaxX = -Double.MAX_VALUE, rMaxZ = -Double.MAX_VALUE;
        for (AABB box : boxes) {
            double dx0 = box.minX - px, dz0 = box.minZ - pz;
            double dx1 = box.maxX - px, dz1 = box.maxZ - pz;
            double[] rxs = {px + cos * dx0 + sin * dz0, px + cos * dx1 + sin * dz0, px + cos * dx1 + sin * dz1, px + cos * dx0 + sin * dz1};
            double[] rzs = {pz - sin * dx0 + cos * dz0, pz - sin * dx1 + cos * dz0, pz - sin * dx1 + cos * dz1, pz - sin * dx0 + cos * dz1};
            for (double x : rxs) {
                rMinX = Math.min(rMinX, x);
                rMaxX = Math.max(rMaxX, x);
            }
            for (double z : rzs) {
                rMinZ = Math.min(rMinZ, z);
                rMaxZ = Math.max(rMaxZ, z);
            }
        }

        int xStart = (int) Math.floor(rMinX / PIXEL), xEnd = (int) Math.ceil(rMaxX / PIXEL);
        int zStart = (int) Math.floor(rMinZ / PIXEL), zEnd = (int) Math.ceil(rMaxZ / PIXEL);
        List<AABB> accepted = new ArrayList<>();

        for (int xi = xStart; xi < xEnd; xi++) {
            double ddx = (xi * PIXEL + HALF_PIXEL) - px;
            for (int zi = zStart; zi < zEnd; zi++) {
                double ddz = (zi * PIXEL + HALF_PIXEL) - pz;
                // Inverse CW rotation = CCW = transpose
                double ox = px + cos * ddx - sin * ddz;
                double oz = pz + sin * ddx + cos * ddz;
                double px0 = xi * PIXEL, px1 = px0 + PIXEL;
                double pz0 = zi * PIXEL, pz1 = pz0 + PIXEL;
                for (AABB box : boxes) {
                    if (ox >= box.minX && ox <= box.maxX && oz >= box.minZ && oz <= box.maxZ) {
                        accepted.add(new AABB(px0, box.minY, pz0, px1, box.maxY, pz1));
                    }
                }
            }
        }
        return fromAabbs(accepted);
    }

    /**
     * X-axis rasterizer: rotates in the YZ plane, X coordinate is preserved.
     * Forward CW:  y' =  cos*dy + sin*dz,  z' = -sin*dy + cos*dz
     * Inverse:     y  =  cos*dy'- sin*dz', z  =  sin*dy'+ cos*dz'
     */
    private static VoxelShape rasterizeX(VoxelShape shape, double angleDeg, Vec3 pivot) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        double py = pivot.y, pz = pivot.z;
        List<AABB> boxes = shape.toAabbs();

        double rMinY = Double.MAX_VALUE, rMinZ = Double.MAX_VALUE;
        double rMaxY = -Double.MAX_VALUE, rMaxZ = -Double.MAX_VALUE;
        for (AABB box : boxes) {
            double dy0 = box.minY - py, dz0 = box.minZ - pz;
            double dy1 = box.maxY - py, dz1 = box.maxZ - pz;
            double[] rys = {py + cos * dy0 + sin * dz0, py + cos * dy1 + sin * dz0, py + cos * dy1 + sin * dz1, py + cos * dy0 + sin * dz1};
            double[] rzs = {pz - sin * dy0 + cos * dz0, pz - sin * dy1 + cos * dz0, pz - sin * dy1 + cos * dz1, pz - sin * dy0 + cos * dz1};
            for (double y : rys) {
                rMinY = Math.min(rMinY, y);
                rMaxY = Math.max(rMaxY, y);
            }
            for (double z : rzs) {
                rMinZ = Math.min(rMinZ, z);
                rMaxZ = Math.max(rMaxZ, z);
            }
        }

        int yStart = (int) Math.floor(rMinY / PIXEL), yEnd = (int) Math.ceil(rMaxY / PIXEL);
        int zStart = (int) Math.floor(rMinZ / PIXEL), zEnd = (int) Math.ceil(rMaxZ / PIXEL);
        List<AABB> accepted = new ArrayList<>();

        for (int yi = yStart; yi < yEnd; yi++) {
            double ddy = (yi * PIXEL + HALF_PIXEL) - py;
            for (int zi = zStart; zi < zEnd; zi++) {
                double ddz = (zi * PIXEL + HALF_PIXEL) - pz;
                double oy = py + cos * ddy - sin * ddz;
                double oz = pz + sin * ddy + cos * ddz;
                double py0 = yi * PIXEL, py1 = py0 + PIXEL;
                double pz0 = zi * PIXEL, pz1 = pz0 + PIXEL;
                for (AABB box : boxes) {
                    if (oy >= box.minY && oy <= box.maxY && oz >= box.minZ && oz <= box.maxZ) {
                        // X extents stay unchanged per column
                        accepted.add(new AABB(box.minX, py0, pz0, box.maxX, py1, pz1));
                    }
                }
            }
        }
        return fromAabbs(accepted);
    }

    /**
     * Z-axis rasterizer: rotates in the XY plane, Z coordinate is preserved.
     * Forward CW:  x' =  cos*dx + sin*dy,  y' = -sin*dx + cos*dy
     * Inverse:     x  =  cos*dx'- sin*dy', y  =  sin*dx'+ cos*dy'
     */
    private static VoxelShape rasterizeZ(VoxelShape shape, double angleDeg, Vec3 pivot) {
        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        double px = pivot.x, py = pivot.y;
        List<AABB> boxes = shape.toAabbs();

        double rMinX = Double.MAX_VALUE, rMinY = Double.MAX_VALUE;
        double rMaxX = -Double.MAX_VALUE, rMaxY = -Double.MAX_VALUE;
        for (AABB box : boxes) {
            double dx0 = box.minX - px, dy0 = box.minY - py;
            double dx1 = box.maxX - px, dy1 = box.maxY - py;
            double[] rxs = {px + cos * dx0 + sin * dy0, px + cos * dx1 + sin * dy0, px + cos * dx1 + sin * dy1, px + cos * dx0 + sin * dy1};
            double[] rys = {py - sin * dx0 + cos * dy0, py - sin * dx1 + cos * dy0, py - sin * dx1 + cos * dy1, py - sin * dx0 + cos * dy1};
            for (double x : rxs) {
                rMinX = Math.min(rMinX, x);
                rMaxX = Math.max(rMaxX, x);
            }
            for (double y : rys) {
                rMinY = Math.min(rMinY, y);
                rMaxY = Math.max(rMaxY, y);
            }
        }

        int xStart = (int) Math.floor(rMinX / PIXEL), xEnd = (int) Math.ceil(rMaxX / PIXEL);
        int yStart = (int) Math.floor(rMinY / PIXEL), yEnd = (int) Math.ceil(rMaxY / PIXEL);
        List<AABB> accepted = new ArrayList<>();

        for (int xi = xStart; xi < xEnd; xi++) {
            double ddx = (xi * PIXEL + HALF_PIXEL) - px;
            for (int yi = yStart; yi < yEnd; yi++) {
                double ddy = (yi * PIXEL + HALF_PIXEL) - py;
                double ox = px + cos * ddx - sin * ddy;
                double oy = py + sin * ddx + cos * ddy;
                double px0 = xi * PIXEL, px1 = px0 + PIXEL;
                double py0 = yi * PIXEL, py1 = py0 + PIXEL;
                for (AABB box : boxes) {
                    if (ox >= box.minX && ox <= box.maxX && oy >= box.minY && oy <= box.maxY) {
                        accepted.add(new AABB(px0, py0, box.minZ, px1, py1, box.maxZ));
                    }
                }
            }
        }
        return fromAabbs(accepted);
    }

    /**
     * General quaternion rasterizer — scans all three axes.
     */
    private static VoxelShape rasterizeQuaternion(VoxelShape shape, Quaternionf q, Vec3 pivot) {
        Quaternionf qInv = new Quaternionf(q).conjugate();
        List<AABB> boxes = shape.toAabbs();
        double px = pivot.x, py = pivot.y, pz = pivot.z;

        double rMinX = Double.MAX_VALUE, rMinY = Double.MAX_VALUE, rMinZ = Double.MAX_VALUE;
        double rMaxX = -Double.MAX_VALUE, rMaxY = -Double.MAX_VALUE, rMaxZ = -Double.MAX_VALUE;
        for (AABB box : boxes) {
            double[] xs = {box.minX, box.maxX}, ys = {box.minY, box.maxY}, zs = {box.minZ, box.maxZ};
            for (double cx : xs)
                for (double cy : ys)
                    for (double cz : zs) {
                        Vector3f v = new Vector3f((float) (cx - px), (float) (cy - py), (float) (cz - pz));
                        v.rotate(q);
                        rMinX = Math.min(rMinX, px + v.x);
                        rMaxX = Math.max(rMaxX, px + v.x);
                        rMinY = Math.min(rMinY, py + v.y);
                        rMaxY = Math.max(rMaxY, py + v.y);
                        rMinZ = Math.min(rMinZ, pz + v.z);
                        rMaxZ = Math.max(rMaxZ, pz + v.z);
                    }
        }

        int xStart = (int) Math.floor(rMinX / PIXEL), xEnd = (int) Math.ceil(rMaxX / PIXEL);
        int yStart = (int) Math.floor(rMinY / PIXEL), yEnd = (int) Math.ceil(rMaxY / PIXEL);
        int zStart = (int) Math.floor(rMinZ / PIXEL), zEnd = (int) Math.ceil(rMaxZ / PIXEL);
        List<AABB> accepted = new ArrayList<>();

        for (int xi = xStart; xi < xEnd; xi++) {
            double ddx = (xi * PIXEL + HALF_PIXEL) - px;
            for (int yi = yStart; yi < yEnd; yi++) {
                double ddy = (yi * PIXEL + HALF_PIXEL) - py;
                for (int zi = zStart; zi < zEnd; zi++) {
                    double ddz = (zi * PIXEL + HALF_PIXEL) - pz;
                    Vector3f orig = new Vector3f((float) ddx, (float) ddy, (float) ddz);
                    orig.rotate(qInv);
                    double ox = px + orig.x, oy = py + orig.y, oz = pz + orig.z;
                    double px0 = xi * PIXEL, px1 = px0 + PIXEL;
                    double py0 = yi * PIXEL, py1 = py0 + PIXEL;
                    double pz0 = zi * PIXEL, pz1 = pz0 + PIXEL;
                    for (AABB box : boxes) {
                        if (ox >= box.minX && ox <= box.maxX
                                && oy >= box.minY && oy <= box.maxY
                                && oz >= box.minZ && oz <= box.maxZ) {
                            accepted.add(new AABB(px0, py0, pz0, px1, py1, pz1));
                        }
                    }
                }
            }
        }
        return fromAabbs(accepted);
    }

    // -------------------------------------------------------------------------
    // Shared utilities
    // -------------------------------------------------------------------------

    private static VoxelShape fromAabbs(List<AABB> aabbs) {
        if (aabbs.isEmpty()) return Shapes.empty();
        VoxelShape result = Shapes.empty();
        for (AABB a : aabbs)
            result = Shapes.joinUnoptimized(result, Shapes.create(a), BooleanOp.OR);
        return result.optimize();
    }

    public static Vec3 applyMatrix(Vec3 v, float[][] m, Vec3 pivot) {
        double dx = v.x - pivot.x, dy = v.y - pivot.y, dz = v.z - pivot.z;
        return new Vec3(
                pivot.x + m[0][0] * dx + m[0][1] * dy + m[0][2] * dz,
                pivot.y + m[1][0] * dx + m[1][1] * dy + m[1][2] * dz,
                pivot.z + m[2][0] * dx + m[2][1] * dy + m[2][2] * dz
        );
    }

    public static float[][] composeMatrix(float yawDeg, float pitchDeg, float rollDeg) {
        double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg), roll = Math.toRadians(rollDeg);
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch), cr = Math.cos(roll), sr = Math.sin(roll);
        double[][] ry = {{cy, 0, sy}, {0, 1, 0}, {-sy, 0, cy}};
        double[][] rx = {{1, 0, 0}, {0, cp, -sp}, {0, sp, cp}};
        double[][] rz = {{cr, -sr, 0}, {sr, cr, 0}, {0, 0, 1}};
        double[][] d = multiplyD(ry, multiplyD(rx, rz));
        return new float[][]{{(float) d[0][0], (float) d[0][1], (float) d[0][2]}, {(float) d[1][0], (float) d[1][1], (float) d[1][2]}, {(float) d[2][0], (float) d[2][1], (float) d[2][2]}};
    }

    public static float[][] transposeMatrix(float[][] m) {
        return new float[][]{{m[0][0], m[1][0], m[2][0]}, {m[0][1], m[1][1], m[2][1]}, {m[0][2], m[1][2], m[2][2]}};
    }

    private static double[][] multiplyD(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
        return r;
    }

    private static double normalizeAngle(float deg) {
        double a = deg % 360.0;
        return a < 0 ? a + 360.0 : a;
    }

    private static boolean isNear(double a, double target) {
        return Math.abs(a - target) < ANGLE_EPSILON;
    }

    private static boolean isNearZero(float v) {
        return Math.abs(v) < ANGLE_EPSILON;
    }
}