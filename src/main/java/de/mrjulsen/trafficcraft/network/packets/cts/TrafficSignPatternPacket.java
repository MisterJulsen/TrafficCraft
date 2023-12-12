package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.IPacketBase;
import de.mrjulsen.mcdragonlib.network.NetworkManagerBase;
import de.mrjulsen.mcdragonlib.utils.Utils;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.client.screen.menu.TrafficSignWorkbenchMenu;
import de.mrjulsen.trafficcraft.data.TrafficSignData;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NewNetworkManager;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignWorkbenchUpdateClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TrafficSignPatternPacket implements IPacketBase<TrafficSignPatternPacket> {
    
    private TrafficSignData data;
    private int index;

    public TrafficSignPatternPacket() {}

    /**
     * @param data TrafficSign data.
     * @param index The index of the slot you want to replace or -1 to create a new pattern.
     */
    public TrafficSignPatternPacket(TrafficSignData data, int index) {
        this.index = index;
        this.data = data;
    }

    @Override
    public void encode(TrafficSignPatternPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.index);
        packet.data.toBytes(buffer);
    }

    @Override
    public TrafficSignPatternPacket decode(FriendlyByteBuf buffer) {
        int index = buffer.readInt();
        TrafficSignData data = TrafficSignData.fromBytes(buffer);

        return new TrafficSignPatternPacket(data, index);
    }

    @Override
    public void handle(TrafficSignPatternPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkManagerBase.handlePacket(packet, context, () -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof TrafficSignWorkbenchMenu menu) {
                final ItemStack stack = menu.patternSlot.getItem();
                if (!(stack.getItem() instanceof PatternCatalogueItem))
                    return;

                if (packet.index >= 0) {
                    PatternCatalogueItem.replacePattern(stack, packet.data, packet.index);
                } else {
                    PatternCatalogueItem.setPattern(stack, packet.data);
                }
                menu.patternSlot.set(stack);
                menu.patternSlot.setChanged();
                menu.broadcastChanges();

                Utils.giveAdvancement(sender, ModMain.MOD_ID, "create_traffic_sign_pattern", "requirement");

                NewNetworkManager.getInstance().send(new TrafficSignWorkbenchUpdateClientPacket(), sender);
            }
        });
    }

    @Override
    public NetworkDirection getDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }
}
