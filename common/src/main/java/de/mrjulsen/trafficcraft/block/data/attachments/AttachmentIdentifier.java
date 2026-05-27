package de.mrjulsen.trafficcraft.block.data.attachments;

import de.mrjulsen.mcdragonlib.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public record AttachmentIdentifier(BlockPos pos, Direction side, ResourceLocation attachmentId) {
    public static final String NBT_POS = "Pos";
    public static final String NBT_SIDE = "Side";
    public static final String NBT_ID = "Id";

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        NbtUtils.putNbtPos(tag, NBT_POS, pos);
        tag.putString(NBT_SIDE, side.getName());
        tag.putString(NBT_ID, attachmentId.toString());
        return tag;
    }

    public static AttachmentIdentifier fromNbt(CompoundTag tag) {
        return new AttachmentIdentifier(
                NbtUtils.getNbtBlockPos(tag, NBT_POS),
                Direction.byName(tag.getString(NBT_SIDE)),
                ResourceLocation.tryParse(tag.getString(NBT_ID))
        );
    }

    public Optional<IPostAttachment<?>> getAttachment(Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof IAttachableBlockEntity be) {
            return Optional.ofNullable(be.getAttachment(side)
                    .map(a -> a.getRegistryType().id().equals(attachmentId) ? a : null)
                    .orElse(null));
        }
        return Optional.empty();
    }

    public <T extends IPostAttachment<T>> Optional<T> getAttachment(Class<T> type, Level level) {
        return getAttachment(level).map(a -> type.cast(a));
    }
}
