package de.mrjulsen.trafficcraft.block.data.attachments;

import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.TrafficSignPostBlock;
import de.mrjulsen.trafficcraft.registry.ModItemTags;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractPostAttachment<T extends IPostAttachment<T>> implements IPostAttachment<T> {

    private static final String NBT_DATA = "Data";
    private static final String NBT_ROTATION = "Rotation";

    private final PostAttachmentRegistry.PostAttachmentContext<?> context;
    private float rotation;

    public AbstractPostAttachment(PostAttachmentRegistry.PostAttachmentContext<?> context) {
        this.context = context;
    }

    public final <B extends BlockEntity & IAttachableBlock> B getBlockEntity() {
        return (B)context.blockEntity();
    }

    @Override
    public final Direction getDirection() {
        return context.direction();
    }

    @Override
    public final float getRotation() {
        return rotation;
    }

    public final void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public final void updateModel() {
        Level level = getBlockEntity().getLevel();
        if (level != null) {
            BlockState state = getBlockEntity().getBlockState();
            BlockPos pos = getBlockEntity().getBlockPos();
            level.setBlock(pos, state, Block.UPDATE_ALL);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(ModItemTags.WRENCHES)) {
            Direction dir = getDirection();
            float rotation = getRotation();
            Direction.Axis axis = getBlockEntity().getBlockState().getValue(TrafficSignPostBlock.AXIS);
            Vec3 hitVec = hit.getLocation().subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            float dirYaw = switch (dir) {
                case WEST -> 90f;
                case SOUTH -> 180f;
                case EAST -> 270f;
                default -> 0f;
            };
            float totalYaw = dirYaw + rotation;

            double rad = Math.toRadians(totalYaw);
            double cos = Math.cos(rad);
            double sin = -Math.sin(rad);

            double local = switch (axis) {
                case Y -> cos * hitVec.x + sin * hitVec.z;
                case X, Z -> -hitVec.y;
            };

            if (!level.isClientSide()) {
                float newRot;
                if (local < 0) {
                    newRot = rotation + 22.5f;
                } else {
                    newRot = rotation - 22.5f;
                }
                newRot = MathUtils.clamp(newRot, -45f, 45f);
                setRotation(newRot);
                updateModel();
            }
            return InteractionResult.SUCCESS;
        }

        return IPostAttachment.super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public final CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat(NBT_ROTATION, rotation);
        nbt.put(NBT_DATA, saveAdditional());
        return nbt;
    }

    @Override
    public final void deserializeNbt(CompoundTag nbt) {
        this.rotation = nbt.getFloat(NBT_ROTATION);
        loadAdditional(nbt.getCompound(NBT_DATA));
    }

    protected CompoundTag saveAdditional() {
        return new CompoundTag();
    }

    protected void loadAdditional(CompoundTag nbt) {
    }
}
