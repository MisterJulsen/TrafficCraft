package de.mrjulsen.trafficcraft.network.packets.stc;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrafficSignTextureResetPacket extends NetworkPacketData {

    private static final String NBT_POS = "Pos";

    public BlockPos pos;

    public TrafficSignTextureResetPacket(DLStatus status) {
        super(status);
    }

    public TrafficSignTextureResetPacket(BlockPos pos) {
        super(DLStatus.OK);
        this.pos = pos;
    }

    @Override
    protected void write(CompoundTag nbt) {
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
    }

    public static void handle(TrafficSignTextureResetPacket packet, NetworkPacketContext context) {
        Player player = context.getPlayer();                
        Level level = player.level();
        BlockEntity entity = level.getBlockEntity(packet.pos);
        if (entity instanceof TrafficSignBlockEntity be) {
            be.resetTexture();
        }
    }
}
