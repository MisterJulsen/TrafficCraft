package de.mrjulsen.trafficcraft.network.packets.cts;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.NbtUtils;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.data.TownSignVariant;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class TownSignPacket extends NetworkPacketData {

    private static final String NBT_MESSAGES = "Messages";
    private static final String NBT_VARIANT = "Variant";
    private static final String NBT_POS = "Pos";
    private static final String NBT_SIDE = "Side";

    private String[] messages;
    private TownSignVariant variant;
    private BlockPos pos;    
    private TownSignBlock.ETownSignSide side;

    public TownSignPacket(DLStatus status) {
        super(status);
    }

    public TownSignPacket(BlockPos pos, String[] messages, TownSignVariant variant, TownSignBlock.ETownSignSide side) {
        super(DLStatus.OK);
        this.pos = pos;
        this.variant = variant;
        this.messages = messages;
        this.side = side;
    }

    @Override
    protected void write(CompoundTag nbt) {
        ListTag msgs = new ListTag();
        for (String msg : messages) {
            msgs.add(StringTag.valueOf(msg));
        }
        nbt.put(NBT_MESSAGES, msgs);
        nbt.putInt(NBT_VARIANT, variant.getIndex());
        NbtUtils.putNbtPos(nbt, NBT_POS, pos);
        nbt.putInt(NBT_SIDE, side.getIndex());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.messages = nbt.getList(NBT_MESSAGES, Tag.TAG_STRING).stream().map(x -> ((StringTag)x).getAsString()).toArray(String[]::new);
        this.variant = TownSignVariant.getVariantByIndex(nbt.getInt(NBT_VARIANT));
        this.pos = NbtUtils.getNbtBlockPos(nbt, NBT_POS);
        this.side = TownSignBlock.ETownSignSide.getSideByIndex(nbt.getInt(NBT_SIDE));
    }
    
    public static void handle(TownSignPacket packet, NetworkPacketContext context) {        
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
        if (sender.level().getBlockState(packet.pos).getBlock() instanceof TownSignBlock && sender.level().getBlockEntity(packet.pos) instanceof TownSignBlockEntity blockEntity) {
            switch (packet.side) {
                case BACK:
                    blockEntity.setBackTexts(packet.messages);
                    break;
                default:
                case FRONT:
                    blockEntity.setTexts(packet.messages);
                    break;
            }
            BlockState state = sender.level().getBlockState(packet.pos);
            sender.level().setBlockAndUpdate(packet.pos, state.setValue(TownSignBlock.VARIANT, packet.variant));
        }
    }
}
