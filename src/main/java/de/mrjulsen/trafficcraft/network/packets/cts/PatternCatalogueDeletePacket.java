package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PatternCatalogueDeletePacket implements IPacketBase<PatternCatalogueDeletePacket>{
    private int index;

    public PatternCatalogueDeletePacket() {}

    public PatternCatalogueDeletePacket(int index) {
        this.index = index;
    }

    @Override
    public void encode(PatternCatalogueDeletePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
    }

    @Override
    public PatternCatalogueDeletePacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();

        return new PatternCatalogueDeletePacket(index);
    }

    @Override
    public void handle(PatternCatalogueDeletePacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem))
                    return;

                PatternCatalogueItem.removePatternAt(stack, packet.index);
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();
                
                NetworkManager.getInstance().send(new TrafficSignWorkbenchUpdateClientPacket(), sender);
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
