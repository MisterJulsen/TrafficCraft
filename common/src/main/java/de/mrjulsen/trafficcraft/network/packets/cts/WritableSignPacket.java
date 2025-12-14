package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.block.DLWritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public class WritableSignPacket extends NetworkPacketData {

    private static final String NBT_MESSAGES = "Messages";
    private static final String NBT_POS = "Pos";

    private String[] messages;
    private BlockPos pos;

    public WritableSignPacket(DLStatus status) {
        super(status);
    }

    public WritableSignPacket(BlockPos pos, String[] messages) {
        super(DLStatus.OK);
        this.pos = pos;
        this.messages = messages;
    }

    @Override
    protected void write(CompoundTag nbt) {
        ListTag msgs = new ListTag();
        for (String msg : messages) {
            msgs.add(StringTag.valueOf(msg));
        }
        nbt.put(NBT_MESSAGES, msgs);
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.messages = nbt.getList(NBT_MESSAGES, Tag.TAG_STRING).stream().map(x -> ((StringTag)x).getAsString()).toArray(String[]::new);
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
    }

    public static void handle(WritableSignPacket packet, NetworkPacketContext context) {
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.level().getBlockEntity(packet.pos) instanceof DLWritableSignBlockEntity blockEntity) {
            blockEntity.setTexts(packet.messages);
        }
    }
}
