package de.mrjulsen.trafficcraft.block.entity;

import com.google.common.collect.ImmutableMap;
import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.client.model.ICustomModelBlockEntity;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.TrafficSignPostBlock;
import de.mrjulsen.trafficcraft.block.data.attachments.IAttachableBlockEntity;
import de.mrjulsen.trafficcraft.block.data.attachments.IPostAttachment;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PostBlockEntity extends DLSyncedBlockEntity implements ICustomModelBlockEntity, IAttachableBlockEntity {

    public record AttachmentModelData(IPostAttachment<?> attachment, Quaternionf rotation) {}

    public static final ModelContext.ModelProperty<AttachmentModelData[]> PROPERTY_ATTACHMENTS = new ModelContext.ModelProperty<>();

    private static final String NBT_ATTACHMENTS = "Attachments";

    private final Map<Direction, IPostAttachment<?>> attachments = new ConcurrentHashMap<>();


    public PostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public PostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POST.get(), pos, state);
    }

    @Override
    public Map<Direction, IPostAttachment<?>> getAttachments() {
        return attachments;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        NbtUtils.putMap(tag, NBT_ATTACHMENTS, attachments, Direction::getName, v -> v.getRegistryType().wrap(v));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Map<Direction, IPostAttachment<?>> data = Utils.getMapWithKey(tag, NBT_ATTACHMENTS, Direction::byName, (v, k) -> PostAttachmentRegistry.load(v, new PostAttachmentRegistry.PostAttachmentContext<>(this, k)).orElse(null));

        this.attachments.clear();
        for (Map.Entry<Direction, IPostAttachment<?>> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                this.attachments.put(entry.getKey(), entry.getValue());
            }
        }
    }


    @Override
    public ModelContext getModelContext() {
        List<AttachmentModelData> attachmentsList = new ArrayList<>(this.attachments.size());

        Direction.Axis postAxis = Direction.Axis.Y;
        if (level.getBlockState(worldPosition).getBlock() instanceof TrafficSignPostBlock) {
            postAxis = level.getBlockState(worldPosition).getValue(TrafficSignPostBlock.AXIS);
        }

        for (Map.Entry<Direction, IPostAttachment<?>> entry : this.attachments.entrySet()) {
            attachmentsList.add(new AttachmentModelData(entry.getValue(), getAttachmentRotation(entry.getValue(), postAxis)));
        }
        return ModelContext.builder()
                .with(PROPERTY_ATTACHMENTS, attachmentsList.toArray(new AttachmentModelData[0]))
                .build();
    }

    public Quaternionf getAttachmentRotation(IPostAttachment<?> attachment, Direction.Axis postAxis) {
        Direction realDirection = attachment.getDirection().getAxis() == Direction.Axis.Z ? attachment.getDirection().getOpposite() : attachment.getDirection();

        Quaternionf extraRot = new Quaternionf();
        if (attachment.getDirection().getAxis() != postAxis) {
            switch (postAxis) {
                case X, Z -> extraRot.rotateX((float) Math.toRadians(attachment.getRotation()));
                default -> extraRot.rotateY((float) Math.toRadians(attachment.getRotation()));
            }
        }

        Quaternionf yRot = new Quaternionf();
        yRot.rotateY((float) Math.toRadians(realDirection.toYRot()));
        yRot.mul(extraRot);
        return yRot;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (IPostAttachment<?> attachment : this.attachments.values()) {
            attachment.onRemoved(getLevel(), getBlockPos());
        }
    }
}
