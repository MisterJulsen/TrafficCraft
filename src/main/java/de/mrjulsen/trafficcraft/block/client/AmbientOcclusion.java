package de.mrjulsen.trafficcraft.block.client;

import java.util.BitSet;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @see {@code ModelBlockRenderer.class}
 */
@OnlyIn(Dist.CLIENT)
public class AmbientOcclusion {
    static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);
    
    final float[] brightness = new float[4];
    final int[] lightmap = new int[4];

    public AmbientOcclusion() {
    }

    public void calculate(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, Direction pDirection,
            float[] pShape, BitSet pShapeFlags, boolean pShade) {
        BlockPos blockpos = pShapeFlags.get(0) ? pPos.relative(pDirection) : pPos;
        AdjacencyInfo modelblockrenderer$adjacencyinfo = AdjacencyInfo.fromFacing(pDirection);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        Cache modelblockrenderer$cache = CACHE.get();
        blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]);
        BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
        int i = modelblockrenderer$cache.getLightColor(blockstate, pLevel, blockpos$mutableblockpos);
        float f = modelblockrenderer$cache.getShadeBrightness(blockstate, pLevel, blockpos$mutableblockpos);
        blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]);
        BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
        int j = modelblockrenderer$cache.getLightColor(blockstate1, pLevel, blockpos$mutableblockpos);
        float f1 = modelblockrenderer$cache.getShadeBrightness(blockstate1, pLevel, blockpos$mutableblockpos);
        blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]);
        BlockState blockstate2 = pLevel.getBlockState(blockpos$mutableblockpos);
        int k = modelblockrenderer$cache.getLightColor(blockstate2, pLevel, blockpos$mutableblockpos);
        float f2 = modelblockrenderer$cache.getShadeBrightness(blockstate2, pLevel, blockpos$mutableblockpos);
        blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]);
        BlockState blockstate3 = pLevel.getBlockState(blockpos$mutableblockpos);
        int l = modelblockrenderer$cache.getLightColor(blockstate3, pLevel, blockpos$mutableblockpos);
        float f3 = modelblockrenderer$cache.getShadeBrightness(blockstate3, pLevel, blockpos$mutableblockpos);
        BlockState blockstate4 = pLevel.getBlockState(blockpos$mutableblockpos
                .setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]).move(pDirection));
        boolean flag = !blockstate4.isViewBlocking(pLevel, blockpos$mutableblockpos)
                || blockstate4.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
        BlockState blockstate5 = pLevel.getBlockState(blockpos$mutableblockpos
                .setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]).move(pDirection));
        boolean flag1 = !blockstate5.isViewBlocking(pLevel, blockpos$mutableblockpos)
                || blockstate5.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
        BlockState blockstate6 = pLevel.getBlockState(blockpos$mutableblockpos
                .setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]).move(pDirection));
        boolean flag2 = !blockstate6.isViewBlocking(pLevel, blockpos$mutableblockpos)
                || blockstate6.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
        BlockState blockstate7 = pLevel.getBlockState(blockpos$mutableblockpos
                .setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]).move(pDirection));
        boolean flag3 = !blockstate7.isViewBlocking(pLevel, blockpos$mutableblockpos)
                || blockstate7.getLightBlock(pLevel, blockpos$mutableblockpos) == 0;
        float f4;
        int i1;
        if (!flag2 && !flag) {
            f4 = f;
            i1 = i;
        } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0])
                    .move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate8 = pLevel.getBlockState(blockpos$mutableblockpos);
            f4 = modelblockrenderer$cache.getShadeBrightness(blockstate8, pLevel, blockpos$mutableblockpos);
            i1 = modelblockrenderer$cache.getLightColor(blockstate8, pLevel, blockpos$mutableblockpos);
        }

        float f5;
        int j1;
        if (!flag3 && !flag) {
            f5 = f;
            j1 = i;
        } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0])
                    .move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate10 = pLevel.getBlockState(blockpos$mutableblockpos);
            f5 = modelblockrenderer$cache.getShadeBrightness(blockstate10, pLevel, blockpos$mutableblockpos);
            j1 = modelblockrenderer$cache.getLightColor(blockstate10, pLevel, blockpos$mutableblockpos);
        }

        float f6;
        int k1;
        if (!flag2 && !flag1) {
            f6 = f;
            k1 = i;
        } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1])
                    .move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate11 = pLevel.getBlockState(blockpos$mutableblockpos);
            f6 = modelblockrenderer$cache.getShadeBrightness(blockstate11, pLevel, blockpos$mutableblockpos);
            k1 = modelblockrenderer$cache.getLightColor(blockstate11, pLevel, blockpos$mutableblockpos);
        }

        float f7;
        int l1;
        if (!flag3 && !flag1) {
            f7 = f;
            l1 = i;
        } else {
            blockpos$mutableblockpos.setWithOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1])
                    .move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate12 = pLevel.getBlockState(blockpos$mutableblockpos);
            f7 = modelblockrenderer$cache.getShadeBrightness(blockstate12, pLevel, blockpos$mutableblockpos);
            l1 = modelblockrenderer$cache.getLightColor(blockstate12, pLevel, blockpos$mutableblockpos);
        }

        int i3 = modelblockrenderer$cache.getLightColor(pState, pLevel, pPos);
        blockpos$mutableblockpos.setWithOffset(pPos, pDirection);
        BlockState blockstate9 = pLevel.getBlockState(blockpos$mutableblockpos);
        if (pShapeFlags.get(0) || !blockstate9.isSolidRender(pLevel, blockpos$mutableblockpos)) {
            i3 = modelblockrenderer$cache.getLightColor(blockstate9, pLevel, blockpos$mutableblockpos);
        }

        float f8 = pShapeFlags.get(0)
                ? modelblockrenderer$cache.getShadeBrightness(pLevel.getBlockState(blockpos), pLevel, blockpos)
                : modelblockrenderer$cache.getShadeBrightness(pLevel.getBlockState(pPos), pLevel, pPos);
        AmbientVertexRemap modelblockrenderer$ambientvertexremap = AmbientVertexRemap.fromFacing(pDirection);
        if (pShapeFlags.get(1) && modelblockrenderer$adjacencyinfo.doNonCubicWeight) {
            float f29 = (f3 + f + f5 + f8) * 0.25F;
            float f31 = (f2 + f + f4 + f8) * 0.25F;
            float f32 = (f2 + f1 + f6 + f8) * 0.25F;
            float f33 = (f3 + f1 + f7 + f8) * 0.25F;
            float f13 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[0].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[1].shape];
            float f14 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[2].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[3].shape];
            float f15 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[4].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[5].shape];
            float f16 = pShape[modelblockrenderer$adjacencyinfo.vert0Weights[6].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert0Weights[7].shape];
            float f17 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[0].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[1].shape];
            float f18 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[2].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[3].shape];
            float f19 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[4].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[5].shape];
            float f20 = pShape[modelblockrenderer$adjacencyinfo.vert1Weights[6].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert1Weights[7].shape];
            float f21 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[0].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[1].shape];
            float f22 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[2].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[3].shape];
            float f23 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[4].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[5].shape];
            float f24 = pShape[modelblockrenderer$adjacencyinfo.vert2Weights[6].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert2Weights[7].shape];
            float f25 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[0].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[1].shape];
            float f26 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[2].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[3].shape];
            float f27 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[4].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[5].shape];
            float f28 = pShape[modelblockrenderer$adjacencyinfo.vert3Weights[6].shape]
                    * pShape[modelblockrenderer$adjacencyinfo.vert3Weights[7].shape];
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f29 * f13 + f31 * f14 + f32 * f15
                    + f33 * f16;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f29 * f17 + f31 * f18 + f32 * f19
                    + f33 * f20;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f29 * f21 + f31 * f22 + f32 * f23
                    + f33 * f24;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f29 * f25 + f31 * f26 + f32 * f27
                    + f33 * f28;
            int i2 = this.blend(l, i, j1, i3);
            int j2 = this.blend(k, i, i1, i3);
            int k2 = this.blend(k, j, k1, i3);
            int l2 = this.blend(l, j, l1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(i2, j2, k2, l2, f13, f14, f15, f16);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(i2, j2, k2, l2, f17, f18, f19, f20);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(i2, j2, k2, l2, f21, f22, f23, f24);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(i2, j2, k2, l2, f25, f26, f27, f28);
        } else {
            float f9 = (f3 + f + f5 + f8) * 0.25F;
            float f10 = (f2 + f + f4 + f8) * 0.25F;
            float f11 = (f2 + f1 + f6 + f8) * 0.25F;
            float f12 = (f3 + f1 + f7 + f8) * 0.25F;
            this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(l, i, j1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(k, i, i1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(k, j, k1, i3);
            this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(l, j, l1, i3);
            this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f9;
            this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f10;
            this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f11;
            this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f12;
        }

        float f30 = pLevel.getShade(pDirection, pShade);

        for (int j3 = 0; j3 < this.brightness.length; ++j3) {
            this.brightness[j3] *= f30;
        }

    }

    /**
     * @return the ambient occlusion light color
     */
    private int blend(int pLightColor0, int pLightColor1, int pLightColor2, int pLightColor3) {
        if (pLightColor0 == 0) {
            pLightColor0 = pLightColor3;
        }

        if (pLightColor1 == 0) {
            pLightColor1 = pLightColor3;
        }

        if (pLightColor2 == 0) {
            pLightColor2 = pLightColor3;
        }

        return pLightColor0 + pLightColor1 + pLightColor2 + pLightColor3 >> 2 & 16711935;
    }

    private int blend(int pBrightness0, int pBrightness1, int pBrightness2, int pBrightness3, float pWeight0,
            float pWeight1, float pWeight2, float pWeight3) {
        int i = (int) ((float) (pBrightness0 >> 16 & 255) * pWeight0 + (float) (pBrightness1 >> 16 & 255) * pWeight1
                + (float) (pBrightness2 >> 16 & 255) * pWeight2 + (float) (pBrightness3 >> 16 & 255) * pWeight3) & 255;
        int j = (int) ((float) (pBrightness0 & 255) * pWeight0 + (float) (pBrightness1 & 255) * pWeight1
                + (float) (pBrightness2 & 255) * pWeight2 + (float) (pBrightness3 & 255) * pWeight3) & 255;
        return i << 16 | j;
    }

    @OnlyIn(Dist.CLIENT)
    protected static enum AdjacencyInfo {
        DOWN(new Direction[] { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH }, 0.5F, true,
                new SizeInfo[] { SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH,
                        SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH },
                new SizeInfo[] { SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH,
                        SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH,
                        SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH,
                        SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH }),
        UP(new Direction[] { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH }, 1.0F, true,
                new SizeInfo[] { SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST,
                        SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH },
                new SizeInfo[] { SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST,
                        SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST,
                        SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST,
                        SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH }),
        NORTH(new Direction[] { Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST }, 0.8F, true,
                new SizeInfo[] { SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP,
                        SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST },
                new SizeInfo[] { SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP,
                        SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN,
                        SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN,
                        SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST }),
        SOUTH(new Direction[] { Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP }, 0.8F, true,
                new SizeInfo[] { SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST,
                        SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST,
                        SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST,
                        SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST },
                new SizeInfo[] { SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST,
                        SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST }),
        WEST(new Direction[] { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH }, 0.6F, true,
                new SizeInfo[] { SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP,
                        SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH },
                new SizeInfo[] { SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP,
                        SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN,
                        SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN,
                        SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH }),
        EAST(new Direction[] { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH }, 0.6F, true,
                new SizeInfo[] { SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH,
                        SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH },
                new SizeInfo[] { SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH,
                        SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP,
                        SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH },
                new SizeInfo[] { SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP,
                        SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH });

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final SizeInfo[] vert0Weights;
        final SizeInfo[] vert1Weights;
        final SizeInfo[] vert2Weights;
        final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING = Util.make(new AdjacencyInfo[6], (p_111134_) -> {
            p_111134_[Direction.DOWN.get3DDataValue()] = DOWN;
            p_111134_[Direction.UP.get3DDataValue()] = UP;
            p_111134_[Direction.NORTH.get3DDataValue()] = NORTH;
            p_111134_[Direction.SOUTH.get3DDataValue()] = SOUTH;
            p_111134_[Direction.WEST.get3DDataValue()] = WEST;
            p_111134_[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AdjacencyInfo(Direction[] pCorners, float pShadeBrightness, boolean pDoNonCubicWeight,
                SizeInfo[] pVert0Weights, SizeInfo[] pVert1Weights, SizeInfo[] pVert2Weights,
                SizeInfo[] pVert3Weights) {
            this.corners = pCorners;
            this.doNonCubicWeight = pDoNonCubicWeight;
            this.vert0Weights = pVert0Weights;
            this.vert1Weights = pVert1Weights;
            this.vert2Weights = pVert2Weights;
            this.vert3Weights = pVert3Weights;
        }

        public static AdjacencyInfo fromFacing(Direction pFacing) {
            return BY_FACING[pFacing.get3DDataValue()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final AmbientVertexRemap[] BY_FACING = Util.make(new AmbientVertexRemap[6], (p_111204_) -> {
            p_111204_[Direction.DOWN.get3DDataValue()] = DOWN;
            p_111204_[Direction.UP.get3DDataValue()] = UP;
            p_111204_[Direction.NORTH.get3DDataValue()] = NORTH;
            p_111204_[Direction.SOUTH.get3DDataValue()] = SOUTH;
            p_111204_[Direction.WEST.get3DDataValue()] = WEST;
            p_111204_[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AmbientVertexRemap(int pVert0, int pVert1, int pVert2, int pVert3) {
            this.vert0 = pVert0;
            this.vert1 = pVert1;
            this.vert2 = pVert2;
            this.vert3 = pVert3;
        }

        public static AmbientVertexRemap fromFacing(Direction pFacing) {
            return BY_FACING[pFacing.get3DDataValue()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected static enum SizeInfo {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private SizeInfo(Direction pDirection, boolean pFlip) {
            this.shape = pDirection.get3DDataValue() + (pFlip ? Direction.values().length : 0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
                protected void rehash(int p_111238_) {
                }
            };
            long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
            return long2intlinkedopenhashmap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
                protected void rehash(int p_111245_) {
                }
            };
            long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
            return long2floatlinkedopenhashmap;
        });

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
            long i = pPos.asLong();
            if (this.enabled) {
                int j = this.colorCache.get(i);
                if (j != Integer.MAX_VALUE) {
                    return j;
                }
            }

            int k = LevelRenderer.getLightColor(pLevel, pState, pPos);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }

                this.colorCache.put(i, k);
            }

            return k;
        }

        public float getShadeBrightness(BlockState pState, BlockAndTintGetter pLevel, BlockPos pPos) {
            long i = pPos.asLong();
            if (this.enabled) {
                float f = this.brightnessCache.get(i);
                if (!Float.isNaN(f)) {
                    return f;
                }
            }

            float f1 = pState.getShadeBrightness(pLevel, pPos);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }

                this.brightnessCache.put(i, f1);
            }

            return f1;
        }
    }
}
