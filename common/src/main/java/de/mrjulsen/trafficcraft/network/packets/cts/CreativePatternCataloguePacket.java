package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class CreativePatternCataloguePacket extends NetworkPacketData {

    private static final String NBT_DATA = "Data";
    
    private NamedTrafficSignTextureReference data;

    public CreativePatternCataloguePacket(DLStatus status) {
        super(status);
    }

    public CreativePatternCataloguePacket(NamedTrafficSignTextureReference data) {
        super(DLStatus.OK);
        this.data = data;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.put(NBT_DATA, data.toNbt());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.data = NamedTrafficSignTextureReference.fromNbt(nbt.getCompound(NBT_DATA));
    }

    public static void handle(CreativePatternCataloguePacket packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
            CreativePatternCatalogueItem.setCustomImage(sender.getMainHandItem(), packet.data);
            CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
        } else if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
            CreativePatternCatalogueItem.setCustomImage(sender.getOffhandItem(), packet.data);
            CreativePatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), -1);
        }
        sender.getInventory().setChanged();
    }
}
