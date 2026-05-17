package de.mrjulsen.trafficcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {

    default BlockGetter self() {
        return (BlockGetter)(Object)this;
    }

    @Shadow
    static <T, C> T traverseBlocks(Vec3 from, Vec3 to, C context, BiFunction<C, BlockPos, T> tester, Function<C, T> onFail) {
        throw new AssertionError();
    }

    @Shadow
    BlockState getBlockState(BlockPos pos);

    @Shadow
    FluidState getFluidState(BlockPos pos);

    @Shadow
    default BlockHitResult clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos, VoxelShape shape, BlockState state) {
        throw new AssertionError();
    }

    @Overwrite
    default BlockHitResult clip(ClipContext context) {
        return (BlockHitResult)traverseBlocks(context.getFrom(), context.getTo(), context, (clipContext, pos) -> {            
            BlockHitResult res = null;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos blockPos = pos.offset(x, 0, z);
                    BlockState blockState = this.getBlockState(blockPos);
                    FluidState fluidState = this.getFluidState(blockPos);
                    Vec3 vec3 = clipContext.getFrom();
                    Vec3 vec32 = clipContext.getTo();

                    VoxelShape voxelShape = clipContext.getBlockShape(blockState, (BlockGetter)(Object)this, blockPos);
                    BlockHitResult blockHitResult = this.clipWithInteractionOverride(vec3, vec32, blockPos, voxelShape, blockState);
                    VoxelShape voxelShape2 = clipContext.getFluidShape(fluidState, (BlockGetter)(Object)this, blockPos);
                    BlockHitResult blockHitResult2 = voxelShape2.clip(vec3, vec32, blockPos);
                    double a = res == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(res.getLocation());
                    double d = blockHitResult == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult.getLocation());
                    double e = blockHitResult2 == null ? Double.MAX_VALUE : clipContext.getFrom().distanceToSqr(blockHitResult2.getLocation());

                    res = a <= d ? (a <= e ? res : blockHitResult2) : (d <= e ? blockHitResult : blockHitResult2);
                }
            }
            return res;
        }, (clipContext) -> {
            Vec3 vec3 = clipContext.getFrom().subtract(clipContext.getTo());
            return BlockHitResult.miss(clipContext.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), new BlockPos((int)clipContext.getTo().x, (int)clipContext.getTo().y, (int)clipContext.getTo().z));
        });
    }
}
