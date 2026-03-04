package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.trafficcraft.components.StreetLampComponent;
import de.mrjulsen.trafficcraft.item.StreetLampConfigCardItem;
import de.mrjulsen.trafficcraft.util.ETimeFormat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StreetLampConfigPacket extends NetworkPacketData {

    private static final String NBT_TURN_ON = "TurnOn";
    private static final String NBT_TURN_OFF = "TurnOff";
    private static final String NBT_TIME_FORMAT = "Format";

    private int turnOnTime;
    private int turnOffTime;
    private ETimeFormat timeFormat;

    public StreetLampConfigPacket(DLStatus status) {
        super(status);
    }

    public StreetLampConfigPacket(int turnOnTime, int turnOffTime, ETimeFormat timeFormat) {
        super(DLStatus.OK);
        this.turnOnTime = turnOnTime;
        this.turnOffTime = turnOffTime;
        this.timeFormat = timeFormat;
    }
    

    @Override
    protected void write(CompoundTag nbt) {
        nbt.putInt(NBT_TURN_ON, turnOnTime);
        nbt.putInt(NBT_TURN_OFF, turnOffTime);
        nbt.putInt(NBT_TIME_FORMAT, timeFormat.getIndex());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.turnOnTime = nbt.getInt(NBT_TURN_ON);
        this.turnOffTime = nbt.getInt(NBT_TURN_OFF);
        this.timeFormat = ETimeFormat.getByIndex(nbt.getInt(NBT_TIME_FORMAT));
    }
    
    public static void handle(StreetLampConfigPacket packet, NetworkPacketContext context) {
        context.queue(() -> {
            ServerPlayer sender = (ServerPlayer)context.getPlayer();
            ItemStack stack;

            if ((stack = sender.getMainHandItem()).getItem() instanceof StreetLampConfigCardItem item) {
                item.setComponent(stack, new StreetLampComponent(packet.turnOnTime, packet.turnOffTime, packet.timeFormat));
            } else if ((stack = sender.getOffhandItem()).getItem() instanceof StreetLampConfigCardItem item) {
                item.setComponent(stack, new StreetLampComponent(packet.turnOnTime, packet.turnOffTime, packet.timeFormat));
            }

            sender.getInventory().setChanged();
        });
    }
}
