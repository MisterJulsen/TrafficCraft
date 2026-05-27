package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.block.data.attachments.AttachmentIdentifier;
import de.mrjulsen.trafficcraft.block.data.attachments.TrafficSignPostAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class UpdateTrafficSignShapePacket extends NetworkPacketData {

    private static final String NBT_DATA = "Data";
    private static final String NBT_SHAPE = "Shape";

    private AttachmentIdentifier key;
    private ResourceLocation shape;

    public UpdateTrafficSignShapePacket(DLStatus status) {
        super(status);
    }

    public UpdateTrafficSignShapePacket(AttachmentIdentifier key, ResourceLocation shape) {
        super(DLStatus.OK);
        this.key = key;
        this.shape = shape;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.put(NBT_DATA, key.toNbt());
        nbt.putString(NBT_SHAPE, shape.toString());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.key = AttachmentIdentifier.fromNbt(nbt.getCompound(NBT_DATA));
        this.shape = ResourceLocation.tryParse(nbt.getString(NBT_SHAPE));
    }
    
    public static void handle(UpdateTrafficSignShapePacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            packet.key.getAttachment(TrafficSignPostAttachment.class, context.getPlayer().level()).ifPresent(a -> {
                a.setModelLocation(packet.shape);
            });
        });
    }
}
