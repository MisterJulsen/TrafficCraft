package de.mrjulsen.trafficcraft.network.packets;

import java.util.Map.Entry;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.common.Location;
import de.mrjulsen.mcdragonlib.utils.ScheduledTask;
import de.mrjulsen.trafficcraft.block.AsphaltSlope;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool.RoadBuildingData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

public class RoadBuilderBuildRoadPacket {

    private Location pos1;
    private Location pos2;
    private byte roadWidth;
    private boolean replaceBlocks;
    private RoadType roadType;
    
    public RoadBuilderBuildRoadPacket(Location pos1, Location pos2, byte roadWidth, boolean replaceBlocks, RoadType roadType) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.roadWidth = roadWidth;
        this.replaceBlocks = replaceBlocks;
        this.roadType = roadType;
    }

    public static void encode(RoadBuilderBuildRoadPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.pos1.toNbt());
        buffer.writeNbt(packet.pos2.toNbt());
        buffer.writeByte(packet.roadWidth);
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeEnum(packet.roadType);
    }

    public static RoadBuilderBuildRoadPacket decode(FriendlyByteBuf buffer) {
        Location pos1 = Location.fromNbt(buffer.readNbt());
        Location pos2 = Location.fromNbt(buffer.readNbt());
        byte roadWidth = buffer.readByte();
        boolean replaceBlocks = buffer.readBoolean();
        RoadType roadType = buffer.readEnum(RoadType.class);

        return new RoadBuilderBuildRoadPacket(pos1, pos2, roadWidth, replaceBlocks, roadType);
    }

    public static void handle(RoadBuilderBuildRoadPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            final Level level = sender.getLevel();
            ItemStack item = null;
            InteractionHand hand = null;

            if (sender.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RoadConstructionTool) {
                item = sender.getItemInHand(InteractionHand.MAIN_HAND);
                hand = InteractionHand.MAIN_HAND;
            } else if (sender.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof RoadConstructionTool) {
                item = sender.getItemInHand(InteractionHand.OFF_HAND);
                hand = InteractionHand.OFF_HAND;
            } else {
                return;
            }

            final RoadBuildingData buildingData = RoadConstructionTool.prepareRoadBuilding(
                level,
                sender,
                hand,
                item,
                packet.pos1.getLocationVec3(),
                packet.pos2.getLocationVec3(), 
                packet.roadWidth,
                packet.replaceBlocks,
                packet.roadType
            );

            ScheduledTask.create(buildingData, level, RoadConstructionTool.BUILD_DELAY_TICKS, buildingData.blocks.size(), (data, lvl, iteration) -> {
                boolean[] canContinue = new boolean[] { true };
                for (Entry<BlockPos, Integer> block : data.blocks.get(iteration).entrySet()) {
                    
                    if (!canContinue[0] || !data.player.isAlive()) {
                        return false;
                    }

                    if (!isPlayerCreative(data.player) && (data.player.getInventory().countItem(data.roadType.getSlope().asItem()) <= 0 && data.player.getInventory().countItem(data.roadType.getBlock().asItem()) <= 0)) {
                        return false;
                    }

                    if (level.getBlockState(block.getKey()).getBlock().defaultDestroyTime() != Block.INDESTRUCTIBLE) {
                        if (block.getValue() > 0 && block.getValue() <= 7 && (isPlayerCreative(data.player) || data.player.getInventory().countItem(data.roadType.getSlope().asItem()) > 0)) {                            
                            level.destroyBlock(block.getKey(), !isPlayerCreative(data.player));
                            level.setBlockAndUpdate(block.getKey(), data.roadType.getSlope().defaultBlockState().setValue(AsphaltSlope.LAYERS, Math.min(block.getValue(), isPlayerCreative(data.player) ? Integer.MAX_VALUE : data.player.getInventory().countItem(data.roadType.getSlope().asItem()))));
                            if (!isPlayerCreative(data.player)) {
                                data.player.getInventory().items.stream().filter(x -> x.is(data.roadType.getSlope().asItem())).findFirst().get().shrink(block.getValue());
                                data.item.hurtAndBreak(1, data.player, (player) -> {
                                    player.broadcastBreakEvent(data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                                    canContinue[0] = false;
                                });
                            }
                        } else if (block.getValue() > 7 && (isPlayerCreative(data.player) || data.player.getInventory().countItem(data.roadType.getBlock().asItem()) > 0)) {
                            level.destroyBlock(block.getKey(), !isPlayerCreative(data.player));
                            level.setBlockAndUpdate(block.getKey(), data.roadType.getBlock().defaultBlockState());
                            if (!isPlayerCreative(data.player)) {
                                data.player.getInventory().items.stream().filter(x -> x.is(data.roadType.getBlock().asItem())).findFirst().get().shrink(1);
                                data.item.hurtAndBreak(1, data.player, (player) -> {
                                    player.broadcastBreakEvent(data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                                    canContinue[0] = false;
                                });                             
                            }
                        }
                    }
                }
                return canContinue[0];
            });
        });
        context.get().setPacketHandled(true);
    }

    private static boolean isPlayerCreative(Player pPlayer) {
        return pPlayer.isCreative() || pPlayer.isSpectator();
    }
}
