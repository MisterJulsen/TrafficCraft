package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.core.Location;
import de.mrjulsen.mcdragonlib.net.BaseNetworkPacket;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.ScheduledTask;
import de.mrjulsen.mcdragonlib.util.ScheduledTask.ScheduledTaskContext;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.AsphaltSlope;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool.RoadBuildingData;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

public class RoadBuilderBuildRoadPacket extends BaseNetworkPacket<RoadBuilderBuildRoadPacket> {

    private Location pos1;
    private Location pos2;
    private byte roadWidth;
    private boolean replaceBlocks;
    private RoadType roadType;

    public RoadBuilderBuildRoadPacket() {}
    
    public RoadBuilderBuildRoadPacket(Location pos1, Location pos2, byte roadWidth, boolean replaceBlocks, RoadType roadType) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.roadWidth = roadWidth;
        this.replaceBlocks = replaceBlocks;
        this.roadType = roadType;
    }

    @Override
    public void encode(RoadBuilderBuildRoadPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeNbt(packet.pos1.toNbt());
        buffer.writeNbt(packet.pos2.toNbt());
        buffer.writeByte(packet.roadWidth);
        buffer.writeBoolean(packet.replaceBlocks);
        buffer.writeEnum(packet.roadType);
    }

    @Override
    public RoadBuilderBuildRoadPacket decode(RegistryFriendlyByteBuf buffer) {
        Location pos1 = Location.fromNbt(buffer.readNbt());
        Location pos2 = Location.fromNbt(buffer.readNbt());
        byte roadWidth = buffer.readByte();
        boolean replaceBlocks = buffer.readBoolean();
        RoadType roadType = buffer.readEnum(RoadType.class);

        return new RoadBuilderBuildRoadPacket(pos1, pos2, roadWidth, replaceBlocks, roadType);
    }

    @Override
    public void handle(RoadBuilderBuildRoadPacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            ServerPlayer sender = (ServerPlayer)contextSupplier.get().getPlayer();
            final Level level = sender.level();
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

            ScheduledTask.create(buildingData, level, RoadConstructionTool.BUILD_DELAY_TICKS, buildingData.blocks.size(), packet::run);
        });
    }

    private boolean run(RoadBuildingData data, ScheduledTaskContext context) {
        ServerLevel serverLevel = (ServerLevel)data.player.level();
        ServerPlayer serverPlayer = (ServerPlayer)data.player;
        boolean[] canContinue = new boolean[] { true };
        for (Entry<BlockPos, Integer> block : data.blocks.get(context.iteration()).entrySet()) {
            
            if (!canContinue[0] || !data.player.isAlive()) {
                return false;
            }

            if (!isPlayerCreative(data.player) && (data.player.getInventory().countItem(data.roadType.getSlope().asItem()) <= 0 && data.player.getInventory().countItem(data.roadType.getBlock().asItem()) <= 0)) {
                return false;
            }

            if (context.level().getBlockState(block.getKey()).getBlock().defaultDestroyTime() != Block.INDESTRUCTIBLE) {
                if (block.getValue() > 0 && block.getValue() <= 7 && (isPlayerCreative(data.player) || data.player.getInventory().countItem(data.roadType.getSlope().asItem()) > 0)) {                            
                    context.level().destroyBlock(block.getKey(), !isPlayerCreative(data.player));
                    int layers = Math.min(block.getValue(), isPlayerCreative(data.player) ? Integer.MAX_VALUE : data.player.getInventory().countItem(data.roadType.getSlope().asItem()));
                    context.level().setBlockAndUpdate(block.getKey(), data.roadType.getSlope().defaultBlockState().setValue(AsphaltSlope.LAYERS, layers));
                    if (!isPlayerCreative(data.player)) {
                        int countLeft = layers;
                        Optional<ItemStack> stack;
                        while (canContinue[0] && countLeft > 0 && (stack = data.player.getInventory().items.stream().filter(x -> x.is(data.roadType.getSlope().asItem())).findFirst()).isPresent()) {
                            int removeCount = countLeft;
                            countLeft -= Math.min(countLeft, stack.get().getCount());
                            stack.get().shrink(removeCount);
                            data.item.hurtAndBreak(1, serverLevel, serverPlayer, (item) -> {
                                serverPlayer.onEquippedItemBroken(item, data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                                canContinue[0] = false;
                            });
                        }
                        if (countLeft > 0) {                            
                            canContinue[0] = false;
                        }
                    }
                } else if (block.getValue() > 7 && (isPlayerCreative(data.player) || data.player.getInventory().countItem(data.roadType.getBlock().asItem()) > 0)) {
                    context.level().destroyBlock(block.getKey(), !isPlayerCreative(data.player));
                    context.level().setBlockAndUpdate(block.getKey(), data.roadType.getBlock().defaultBlockState());
                    if (!isPlayerCreative(data.player)) {
                        Optional<ItemStack> stack = data.player.getInventory().items.stream().filter(x -> x.is(data.roadType.getBlock().asItem())).findFirst();
                        if (stack.isPresent()) {
                            stack.get().shrink(1);
                        } else {
                            canContinue[0] = false;
                        }
                        data.item.hurtAndBreak(1, serverLevel, serverPlayer, (item) -> {
                            serverPlayer.onEquippedItemBroken(item, data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                            canContinue[0] = false;
                        });                             
                    }
                }
            }
        }

        if (context.iteration() >= data.blocks.size() - 1) {
            if (context.level().dimension().location().equals(BuiltinDimensionTypes.NETHER.location())) {
                DLUtils.giveAdvancement((ServerPlayer)data.player, TrafficCraft.MOD_ID, "highway_to_hell", "req");
            } else if (context.level().dimension().location().equals(BuiltinDimensionTypes.END.location())) {
                DLUtils.giveAdvancement((ServerPlayer)data.player, TrafficCraft.MOD_ID, "final_destination", "req");
            }
        }

        return canContinue[0];
    }

    private static boolean isPlayerCreative(Player pPlayer) {
        return pPlayer.isCreative() || pPlayer.isSpectator();
    }
}
