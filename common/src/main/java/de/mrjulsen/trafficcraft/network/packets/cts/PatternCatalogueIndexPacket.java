package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PatternCatalogueIndexPacket extends NetworkPacketData {

    private static final String NBT_DATA = "Data";

    private int index;

    public PatternCatalogueIndexPacket(DLStatus status) {
        super(status);
    }

    public PatternCatalogueIndexPacket(int index) {
        super(DLStatus.OK);
        this.index = index;
    }

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putInt(NBT_DATA, index);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.index = nbt.getInt(NBT_DATA);
    }


    public static void handle(PatternCatalogueIndexPacket packet, NetworkPacketContext context) {        
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.getMainHandItem().getItem() instanceof PatternCatalogueItem) {
            PatternCatalogueItem.setSelectedIndex(sender.getMainHandItem(), packet.index);
            if (sender.getMainHandItem().getItem() instanceof CreativePatternCatalogueItem) {
                CreativePatternCatalogueItem.clearCustomImage(sender.getMainHandItem());
            }
        } else if (sender.getOffhandItem().getItem() instanceof PatternCatalogueItem) { 
            PatternCatalogueItem.setSelectedIndex(sender.getOffhandItem(), packet.index);
            if (sender.getOffhandItem().getItem() instanceof CreativePatternCatalogueItem) { 
                CreativePatternCatalogueItem.clearCustomImage(sender.getOffhandItem());
            }
        }
        sender.getInventory().setChanged();
    }
}
