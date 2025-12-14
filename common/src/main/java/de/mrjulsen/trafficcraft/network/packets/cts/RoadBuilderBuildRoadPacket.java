package de.mrjulsen.trafficcraft.network.packets.cts;

import java.util.Map.Entry;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.data.WorldLocation;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.ScheduledTask;
import de.mrjulsen.mcdragonlib.util.ScheduledTask.ScheduledTaskContext;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.AsphaltSlope;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool;
import de.mrjulsen.trafficcraft.item.RoadConstructionTool.RoadBuildingData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

public class RoadBuilderBuildRoadPacket extends NetworkPacketData {

    private static final String NBT_POS1 = "Pos1";
    private static final String NBT_POS2 = "Pos2";
    private static final String NBT_ROAD_WIDTH = "RoadWidth";
    private static final String NBT_REAPLCE_BLOCKS = "ReplaceBlocks";
    private static final String NBT_ROAD_TYPE = "RoadType";

    private WorldLocation pos1;
    private WorldLocation pos2;
    private byte roadWidth;
    private boolean replaceBlocks;
    private RoadType roadType;

    public RoadBuilderBuildRoadPacket(DLStatus status) {
        super(status);
    }
    
    public RoadBuilderBuildRoadPacket(WorldLocation pos1, WorldLocation pos2, byte roadWidth, boolean replaceBlocks, RoadType roadType) {
        super(DLStatus.OK);
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.roadWidth = roadWidth;
        this.replaceBlocks = replaceBlocks;
        this.roadType = roadType;
    }    

    @Override
    protected void write(CompoundTag nbt) {
        nbt.put(NBT_POS1, pos1.toNbt());
        nbt.put(NBT_POS2, pos2.toNbt());
        nbt.putByte(NBT_ROAD_WIDTH, roadWidth);
        nbt.putBoolean(NBT_REAPLCE_BLOCKS, replaceBlocks);
        nbt.putInt(NBT_ROAD_TYPE, roadType.getIndex());
    }

    @Override
    protected void read(CompoundTag nbt) {
        this.pos1 = WorldLocation.loadFromNbt(nbt.getCompound(NBT_POS1));
        this.pos2 = WorldLocation.loadFromNbt(nbt.getCompound(NBT_POS2));
        this.roadWidth = nbt.getByte(NBT_ROAD_WIDTH);
        this.replaceBlocks = nbt.getBoolean(NBT_REAPLCE_BLOCKS);
        this.roadType = RoadType.getRoadTypeByIndex(nbt.getInt(NBT_ROAD_TYPE));
    }

    public static void handle(RoadBuilderBuildRoadPacket packet, NetworkPacketContext context) {        
        ServerPlayer sender = (ServerPlayer)context.getPlayer();
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
    }

    private boolean run(RoadBuildingData data, ScheduledTaskContext context) {
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
                            data.item.hurtAndBreak(1, data.player, (player) -> {
                                player.broadcastBreakEvent(data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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
                        data.item.hurtAndBreak(1, data.player, (player) -> {
                            player.broadcastBreakEvent(data.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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
